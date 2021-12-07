package tresa.simulator;

import java.io.File;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import org.apache.lucene.document.Document;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;

// Added 8.0.0

public class TReSaMain {
    String indexDir = "Index"; // REDO
    String dataDir = "Server_Parsing/Reuters";
    QuerySearch querySearch;
    TReSaIndex sec;

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
                //System.out.println("Name of dir");
                String selectedDir = scanner.next();
                try {
                    tester.createOneIndex(selectedDir);

                } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }

            }else if(selection == 3){ // Add Single File
                //System.out.println("Name of file");
                String selectedFile = server.complete;
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

            }else if(selection == 6) //BooleanQuery search
            {
                try
                {
                    String queryInput = scanner.nextLine();
                    String queryInput2 = scanner.nextLine();
                    //System.out.println(queryInput);
                    QuerySearch docSearcher = new QuerySearch();
                    ScoreDoc[] searchResults = docSearcher.testSearch(queryInput,queryInput2);
                    printSearchResults(searchResults,queryInput,docSearcher.getIndexSearcher());
                    docSearcher.closeReader();

                } catch (IOException | ParseException e)
                {
                    e.printStackTrace();
                }

            }

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


    private void testFileToDelete(String deleteFile) throws IOException, NoSuchAlgorithmException {
        File file = new File(deleteFile);
        sec = new TReSaIndex(indexDir);
        sec.deletingFiles(deleteFile);
        sec.close();
    }

    private static void printSearchResults(ScoreDoc[] searchResults, String searchQuery, IndexSearcher indexSearcher) {
        if (searchResults != null && searchResults.length > 0)
        {
            System.out.println(searchResults.length + " documents found");
            for (int i = 0; i < searchResults.length; i++)
            {
                int docIndex = searchResults[i].doc;
                Document doc;
                try {
                    doc = indexSearcher.doc(docIndex);
                    String filepath = doc.get("filepath");
                    File file = new File(filepath);
                    String filename = file.getName();
                    System.out.println("Document Name: " + filename);
                    System.out.println("Rank: " + (i + 1));
                    System.out.println("Path: " + filepath);
                    System.out.println("Relevance Score: " + searchResults[i].score);
                } catch (IOException e) {
                    System.out.println("Document with id - " + docIndex + " no longer exists");
                }
            }
        } else
            System.out.println("No documents found for the query: " + searchQuery);
    }
}
