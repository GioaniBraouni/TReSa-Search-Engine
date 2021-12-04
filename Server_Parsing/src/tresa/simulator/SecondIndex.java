package tresa.simulator;


import java.io.*;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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


public class SecondIndex{
    private IndexWriter writer;
    IndexSearcher searcher;
    String indexDir = "Index";
    /*
    TODO FOR INDEX WRITER
    In either case, documents are added with addDocument and removed with deleteDocuments(Term...) or deleteDocuments(Query...).
    A document can be updated with updateDocument (which just deletes and then adds the entire document).
    When finished adding, deleting and updating documents, close should be called.
     */
    public SecondIndex(String indexDirectoryPath) throws IOException {
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

        Map<String,Analyzer> analyzerPerField = new HashMap<>();

        analyzerPerField.put(LuceneConstants.TITLE,new StandardAnalyzer(stopSet));
        analyzerPerField.put(LuceneConstants.PLACES,new StandardAnalyzer(stopSet));


        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(stopSet),analyzerPerField);

        IndexWriterConfig config = new IndexWriterConfig(wrapper); // Filters StandardTokenizer with LowerCaseFilter and StopFilter, using a configurable list of stop words.

        writer = new IndexWriter(indexDirectory, config); // The IndexWriterConfig.OpenMode option on IndexWriterConfig.setOpenMode(OpenMode) determines whether a new index is created, or whether an existing index is opened.
    }



    public void close() throws IOException {
        writer.close();
    }

    protected Document getDocument(File file) throws IOException {


        Document document = new Document();
        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));


        String currentLine; // If line contains <TITLE> give more weight. (IDEA)
        while ((currentLine = br.readLine()) != null) {


            String result = currentLine.toLowerCase(Locale.ROOT);


            if (result.contains("title")) {
                //result = result.replaceAll("title"," ");
                document.add(new Field(LuceneConstants.TITLE, currentLine, TextField.TYPE_STORED));
            } else if (result.contains("places")) {
                //result = result.replaceAll("places"," ");
                document.add(new Field(LuceneConstants.PLACES, currentLine, TextField.TYPE_STORED));
            } else if (result.contains("people")) {
                //result = result.replaceAll("people"," ");
                document.add(new Field(LuceneConstants.PEOPLE, currentLine, TextField.TYPE_STORED));
            } else {
                document.add(new Field(LuceneConstants.BODY, currentLine, TextField.TYPE_STORED));
            }





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
    private void deleteDoc(File file) throws IOException {
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
    public void deletingFiles(String fileName) throws IOException {
        File file = new File(fileName);
        System.out.println("Deleting from Index file: " + file.getCanonicalPath());
        deleteDoc(file);
        close();
    }

    private void indexFile(File file) throws IOException, ParseException {
        Path path = Paths.get(indexDir);
        File dir = new File(indexDir);
        if (Objects.requireNonNull(dir.listFiles()).length < 2 || !isAlreadyIndexed(file)) {
            System.out.println("Indexing " + file.getCanonicalPath());
            Document document = getDocument(file);
            //TODO edw prepei na valw chack gia ta fields. Prepei prwta na parw to document
            writer.addDocument(document);
        }
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException, ParseException {
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
            IOException, ParseException {
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

    private boolean isAlreadyIndexed(File file) throws IOException {
        // Prwto check gia file name
        String url = file.getAbsolutePath();
        TermQuery query1 = new TermQuery(new Term(LuceneConstants.FILE_PATH,url));
        BooleanQuery matchingQuery = new BooleanQuery.Builder()
                .add(query1, BooleanClause.Occur.SHOULD)
                .build();
        Path path = Paths.get(indexDir);
        Directory index = FSDirectory.open(path);
        IndexReader r = DirectoryReader.open(index);
        searcher = new IndexSearcher(r);

        TopDocs results = searcher.search(matchingQuery,1);

        if (results.totalHits.value == 0){
            System.out.println(results.totalHits.value );
            r.close();
            return false;

        }
        r.close();
        return true;

//        searcher = new IndexSearcher();
//        String url = file.getAbsolutePath();
//        TopDocs result = searcher.search(new TermQuery(new Term(LuceneConstants.FILE_PATH,url)),1);
//        if (result.totalHits.value == 0){
//            return true;
//        }
//        return false;
    }

    private static boolean isDirEmpty(final Path directory) throws IOException {
        try(DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory)) {
            return !dirStream.iterator().hasNext();
        }
    }

}