package tresa.simulator;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

public class Server extends Thread {
    public String complete = "";
    public static int ended = 0;
    TReSaMain main;




    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {

            while (true) {
                Socket client = serverSocket.accept();
                Scanner fromClient = new Scanner(client.getInputStream());
                PrintWriter toClient = new PrintWriter(client.getOutputStream(),true);

                while (fromClient.hasNextLine()) {
                    String clientString = fromClient.nextLine();



                    this.complete = clientString;

                    if (!this.complete.equals("")) {
                        if (this.complete.contains("@@@")) {
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            System.out.println("Name of file");
                            String selectedFile = this.complete;
                            try {
                                tester.singleFile(selectedFile);
                            } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }else if (this.complete.contains("!@#")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            //System.out.println("Name of file");
                            String selectedFile = this.complete;
                            try {
                                tester.createOneIndex(selectedFile);
                            } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }else {

                            //System.out.println(queryInput);
                            QuerySearch docQuerySearch = new QuerySearch();
                            ScoreDoc[] searchResults = docQuerySearch.search(this.complete,"100");
                            toClient.println(TReSaMain.printSearchResults(searchResults,this.complete, docQuerySearch.getIndexSearcher()));
                            docQuerySearch.closeReader();

                        }

//                        System.out.println(this.complete);

                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


    }



}