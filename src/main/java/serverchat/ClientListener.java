package main.java.serverchat;

import java.io.BufferedReader;
import java.io.IOException;

/**
 * The TCP listener for the client
 */
public class ClientListener implements Runnable {

    private Client client;
    private BufferedReader inputFromServer;
    private AES aes;

    public ClientListener(Client client, BufferedReader clientSocket, AES aes) throws IOException {
        this.client = client;
        this.inputFromServer = clientSocket;
        this.aes = aes;
    }

    private boolean checkAuthentication() {
        //Receive CONNECTED message
        String decodedString = null;
        try {
            decodedString = aes.decrypt(inputFromServer.readLine());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return MessageFactory.decode(decodedString).messageType() == Message.MessageType.CONNECTED;
    }

    private void startListener() throws IOException {
        //Read until the client disconnects
        String line = "";
        while (line != null) {
            line = inputFromServer.readLine();
            String decodedString = aes.decrypt(line);
            client.parseResponse(MessageFactory.decode(decodedString));
        }
    }

    @Override
    public void run() {
        boolean authenticated = checkAuthentication();

        // Start reading if authenticated
        if (authenticated) {
            try {
                startListener();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
