package tresa.simulator;


import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.IndexSearcher;

import java.io.*;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.Locale;


public class TReSaIndex {
    IndexSearcher searcher;
    String indexDir = "Index";
    HashSet<String> articleID = new HashSet<>();

    public void commit() throws IOException {
        TReSaMain.writer.commit();
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException, NoSuchAlgorithmException {
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
        return TReSaMain.writer.numRamDocs();
    }


    public int createSingleIndex(String fileName, FileFilter filter) throws
            IOException,  NoSuchAlgorithmException {
        //get all files in the data directory
        File files = new File(fileName);

        if (!files.isHidden()
                && files.exists()
                && files.canRead()
                && filter.accept(files)
        ) {
            indexFile(files);
        }

        return TReSaMain.writer.numRamDocs();
    }


    private void indexFile(File file) throws IOException, NoSuchAlgorithmException
    {
        Document document = getDocument(file);

        if (TReSaMain.writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE_OR_APPEND) {
            if (!TReSaMain.initialIndex) {
                System.out.println("Indexing " + file.getCanonicalPath());
                TReSaMain.hashSet.add(file.getName());
                TReSaMain.writer.addDocument(document);
            }
            else
            {
                //Υποθετουμε οτι για την τροποποιηση πρεπει να διαγραψουμε το κειμενο και να το ξανακανουμε add
                //Επισης οταν κανουμε add πρεπει να ενημερωσουμε το hashset και να ενημερωσουμε index
                //Επισης οταν κανουμε delete τα βηματα ειναι ιδια
                if(TReSaMain.hashSet.contains(file.getName()))
                    System.out.println("File " +file.getName()+ " exists");
                else
                {
                    TReSaMain.hashSet.add(file.getName());
                    System.out.println("Adding: " + file.getCanonicalPath());
                    TReSaMain.writer.addDocument(document);
                }
                //deletingFiles(file.getCanonicalPath());
            }
        }

    }

    protected Document getDocument(File file) throws IOException
    {
        Document document = new Document();
        //index file contents
        BufferedReader articleReader = new BufferedReader(new FileReader(file));

        Preprocessor prep;

        String currentLine; // If line contains <TITLE> give more weight. (IDEA)
        while ((currentLine = articleReader.readLine()) != null)
        {
            String result = currentLine.toLowerCase(Locale.ROOT);

            prep = new Preprocessor(result);

            if (result.contains("title")) {
                document.add(new Field(TReSaFields.TITLE, prep.toString(), TextField.TYPE_STORED));
            } else if (result.contains("places")) {
                document.add(new Field(TReSaFields.PLACES, prep.toString(), TextField.TYPE_STORED));
            } else if (result.contains("people")) {
                //result = result.replaceAll("people"," ");
                document.add(new Field(TReSaFields.PEOPLE, prep.toString(), TextField.TYPE_STORED));
            } else {
                document.add(new Field(TReSaFields.BODY, prep.toString(), TextField.TYPE_STORED));
            }
            //Πιθανον σε καποιο field να μην χρειαζεται η προεπεξεργασια.
        }

        document.add(new Field(TReSaFields.FILENAME,file.getName(),StringField.TYPE_STORED));

        articleReader.close();
        return document;
    }


    public void deletingFiles(String fileName) throws IOException, NoSuchAlgorithmException {
        File file = new File(fileName);
        deleteDoc(file);
    }


    private void deleteDoc(File file) throws IOException, NoSuchAlgorithmException {
        Document doc = getDocument(file);
        Term contentTerm = new Term(TReSaFields.BODY,doc.get(TReSaFields.BODY));
        Term titleTerm = new Term(TReSaFields.TITLE,doc.get(TReSaFields.TITLE));
        Term placesTerm = new Term(TReSaFields.PLACES,doc.get(TReSaFields.PLACES));
        Term peopleTerm = new Term(TReSaFields.PEOPLE,doc.get(TReSaFields.PEOPLE));
        Term fileTerm = new Term(TReSaFields.FILENAME,doc.get(TReSaFields.FILENAME));

        TReSaMain.writer.deleteDocuments(fileTerm);
        TReSaMain.writer.deleteDocuments(contentTerm);
        TReSaMain.writer.deleteDocuments(titleTerm);
        TReSaMain.writer.deleteDocuments(placesTerm);
        TReSaMain.writer.deleteDocuments(peopleTerm);

    }
}