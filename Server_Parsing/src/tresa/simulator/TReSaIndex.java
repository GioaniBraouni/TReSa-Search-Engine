package tresa.simulator;


import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;


public class TReSaIndex {
    private IndexWriter writer;
    IndexSearcher searcher;
    String indexDir = "Index";
    QueryParser queryParser;
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


        List<String> stopWords = List.of("places","people","title","body","reuter");

        CharArraySet stopSet = new CharArraySet(stopWords,true);

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

        stopSet.addAll(enStopSet);

        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer(stopSet)); // Filters StandardTokenizer with LowerCaseFilter and StopFilter, using a configurable list of stop words.

        writer = new IndexWriter(indexDirectory, config); // The IndexWriterConfig.OpenMode option on IndexWriterConfig.setOpenMode(OpenMode) determines whether a new index is created, or whether an existing index is opened.
    }



    public void close() throws IOException {
        writer.close();
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
        Path path = Paths.get(indexDir);
        File dir = new File(indexDir);

        Document document = getDocument(file);


        if (writer.getConfig().getOpenMode() == IndexWriterConfig.OpenMode.CREATE_OR_APPEND) {
            if (!isAlreadyIndexed(document)) {
                System.out.println("Indexing " + file.getCanonicalPath());
                writer.addDocument(document);
            } else {
                System.out.println("Replacing file : " + file.getCanonicalPath());
                deletingFiles(file.getCanonicalPath());
                writer.addDocument(document);

            }
        }

    }

    protected Document getDocument(File file) throws IOException, NoSuchAlgorithmException {

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

        writer.deleteDocuments(fileTerm);
        writer.deleteDocuments(contentTerm);
        writer.deleteDocuments(titleTerm);
        writer.deleteDocuments(placesTerm);
        writer.deleteDocuments(peopleTerm);

        //writer.commit();
        //writer.forceMergeDeletes();

        //writer.close();

    }

    private boolean isAlreadyIndexed(Document document) throws IOException {
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