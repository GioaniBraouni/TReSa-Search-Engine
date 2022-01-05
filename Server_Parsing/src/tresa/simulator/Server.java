package tresa.simulator;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Server extends Thread {
    public String complete = "";
    public static int ended = 0;
    TReSaMain main;
    TReSaIndex index;
    public static HashSet<String> hashSet= new HashSet<String>();
    //public static boolean initialIndex = false;
    public static boolean foundError;
    public File file = new File(this.complete);




    @Override
    public void run() {
        try (ServerSocket serverSocket = new ServerSocket(5555)) {

            while (true) {
                Socket client = serverSocket.accept();
                Scanner fromClient = new Scanner(client.getInputStream());
                PrintWriter toClient = new PrintWriter(client.getOutputStream(),true);

                while (fromClient.hasNextLine()) {
                    String clientString = fromClient.nextLine();


                    foundError = false;
                    this.complete = clientString;

                    if (!this.complete.equals("")) {
                        //addFile
                        if (this.complete.contains("@@@")) {
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            String selectedFile = this.complete;
                            try {
                                tester.singleFile(selectedFile);
                                //initialIndex = true;
                                OutputStream outputStream = client.getOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                                out.writeObject("File " + this.complete + " has been indexed");
                                out.close();
                            } catch (IOException | ParseException | NoSuchAlgorithmException | NullPointerException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }
                        //addFolder
                        else if (this.complete.contains("6^7")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            //System.out.println("Name of file");
                            String selectedFile = this.complete;
                            try {
                                tester.createOneIndex(selectedFile);
                                //initialIndex = true;
                                OutputStream outputStream = client.getOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                                out.writeObject("Folder " + this.complete + " has been indexed");
                                out.close();
                            } catch (IOException | ParseException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }
                        //deleteFile
                        else if(this.complete.contains("#()")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();
                            //System.out.println("Name of file");
                            String selectedFile = this.complete;
                            OutputStream outputStream = client.getOutputStream();
                            ObjectOutputStream out = new ObjectOutputStream(outputStream);

                            try {
                                tester.deleteSingleFileFromUI(selectedFile);
                                if(!foundError){
                                    out.writeObject("true");
                                }else{
                                    out.writeObject("false");
                                }
                                out.close();
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                            this.complete = "";
                        }
                        //deleteFolder
                        else if (this.complete.contains("@-!")){
                            this.complete = this.complete.substring(3);
                            TReSaMain tester = new TReSaMain();

                            String selectedFile = this.complete;
                            OutputStream outputStream = client.getOutputStream();
                            ObjectOutputStream out = new ObjectOutputStream(outputStream);

                            try {
                                tester.folderDeletion(selectedFile);
                                if(!foundError)
                                {
                                    out.writeObject("true");
                                }
                                else
                                    out.writeObject("false");
                                out.close();
                            } catch (IOException | NoSuchAlgorithmException e) {
                                e.printStackTrace();
                            }
                        }
                        //articleCompare
                        else if (this.complete.contains("*&&")){
                            try {
                                this.complete = this.complete.substring(3);
                                String[] fileNameAndNumber = this.complete.split(" ");
                                String userFile = fileNameAndNumber[0];
                                int top = Integer.parseInt(fileNameAndNumber[1]);
                                TReSaMain tester = new TReSaMain();
                                String selectedFile = this.complete;
                                OutputStream outputStream = client.getOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                                HashMap<String,Float> endResults = tester.searchFileInIndex(userFile,top);
                                out.writeObject(endResults);
                                out.close();
                            }catch (NoSuchAlgorithmException | IOException e){
                                OutputStream outputStream = client.getOutputStream();
                                ObjectOutputStream out = new ObjectOutputStream(outputStream);
                                out.writeObject(null);
                                out.close();
                                e.printStackTrace();
                            }
                        }
                        else {

                            //System.out.println(queryInput);



                            StringBuilder stringBuilder = new StringBuilder();

                            try{
                                QuerySearch docQuerySearch = new QuerySearch();
                                ScoreDoc[] searchResults = docQuerySearch.search(this.complete);
                                HashMap<String,HashMap<String,Float>> sendToClient = new HashMap<>();
                                sendToClient = TReSaMain.printSearchResults(searchResults,this.complete, docQuerySearch.getIndexSearcher());
                                final OutputStream out = client.getOutputStream();
                                final ObjectOutputStream map = new ObjectOutputStream(out);
                                map.writeObject(sendToClient);
                                out.close();
                                docQuerySearch.closeReader();
                            }catch (IOException | NullPointerException e){
                                final OutputStream out = client.getOutputStream();
                                final ObjectOutputStream map = new ObjectOutputStream(out);
                                map.writeObject(null);
                                out.close();
                                e.printStackTrace();
                            }




//                            toClient.println(stringBuilder.toString());


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

    public void readSet() throws IOException {
        Path path = Paths.get("Index");
        Directory index = FSDirectory.open(path);
        if (!DirectoryReader.indexExists(index))
        {
            return ;
        }
        IndexReader r = DirectoryReader.open(index);
        for (int i=0; i<r.maxDoc(); i++) {
            Document document =r.document(i);
            hashSet.add(document.get("fileName"));
            //System.out.println(document.get("fileName"));
        }


        r.close();


    }



}