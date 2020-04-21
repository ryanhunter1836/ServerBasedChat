package main.java.serverchat;


import java.io.*;
import java.net.*;

//Instance of a connection between server and client
public class ClientConnection implements Runnable, Message
{
    private Socket socket;
    public String userName;
    
    public ClientConnection(Socket socket)
    {
        this.socket = socket;
    }

    public String getUserName() {
    	return this.userName;
    }

    //Required for the runnable interface
    public void run()
    {
        try
        {	
            //Set up input and output byte streams
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);

            String inputLine = "";
            String outputLine = "";
            
            System.out.print("What is your user name? ");
            this.userName = inputStream.readLine();
            
            while (true)
            {
                outputLine =getUserName()+": "+ inputLine + "\r";
                

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
