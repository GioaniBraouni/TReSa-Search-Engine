package tresa.simulator;

import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
                            } catch (IOException | ParseException | NoSuchAlgorithmException | NullPointerException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }else if (this.complete.contains("6^7")){
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
                        }else if(this.complete.contains("#()")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            //System.out.println("Name of file");
                            String selectedFile = this.complete;
                            try {
                                tester.deleteSingleFileFromUI(selectedFile);
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }else if (this.complete.contains("@-!")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            String selectedFile = this.complete;
                            try {
                                tester.folderDeletion(selectedFile);
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                        else {

                            //System.out.println(queryInput);
                            QuerySearch docQuerySearch = new QuerySearch();
                            ScoreDoc[] searchResults = docQuerySearch.search(this.complete);
                            HashMap<String,HashMap<String,Float>> sendToClient = new HashMap<>();
                            sendToClient = TReSaMain.printSearchResults(searchResults,this.complete, docQuerySearch.getIndexSearcher());

                            StringBuilder stringBuilder = new StringBuilder();

                            try{
                                final OutputStream out = client.getOutputStream();
                                final ObjectOutputStream map = new ObjectOutputStream(out);
                                map.writeObject(sendToClient);
                                out.close();
                            }catch (IOException e){
                                e.printStackTrace();
                            }




//                            toClient.println(stringBuilder.toString());
                            docQuerySearch.closeReader();

                        }

//                        System.out.println(this.complete);

                    }else {
                        toClient.println(" Empty Input");
                    }
                }
            }
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }


    }



}