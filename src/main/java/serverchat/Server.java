package main.java.serverchat;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.io.*;
import java.util.Hashtable;

//Code is based off the documentation: https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html
//UDP Server sample code: https://www.baeldung.com/udp-in-java

public class Server implements Message
{
    private String XRES;
    private boolean listening = false;
    //UDP socket used for authentication
    private DatagramSocket serverSocket = null;
    int portNumber;

    //Thread pool to hold all of the TCP client connections
    private final ExecutorService threadPool;

    //UDP authentication server
    public Server(int port)
    {
        portNumber = port;

        //Create the thread pool
        threadPool = Executors.newFixedThreadPool(10);
    }

    //Call the start method to start the server
    public void start() throws Exception
    {
        listening = true;
        try {
            serverSocket = new DatagramSocket(portNumber);
            System.out.println("Authentication Server Listening");

            //Welcoming socket that accepts connections from a client then spins off into separate thread
            while(listening)
            {
                byte[] buffer = new byte[Message.PacketLength];
                DatagramPacket packet = new DatagramPacket(buffer, Message.PacketLength);
                serverSocket.receive(packet);
                System.out.println("Received connection from client");

                //Decode the packet
                //DecodedMessage message = (DecodedMessage)MessageFactory.decode(packet.getData());

                //Get the message type, message, and destination routing details
                //Message.MessageType messageType = message.messageType();
                //String messageContent = message.message();

                int clientPortNum = packet.getPort();
                InetAddress clientAddress = packet.getAddress();

                //Go through the authentication with the client
                /*
                if(inServer.readUTF().equalsIgnoreCase("hello")) {
                	//CHALLENGE
                	if(!challenge(inServer, outServer))
                	{	
                		outServer.writeUTF("AUTH_FAIL");
                		break;
                	}else {
                		outServer.writeUTF("AUTH_SUCC");
                	}
                }
                */
               
                //If client authentication is successful, spawn a new thread for the TCP connection
                //NOTE: NEED TO REPLACE PORT NUMBER WITH A RANDOM PORT NUMBER
                //threadPool.execute(new ClientConnection(500));
            }
        }
        catch(IOException e)
        {
            System.out.println(e.toString());
        }
    }

    //Using Challenge
    public boolean challenge(DataInputStream inServer, DataOutputStream outServer) throws Exception {
    	int rand = (int)Math.random(); //generates a random number to confirm
    	String key = ""; // need to figure out how to generate or find key
    	
    	initialHash1(rand + key); // get XRES to be compared to user RES
    	String RES = hash1(rand + key); // get RES
    	outServer.writeUTF("Challenge key" + rand);
    	
    	if(inServer.readInt() == rand && XRES == RES) {
    		outServer.writeUTF("CONNECTED TO SERVER"); //CONNECTED
    		return true;
    	}else {
    		
    		return false;
    	}
    
    }
    
    // To be used to determine initial XRES to be compared later
    private void initialHash1(String key)
    {
    	XRES = hash1(key);
    }
    
    // A3: Hashing function from key
    private String hash1(String key)
    {	
    	// super basic implementation, will probably change to account for collision
    	MD5 m = new MD5();
    	String res = m.getMD5(key);
    	
    	return res;
    }
    
    
    //Call the stop method to gracefully shutdown the server
    public void stop()
    {
        shutdownAndAwaitTermination(threadPool);
    }

    //Dispose of the thread pool and close any lingering connections.  Copied from documentation
    private void  shutdownAndAwaitTermination(ExecutorService pool)
    {
        pool.shutdown(); // Disable new tasks from being submitted
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS))
            {
                pool.shutdownNow();
                // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie)
        {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
