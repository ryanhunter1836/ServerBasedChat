package main.java.serverchat;

import java.io.*;
import java.net.*;

//Instance of a connection between server and client
public class ServerToClientConnectionInstance implements Runnable, Message
{
    private int portNumber;
    private String userName;
    private boolean clientConnected;
    PrintWriter out = null;

    //Object to hold a connection between the server and the client
    public ServerToClientConnectionInstance(int portNumber, String clientID)
    {
        this.portNumber = portNumber;
        this.userName = clientID;
        clientConnected = false;
    }

    public String getUserName() {
    	return this.userName;
    }
    public boolean connected() { return clientConnected; }

    @Override
    //Required for the runnable interface
    public void run()
    {
        //Open a TCP socket on a unique port number and wait for the client to connect
        try
        {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket socket = serverSocket.accept();
            clientConnected = true;

            //Setup the input and output streams for the socket
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //This is just a test method that echoes input back to the client.
            // ADD CHAT SESSION LOGIC HERE
            String inputLine = "";
            //Read until the client disconnects
            while ((inputLine = in.readLine()) != null)
            {
                if(inputLine.contains("Log off"))
                {
                    break;
                }

                //Echo the data back to the client
                out.println(inputLine);   
            }
            in.close();
            out.close();
            socket.close();
            Server.disconnect(userName);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    //Method to send a message to this specific client
    public void sendMessage(EncodedMessage message)
    {
        //Guard against sending a message to a disconnected client
        if(clientConnected && out != null)
        {
            out.println(message.encodedMessage());
        }
    }

    //Implementation of chat sessions
    public void chat(BufferedReader in, PrintWriter out, String recipient)
    {
    	boolean isChatSessionActive = true;
    	System.out.println("You are now in a chat with " + recipient);
    	while(isChatSessionActive)
    	{
    		 String outputLine = getUserName()+": "+ in + "\r";
             out.println(outputLine);
    	}
    }
}
