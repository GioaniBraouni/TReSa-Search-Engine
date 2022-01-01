package tresa.simulator;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.lucene.analysis.*;

import org.apache.lucene.analysis.core.StopFilterFactory;
import org.apache.lucene.analysis.custom.CustomAnalyzer;
import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilter;
import org.apache.lucene.analysis.miscellaneous.WordDelimiterGraphFilterFactory;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.synonym.SynonymFilterFactory;
import org.apache.lucene.analysis.util.TokenizerFactory;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import static tresa.simulator.TReSaMain.writer;


public class TReSaIndex {
    IndexSearcher searcher;
    String indexDir = "Index";
    QueryParser queryParser;

    public void commit() throws IOException {
        writer.commit();
    }

    public void close() throws IOException {
        writer.close();
    }

    public int deleteFolderFromIndex(String dataDirPath, FileFilter filter) throws
            IOException, ParseException, NoSuchAlgorithmException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();
        for (File file : files) {
            if (!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
            ) {
                fromUI(file.toString());
            }
        }
        return writer.numRamDocs();
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException, ParseException, NoSuchAlgorithmException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();
        for (File file : files) {
            if (!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
            ) {
                indexFile(file);
            }
        }
        return writer.numRamDocs();
    }


    public int createSingleIndex(String fileName, FileFilter filter) throws
            IOException, ParseException, NoSuchAlgorithmException {
        //get all files in the data directory
        File files = new File(fileName);

        if (!files.isHidden()
                && files.exists()
                && files.canRead()
                && filter.accept(files)
        ) {
            indexFile(files);
        }

        return writer.numRamDocs();
    }


    private void indexFile(File file) throws IOException, NoSuchAlgorithmException {
        Document document = getDocument(file);

        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE_OR_APPEND) {
            if (!Server.hashSet.contains(file.getName())) {
                System.out.println("Indexing " + file.getCanonicalPath());
                Server.hashSet.add(file.getName());
                writer.addDocument(document);
            }
            else
            {
                //Υποθετουμε οτι για την τροποποιηση πρεπει να διαγραψουμε το κειμενο και να το ξανακανουμε add
                //Επισης οταν κανουμε add πρεπει να ενημερωσουμε το hashset και να ενημερωσουμε index
                //Επισης οταν κανουμε delete τα βηματα ειναι ιδια
                if(Server.hashSet.contains(file.getName()))
                {
                    System.out.println("Found " +file.getName() + " in the index");
                    deleteDoc(file);
                    Server.hashSet.add(file.getName());

                    System.out.println("Replacing with the new version");
                    writer.addDocument(document);

                    System.out.println();
                }
                else
                {
                    Server.hashSet.add(file.getName());
                    System.out.println("Adding: " + file.getCanonicalPath());
                    writer.addDocument(document);
                }
                //deletingFiles(file.getCanonicalPath());
            }
        }
    }

    protected Document getDocument(File file) throws IOException, NoSuchAlgorithmException {

        Document document = new Document();
        //index file contents
        BufferedReader articleReader = new BufferedReader(new FileReader(file));

        Preprocessor prep;

        StringBuilder stringBuilder = new StringBuilder();

        String currentLine; // If line contains <TITLE> give more weight. (IDEA)
        try {


            while ((currentLine = articleReader.readLine()) != null) {
                String result = currentLine.toLowerCase(Locale.ROOT);

                //prep = new Preprocessor(result);

                if (result.contains("</title>")) {
                    document.add(new Field(TReSaFields.TITLE, result.toString(), TextField.TYPE_STORED));
                } else if (result.contains("</places>")) {
                    document.add(new Field(TReSaFields.PLACES, result.toString(), TextField.TYPE_STORED));
                } else if (result.contains("</people>")) {
                    //result = result.replaceAll("people"," ");
                    document.add(new Field(TReSaFields.PEOPLE, result.toString(), TextField.TYPE_STORED));
                } else {
                    assert stringBuilder != null;
                    stringBuilder.append(result.toString()).append(" ");
                    //document.add(new Field(TReSaFields.BODY, prep.toString(), TextField.TYPE_STORED));
                }
                //Πιθανον σε καποιο field να μην χρειαζεται η προεπεξεργασια.
            }
            document.add(new Field(TReSaFields.BODY, stringBuilder.toString(), TextField.TYPE_STORED));
            document.add(new Field(TReSaFields.FILENAME, file.getName(), StringField.TYPE_STORED));

            articleReader.close();
        }catch (NullPointerException e){
            e.printStackTrace();
        }
        return document;
    }


    public void deletingFiles(String fileName) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);
        deleteDoc(file);
    }

    public boolean fromUI(String fileName) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);
        return deleteDoc(file);
    }


    private boolean deleteDoc(File file) throws IOException, NoSuchAlgorithmException {
        try {
            Document doc = getDocument(file);
            System.out.println("Deleting: " + file.getCanonicalPath());
            if(Server.hashSet.contains(file.getName()))
            {
                Term contentTerm = new Term(TReSaFields.BODY, doc.get(TReSaFields.BODY));
                Term titleTerm = new Term(TReSaFields.TITLE, doc.get(TReSaFields.TITLE));
                Term placesTerm = new Term(TReSaFields.PLACES, doc.get(TReSaFields.PLACES));
                Term peopleTerm = new Term(TReSaFields.PEOPLE, doc.get(TReSaFields.PEOPLE));
                Term fileTerm = new Term(TReSaFields.FILENAME, doc.get(TReSaFields.FILENAME));

                writer.deleteDocuments(fileTerm);
                writer.deleteDocuments(contentTerm);
                writer.deleteDocuments(titleTerm);
                writer.deleteDocuments(placesTerm);
                writer.deleteDocuments(peopleTerm);

                Server.hashSet.remove(file.getName());
            }
            else
            {
                System.err.println("File " + file.getName() + " does not exist in the index");
                Server.foundError = true;
                return false;
            }
        }catch (NullPointerException e){
            System.err.println("Wrong File Format");
        }
        return false;




    }

    protected boolean isAlreadyIndexed(Document document) throws IOException {
        // Prwto check gia file name

        Path path = Paths.get(indexDir);
        Directory index = FSDirectory.open(path);
        if (!DirectoryReader.indexExists(index))
        {
            return false;
        }
        TermQuery query1 = new TermQuery(new Term(TReSaFields.FILENAME,document.get(TReSaFields.FILENAME)));
        BooleanQuery matchingQuery = new BooleanQuery.Builder()
                .add(query1,BooleanClause.Occur.SHOULD)
                .build();


        IndexReader r = DirectoryReader.open(index);
        searcher = new IndexSearcher(r);
        TopDocs results = searcher.search(matchingQuery,1);

        if (results.totalHits.value == 0){
            r.close();
            return false;

        }
        r.close();
        return true;

    }



}