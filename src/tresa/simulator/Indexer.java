package tresa.simulator;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

public class Indexer {
    private IndexWriter writer;
    /*
    TODO FOR INDEX WRITER
    In either case, documents are added with addDocument and removed with deleteDocuments(Term...) or deleteDocuments(Query...).
    A document can be updated with updateDocument (which just deletes and then adds the entire document).
    When finished adding, deleting and updating documents, close should be called.
     */
    public Indexer(String indexDirectoryPath) throws IOException {
        //this directory will contain the indexes
        Path indexPath = Paths.get(indexDirectoryPath);
        if(!Files.exists(indexPath)) {
            Files.createDirectory(indexPath);
        }
        //Path indexPath = Files.createTempDirectory(indexDirectoryPath);
        Directory indexDirectory = FSDirectory.open(indexPath);
        //create the indexer
        IndexWriterConfig config = new IndexWriterConfig(new StandardAnalyzer()); // Filters StandardTokenizer with LowerCaseFilter and StopFilter, using a configurable list of stop words.
        writer = new IndexWriter(indexDirectory, config); // The IndexWriterConfig.OpenMode option on IndexWriterConfig.setOpenMode(OpenMode) determines whether a new index is created, or whether an existing index is opened.
    }
    public void close() throws CorruptIndexException, IOException {
        writer.close();
    }
    private Document getDocument(File file) throws IOException {



        Document document = new Document();
        //index file contents
        BufferedReader br = new BufferedReader(new FileReader(file));

        String currentLine = br.readLine().toString(); // If line contains <TITLE> give more weight. (IDEA)

        System.out.println(currentLine);

        Field title = new Field(LuceneConstants.TITLE,currentLine,TextField.TYPE_STORED);

        Field contentField = new Field(LuceneConstants.CONTENTS, currentLine,
                TextField.TYPE_STORED);
        //index file name
        Field fileNameField = new Field(LuceneConstants.FILE_NAME, file.getName(),
                StringField.TYPE_STORED);
        //index file path
        Field filePathField = new Field(LuceneConstants.FILE_PATH,
                file.getCanonicalPath(), StringField.TYPE_STORED);

        document.add(title);
        document.add(contentField);
        document.add(fileNameField);
        document.add(filePathField);
        br.close();
        return document;
    }
    private void indexFile(File file) throws IOException {
        System.out.println("Indexing "+file.getCanonicalPath());
        Document document = getDocument(file);
        writer.addDocument(document);
    }
    public int createIndex(String dataDirPath, FileFilter filter) throws
            IOException {
        //get all files in the data directory
        File[] files = new File(dataDirPath).listFiles();
        for (File file : files) {
            if(!file.isDirectory()
                    && !file.isHidden()
                    && file.exists()
                    && file.canRead()
                    && filter.accept(file)
            ){
                indexFile(file);
            }
        }
        return writer.numRamDocs();
    }
}
