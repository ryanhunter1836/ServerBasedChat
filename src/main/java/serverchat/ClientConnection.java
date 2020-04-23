package main.java.serverchat;

import java.util.*;
import java.io.*;
import java.net.*;

//Instance of a connection between server and client
public class ClientConnection implements Runnable, Message
{
    private Socket socket;
    public String userName;
    
    public ClientConnection(Socket socket, String clientID)
    {
        this.socket = socket;
        this.userName = clientID;
    }

    public String getUserName() {
    	return this.userName;
    }

    @Override
    //Required for the runnable interface
    public void run()
    {
        try
        {	
            //Reading client input
        	BufferedReader inputBuff = new BufferedReader(new InputStreamReader(System.in));
        	        	
        	//Reading from the server
        	BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        	
        	//writing to the server
        	PrintWriter outWrite = new PrintWriter(socket.getOutputStream(), true);
            
        	String inputLine = "";
            String outputLine = "";
                        
            while (true)
            {
            	inputLine =inputBuff.readLine();
            	outputLine = getUserName()+": "+ inputLine + "\r";
                 
            	outWrite.println(outputLine);
            	outWrite.flush();
            	
            	//fromServer.readLine();
            	
            	System.out.println(fromServer.readLine());
            	
                if (inputLine.contains("Log off"))
                {
                    break;
                }
                //Exit if the server is stopped and interrupts the thread
                if(Thread.interrupted())
                {
                	System.out.println("Connection was interrupted!");
                    break;
                }
            }
            System.out.println("Closing connection");
            socket.close();
            
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
    
    public void chat(BufferedReader in, PrintWriter out, String recipient){
    	boolean inChat = true;
    	System.out.println("You are now in a chat with "+recipient);
    	while(inChat) {
    		
    		 String outputLine =getUserName()+": "+ in + "\r";
             

             out.println(outputLine);
    		
    	}
    	
    	
    }
    
    
    
}
