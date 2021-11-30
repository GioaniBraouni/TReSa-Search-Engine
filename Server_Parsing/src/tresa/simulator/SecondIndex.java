package tresa.simulator;


import java.io.*;
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
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


public class SecondIndex {
    private IndexWriter writer;

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
        //Path indexPath = Files.createTempDirectory(indexDirectoryPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        //create the indexer



//        final List<String> stopWords = List.of(
//                "an","a","and","are","as","at","be","but","by","for","if","in","into","is","it","no","not","of","on","or","s","such","t","that","the","their","then","there","these","they","this","to","was","will","with","www"
//        );
//
//        final CharArraySet stopSet = new CharArraySet(stopWords,true);
//
//        StopAnalyzer stopAnalyzer = new StopAnalyzer(stopSet);
//
//        StandardAnalyzer std = new StandardAnalyzer(stopSet);

        List<String> stopWords = List.of("places","people","title","body");

        CharArraySet stopSet = new CharArraySet(stopWords,true);

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;


        stopSet.addAll(enStopSet);

        Map<String,Analyzer> analyzerPerField = new HashMap<>();
        //analyzerPerField.put(LuceneConstants.BODY,new EnglishAnalyzer());
        analyzerPerField.put(LuceneConstants.TITLE,new StandardAnalyzer(stopSet));
        analyzerPerField.put(LuceneConstants.PLACES,new StandardAnalyzer(stopSet));
        //analyzerPerField.put(LuceneConstants.PEOPLE,new EnglishAnalyzer());

        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(stopSet),analyzerPerField);




        //EnglishAnalyzer englishAnalyzer = new EnglishAnalyzer(stopSet);



        IndexWriterConfig config = new IndexWriterConfig(wrapper); // Filters StandardTokenizer with LowerCaseFilter and StopFilter, using a configurable list of stop words.

        writer = new IndexWriter(indexDirectory, config); // The IndexWriterConfig.OpenMode option on IndexWriterConfig.setOpenMode(OpenMode) determines whether a new index is created, or whether an existing index is opened.
    }

    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }

    private Document getDocument(File file) throws IOException {


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
                document.add(new Field(LuceneConstants.CONTENTS, currentLine, TextField.TYPE_STORED));
            }





        }


        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(), StringField.TYPE_STORED);
        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH, file.getCanonicalPath(), StringField.TYPE_STORED);
        document.add(fileNameField);
        document.add(filePathField);

//        Field title = new Field(LuceneConstants.TITLE,currentLine,TextField.TYPE_STORED);
//
//        Field contentField = new Field(LuceneConstants.CONTENTS, currentLine,
//                TextField.TYPE_STORED);
//        //index file name
//        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(),
//                StringField.TYPE_STORED);
//        //index file path
//        Field filePathField = new Field(LuceneConstants.FILE_PATH,
//                file.getCanonicalPath(), StringField.TYPE_STORED);
//
//        document.add(title);
//        document.add(contentField);
//        document.add(fileNameField);
//        document.add(filePathField);
        br.close();
        return document;
    }

    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "  + file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }

    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException {
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
}
