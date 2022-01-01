package tresa.simulator;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Formatter;
import java.util.logging.SimpleFormatter;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.PerFieldAnalyzerWrapper;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;


// Added 8.0.0

public class TReSaMain {
    String indexDir = "Index"; // REDO
    String dataDir = "Server_Parsing/Reuters2";
    public static IndexWriter writer;
    QuerySearch querySearch;
    TReSaIndex sec;
    public static Query query;

    public static void main(String[] args) throws IOException {
        Server server = new Server();
        server.readSet();
        server.start();
        TReSaMain tester = new TReSaMain();
        tester.initialiseIndexWriter();
        Scanner scanner = new Scanner(System.in);

        while (true){
            int selection;
            System.out.println("Add reuters(1)");
            System.out.println("Add a folder of articles(2)");
            System.out.println("Add a single article(3)");
            System.out.println("Delete a article(4)");
            System.out.println("Enter a query(5)");
            System.out.println("Quit(7)");
            System.out.println("Enter Choice");
            selection = scanner.nextInt();
            scanner.nextLine();
            if (selection == 1){ // Adds Reuters TODO REMOVE IN THE END
                try {
                    //tester = new LuceneTester();
                    tester.createIndex();

                } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
            }else if(selection == 2){ // Add folder

                String selectedDir = scanner.next();
                try {
                    tester.createOneIndex(selectedDir);

                } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }else if(selection == 3){ // Add Single File
                //System.out.println("Name of file");
                //String selectedFile = server.complete;
                String selectedFile = scanner.nextLine();
                try {
                    tester.singleFile(selectedFile);
                }catch (IOException | ParseException | NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
                server.complete = "";

            }else if (selection == 4){ // Delete file
                System.out.println("Name of the file for deletion");
                String fileToDelete = scanner.next();
                try {
                    //tester.fileToDelete(fileToDelete);
                    tester.testFileToDelete(fileToDelete);
                }catch (IOException |  NoSuchAlgorithmException e){
                    e.printStackTrace();
                }
            }
            else if(selection == 5) //Query search
            {
                try
                {
                    String queryInput = scanner.nextLine();
                    //System.out.println(queryInput);
                    QuerySearch docQuerySearch = new QuerySearch();
                    ScoreDoc[] searchResults = docQuerySearch.search(queryInput);
                    printSearchResults(searchResults,queryInput, docQuerySearch.getIndexSearcher());
                    docQuerySearch.closeReader();

                } catch (IOException | ParseException e)
                {
                    e.printStackTrace();
                }
            }
            else if(selection == 6){
                try{
                    //int queryInput = scanner.nextInt();
                    //String filename = scanner.next();
                    //System.out.println(queryInput);
                    QuerySearch docQuerySearch = new QuerySearch();
                    IndexSearcher indexSearcher = null;
                    File[] files = new File("Reuters").listFiles();
                    int i = 0;
                    for (File f : files)
                    {
                        System.out.println(f.getCanonicalPath());
                        System.out.println(i);
                        ScoreDoc[] searchResults = docQuerySearch.searchFile(f.getCanonicalPath(),5);
                        i++;
                    }
                    //printSearchResults(searchResults,"test",indexSearcher);
                } catch (IOException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        }
    }


    protected HashMap<String , Float> searchFileInIndex(String fileName, int top) throws IOException, NoSuchAlgorithmException, ParseException {

        int i = 0;

        HashMap<String ,Float> results = new HashMap<>();

        Path indexPath = Paths.get("Index");
        Directory indexDirectory = FSDirectory.open(indexPath);
        IndexReader reader = DirectoryReader.open(indexDirectory);

        File file = new File(fileName);

        ScoreDoc[] topResults;

        QuerySearch fileSearcher = new QuerySearch();

        IndexSearcher indexSearcher = new IndexSearcher(reader);

        topResults = fileSearcher.searchFile(fileName,top+1);

        for (ScoreDoc ds : topResults){
            Document document = indexSearcher.doc(ds.doc);
            //System.out.println(document.get("fileName") + " " + topResults[i].score);
            results.put(document.get("fileName"),topResults[i].score);
            i++;
        }


        return results;
    }

    private static void initialiseIndexWriter() throws IOException {
        //this directory will contain the indexes
        Path indexPath = Paths.get("Index");
        if (!Files.exists(indexPath)) {
            Files.createDirectory(indexPath);
        }

        Directory indexDirectory = FSDirectory.open(indexPath);


        List<String> stopWords = List.of("places","people","title","body","reuter");

        CharArraySet stopSet = new CharArraySet(stopWords,true);

        CharArraySet enStopSet = EnglishAnalyzer.ENGLISH_STOP_WORDS_SET;

        stopSet.addAll(enStopSet);

        Map<String, Analyzer> analyzerMap = new HashMap<String, Analyzer>();
        analyzerMap.put(TReSaFields.PEOPLE, new StandardAnalyzer(stopSet));
        analyzerMap.put(TReSaFields.TITLE, new StandardAnalyzer(stopSet));
        analyzerMap.put(TReSaFields.PLACES, new StandardAnalyzer(stopSet));
        PerFieldAnalyzerWrapper wrapper = new PerFieldAnalyzerWrapper(new EnglishAnalyzer(stopSet), analyzerMap);

        IndexWriterConfig config = new IndexWriterConfig(wrapper);

        writer = new IndexWriter(indexDirectory, config);
    }

    private void createIndex() throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex();
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.commit();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");
        System.out.println();

    }

    //TODO MERGE createIndex && createOneIndex AT THE END

    protected void createOneIndex(String selectedDir) throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex();
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createIndex(selectedDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.commit();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");
        System.out.println();

    }

    protected void singleFile(String selectedFile) throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex();
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createSingleIndex(selectedFile, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.commit();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");
        System.out.println();

    }


    protected void testFileToDelete(String deleteFile) throws IOException, NoSuchAlgorithmException {
        File file = new File(deleteFile);
        sec = new TReSaIndex();
        sec.deletingFiles(deleteFile);
        sec.commit();
    }

    protected int folderDeletion(String deleteFile) throws IOException, NoSuchAlgorithmException, ParseException {
        File file = new File(deleteFile);
        sec = new TReSaIndex();
        int numRamDocs = sec.deleteFolderFromIndex(deleteFile,new TextFileFilter());
        System.out.println(numRamDocs);
        sec.commit();
        return numRamDocs;
    }

    protected boolean deleteSingleFileFromUI(String deleteFile) throws IOException, NoSuchAlgorithmException {
        File file = new File(deleteFile);
        sec = new TReSaIndex();
        Boolean wasItDeleted = sec.fromUI(deleteFile);
        sec.commit();
        return wasItDeleted;
    }

    private static String simplePrint(String[] arr){
        for (String frag : arr)
        {

//            System.out.println("=======================");
//            System.out.println(frag);
            return frag;
        }
        return null;
    }

    protected static HashMap<String,HashMap<String,Float>> printSearchResults(ScoreDoc[] searchResults, String searchQuery, IndexSearcher indexSearcher) throws IOException {

        StringBuilder result = new StringBuilder(" ");

        SimpleHTMLFormatter formatter = new SimpleHTMLFormatter();

        QueryScorer scorer = new QueryScorer(query);

        Highlighter highlighter = new Highlighter(formatter, scorer);

        Fragmenter fragmenter = new SimpleSpanFragmenter(scorer, 35);

        highlighter.setTextFragmenter(fragmenter);
        StringBuilder toReturn = new StringBuilder();
        HashMap<String,HashMap<String,Float>> mapped = new HashMap<>();

        try {
            if (searchResults != null && searchResults.length > 0) {

                //System.out.println(searchResults.length + " documents found");
                result = new StringBuilder(searchResults.length + " document found");
                for (int i = 0; i < searchResults.length; i++) {
                    int docIndex = searchResults[i].doc;
                    Document doc;
                    try {
                        doc = indexSearcher.doc(docIndex);
                        String filepath = doc.get("fileName");
                        File file = new File(filepath);
                        String filename = file.getName();
                        result.append(" Document Name: ").append(" Rank: ").append(i + 1).append(" ").append(filename).append(" ")
                                .append(searchResults[i].score).append(doc.get("title"));

                        String body;
                        String title;
                        String places;
                        String people;

                        String[] fragsBody = highlighter.getBestFragments(new StandardAnalyzer(), "body", doc.get(TReSaFields.BODY), 10);
                        body = simplePrint(fragsBody);

                        String[] fragsTitle = highlighter.getBestFragments(new StandardAnalyzer(), "title", doc.get(TReSaFields.TITLE), 10);
                        title = simplePrint(fragsTitle);

                        String[] fragsPlaces = highlighter.getBestFragments(new StandardAnalyzer(), "places", doc.get(TReSaFields.PLACES), 10);
                        places = simplePrint(fragsPlaces);

                        String[] fragsPeople = highlighter.getBestFragments(new StandardAnalyzer(), "people", doc.get(TReSaFields.PEOPLE), 10);
                        people = simplePrint(fragsPeople);

                        String[] allStrings = {body, title, places, people};

                        for (String s : allStrings) {
                            if (s != null) {
//                                mapped.put(filename, new HashMap(){{(put(s,searchResults[i].score))}});
                                HashMap<String,Float> temp = new HashMap<>();
                                temp.put(s,searchResults[i].score);
                                mapped.put(filename, temp);
                            }
                        }



                    } catch (IOException e) {
                        //System.out.println("Document with id - " + docIndex + " no longer exists");
                        result.append("Document with id - ").append(docIndex).append(" no longer exists");
                    } catch (InvalidTokenOffsetsException e) {
                        e.printStackTrace();
                    }
                }
            } else {
                //System.out.println("No documents found for the query: " + searchQuery);
                result.append("No documents found for the query: ").append(searchQuery);
            }
        }catch (NullPointerException e){
            System.err.println("File with wrong format");
        }


        return mapped;
    }




}
