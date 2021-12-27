package tresa.simulator;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.Formatter;
import java.util.logging.SimpleFormatter;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.highlight.*;


// Added 8.0.0

public class TReSaMain {
    String indexDir = "Index"; // REDO
    String dataDir = "Reuters";
    QuerySearch querySearch;
    TReSaIndex sec;
    public static Query query;

    public static void main(String[] args) {
        Server server = new Server();
        server.start();
        TReSaMain tester = new TReSaMain();
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


//            else
//            {
//                try {
//                    sec.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }

        }


    }
    private void createIndex() throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createIndex(dataDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");

    }

    //TODO MERGE createIndex && createOneIndex AT THE END

    protected void createOneIndex(String selectedDir) throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createIndex(selectedDir, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");

    }

    protected void singleFile(String selectedFile) throws IOException, ParseException, NoSuchAlgorithmException {
        sec = new TReSaIndex(indexDir);
        int numIndexed;
        long startTime = System.currentTimeMillis();
        numIndexed = sec.createSingleIndex(selectedFile, new TextFileFilter());
        long endTime = System.currentTimeMillis();
        sec.close();
        System.out.println(numIndexed+" File(s) indexed, time taken: " +
                (endTime-startTime)+" ms");

    }


    protected void testFileToDelete(String deleteFile) throws IOException, NoSuchAlgorithmException {
        File file = new File(deleteFile);
        sec = new TReSaIndex(indexDir);
        sec.deletingFiles(deleteFile);
        sec.close();
    }

    protected void folderDeletion(String deleteFile) throws IOException, NoSuchAlgorithmException, ParseException {
        File file = new File(deleteFile);
        sec = new TReSaIndex(indexDir);
        sec.deleteFolderFromIndex(deleteFile,new TextFileFilter());
        sec.close();
    }

    protected void deleteSingleFileFromUI(String deleteFile) throws IOException, NoSuchAlgorithmException {
        File file = new File(deleteFile);
        sec = new TReSaIndex(indexDir);
        sec.fromUI(deleteFile);
        sec.close();
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

    protected static HashMap<String,HashMap<String,Float>> printSearchResults(ScoreDoc[] searchResults, String searchQuery, IndexSearcher indexSearcher) {

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
//                        System.out.println("Document Name: " + filename);
//
//                        System.out.println("Rank: " + (i + 1));
//
//                        System.out.println("Path: " + filepath);
//
//                        System.out.println("Relevance Score: " + searchResults[i].score);

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
