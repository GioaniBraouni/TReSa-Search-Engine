package tresa.simulator;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;

import org.apache.lucene.analysis.*;

import org.apache.lucene.analysis.en.EnglishAnalyzer;

import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;

import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class TReSaIndex {
    private IndexWriter writer;
    IndexSearcher searcher;
    String indexDir = "Index";
    HashSet<String> articleID = new HashSet<>();

    /*
    TODO FOR INDEX WRITER
    In either case, documents are added with addDocument and removed with deleteDocuments(Term...) or deleteDocuments(Query...).
    A document can be updated with updateDocument (which just deletes and then adds the entire document).
    When finished adding, deleting and updating documents, close should be called.
     */
    public TReSaIndex(String indexDirectoryPath) throws IOException {
        //this directory will contain the indexes
        Path indexPath = Paths.get(indexDirectoryPath);
        if (!Files.exists(indexPath)) {
            Files.createDirectory(indexPath);
        }

        Directory indexDirectory = FSDirectory.open(indexPath);


        List<String> stopWords = List.of("places","people","title","body");

        CharArraySet stopSet = new CharArraySet(stopWords,true);

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;


        stopSet.addAll(enStopSet);

        IndexWriterConfig config = new IndexWriterConfig(new EnglishAnalyzer(stopSet)); // Filters StandardTokenizer with LowerCaseFilter and StopFilter, using a configurable list of stop words.

        writer = new IndexWriter(indexDirectory, config); // The IndexWriterConfig.OpenMode option on IndexWriterConfig.setOpenMode(OpenMode) determines whether a new index is created, or whether an existing index is opened.
    }



    public void close() throws IOException {
        writer.close();
    }

    protected Document getDocument(File file) throws IOException, NoSuchAlgorithmException {


        Document document = new Document();
        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));

        Preprocessor prep;

        String currentLine; // If line contains <TITLE> give more weight. (IDEA)
        while ((currentLine = br.readLine()) != null)
        {
            String result = currentLine.toLowerCase(Locale.ROOT);

            prep = new Preprocessor(currentLine);

            if (result.contains("title")) {
                document.add(new Field(LuceneConstants.TITLE, prep.toString(), TextField.TYPE_STORED));
            } else if (result.contains("places")) {
                document.add(new Field(LuceneConstants.PLACES, prep.toString(), TextField.TYPE_STORED));
            } else if (result.contains("people")) {
                //result = result.replaceAll("people"," ");
                document.add(new Field(LuceneConstants.PEOPLE, prep.toString(), TextField.TYPE_STORED));
            } else if (result.contains("body")){
                document.add(new Field(LuceneConstants.BODY, prep.toString(), TextField.TYPE_STORED));
            }
            //Πιθανον σε καποιο field να μην χρειαζεται η προεπεξεργασια.
        }

        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);
        document.add(fileNameField);
        document.add(filePathField);

        br.close();
        return document;
    }

    //DELE
    private void deleteDoc(File file) throws IOException, NoSuchAlgorithmException {
        Document doc = getDocument(file);
        Term fileTerm = new Term(LuceneConstants.FILE_NAME,doc.get(LuceneConstants.FILE_NAME));
        Term contentTerm = new Term(LuceneConstants.BODY,doc.get(LuceneConstants.BODY));
        Term pathTerm = new Term(LuceneConstants.FILE_PATH,doc.get(LuceneConstants.FILE_PATH));
        Term titleTerm = new Term(LuceneConstants.TITLE,doc.get(LuceneConstants.TITLE));
        Term placesTerm = new Term(LuceneConstants.PLACES,doc.get(LuceneConstants.PLACES));
        Term peopleTerm = new Term(LuceneConstants.PEOPLE,doc.get(LuceneConstants.PEOPLE));

        System.out.println(peopleTerm.toString());
        System.out.println(contentTerm);

        writer.deleteDocuments(fileTerm);
        writer.deleteDocuments(contentTerm);
        writer.deleteDocuments(pathTerm);
        writer.deleteDocuments(titleTerm);
        writer.deleteDocuments(placesTerm);
        writer.deleteDocuments(peopleTerm);

        //writer.commit();
        writer.forceMergeDeletes();
        writer.close();

    }
    //DELE
    public void deletingFiles(String fileName) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);
        System.out.println("Deleting from Index file: " + file.getCanonicalPath());
        deleteDoc(file);
        close();
    }

    private void indexFile(File file) throws IOException, NoSuchAlgorithmException {
        Path path = Paths.get(indexDir);
        File dir = new File(indexDir);

        Document document = getDocument(file);

            System.out.print("Does the file exists in the index? ");
            if (!articleID.contains(file.getName()))
            {
                System.out.println(articleID.contains(file.getName()));
                articleID.add(file.getName());
                System.out.println("Indexing " + file.getCanonicalPath());
                writer.addDocument(document);
            }else {
                System.out.println(articleID.contains(file.getName()));
            }


          //TODO edw prepei na valw check gia ta fields. Prepei prwta na parw to document
        //writer.addDocument(document);

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

}