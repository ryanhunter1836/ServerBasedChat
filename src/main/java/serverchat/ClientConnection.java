package main.java.serverchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

//Instance of a connection between server and client
public class ClientConnection implements Runnable, Message
{
    private int portNumber;
    private Socket socket;

    public ClientConnection(int portNumber)
    {
        this.portNumber = portNumber;
    }

    //Required for the runnable interface
    public void run()
    {
        try
        {
            //Setup the listening socket
            ServerSocket listeningSocket = new ServerSocket(portNumber);
            //Accept the connection from the client
            socket = listeningSocket.accept();

            System.out.println("Received connection from client on port " + portNumber);
            //Set up input and output byte streams
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);

            String inputLine = "";
            String outputLine = "";

            while ((inputLine = inputStream.readLine()) != null)
            {
                outputLine = inputLine + "\r";

                //** EXAMPLE CODE
                Message encoded = MessageFactory.encode(MessageType.CHAT, outputLine);
                Message decoded = MessageFactory.decode(((EncodedMessage)encoded).encodedMessage());

                //USE THIS METHOD TO GET THE BYTES TO SEND THROUGH THE SOCKET
                // ((EncodedMessage)encoded).encodedMessage();

                outputStream.println(outputLine);
                if (inputLine.contains("Log off"))
                {
                    break;
                }
                //Exit if the server is stopped and interrupts the thread
                if(Thread.interrupted())
                {
                    break;
                }
            }
            System.out.println("Closing connection");
            socket.close();
            inputStream.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
