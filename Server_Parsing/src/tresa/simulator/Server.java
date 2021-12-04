package tresa.simulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread {
    public String complete = "";
    public static int ended = 0;




    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(5555)) {

            while (true) {
                Socket client = serverSocket.accept();
                Scanner fromClient = new Scanner(client.getInputStream());

                while (fromClient.hasNextLine()) {
                    String clientString = fromClient.nextLine();

                    this.complete = clientString;

                    if (!this.complete.equals("")) {

                        LuceneTester tester = new LuceneTester();
                        System.out.println("Name of file");
                        String selectedFile = this.complete;
                        try {
                            tester.singleFile(selectedFile);
                        }catch (IOException e){
                            e.printStackTrace();
                        }
                        this.complete = "";

//                        System.out.println(this.complete);

                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }


    }



}