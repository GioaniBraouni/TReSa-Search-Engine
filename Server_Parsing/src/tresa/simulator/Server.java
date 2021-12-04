package tresa.simulator;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class Server extends Thread {
    private String complete = "";




    @Override
    public void run() {

        try (ServerSocket serverSocket = new ServerSocket(5555)) {


            int i = 1;
            while (true) {

                Socket client = serverSocket.accept();
                Scanner fromClient = new Scanner(client.getInputStream());

                while (fromClient.hasNextLine()) {
                    String clientString = fromClient.nextLine();

                    this.complete = clientString;

                    if (!this.complete.equals("")) {

                        System.out.println(this.complete);
                        complete = "";

                    }
                }

            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }



}