package main.java.serverchat;

import java.io.IOException;
import java.util.Scanner;

public class ClientInterface implements Runnable {

    Client client;

    public ClientInterface(Client client) {
        this.client = client;
    }

    @Override
    public void run() {
        // String to read message from input
        Scanner scanner = new Scanner(System.in);
        String line = "";

        // keep reading until "Log off" is input
        while (!line.equals("Log off")) {
            client.setSocketTimeout(60000);
            line = scanner.nextLine();
            try {
                client.parseRequest(line);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        client.closeTCPSocket();
    }
}
