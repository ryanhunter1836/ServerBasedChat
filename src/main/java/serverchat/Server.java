package main.java.serverchat;

import java.util.*;
import java.net.*;
import java.util.concurrent.*;
import java.io.*;

//Code is based off the documentation: https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html

public class Server
{
    int portNumber;
    private boolean listening = false;
    private ServerSocket serverSocket = null;

    private final ExecutorService threadPool;

    public Server(int port)
    {
        portNumber = port;

        //Create the thread pool
        threadPool = Executors.newFixedThreadPool(10);
    }
    
    public void helloWait() throws Exception{
    	DatagramSocket ds = new DatagramSocket(portNumber);
    	
    	byte[] receive = new byte[1000];
    	DatagramPacket helloReceive = null;
		DatagramPacket sendChallenge=null;
		
    	System.out.println("port number: "+ portNumber);
		
    	while(true) {
    		helloReceive = new DatagramPacket(receive, receive.length);
    		
    		ds.receive(helloReceive);
    		
    		 String dataString =  new String(helloReceive.getData(), 0, helloReceive.getLength());
    		 System.out.println(dataString);
    			
    		if(dataString.equals("hello")) {
    			System.out.println("received hello");  
    			
    		   	//CHALLENGE
    			int rand = (int)(Math.random()*100); //generates a random number to confirm    			
    			receive = (rand+"").getBytes();
    			sendChallenge = new DatagramPacket(receive, receive.length);
    			sendChallenge.setAddress(helloReceive.getAddress());
    			sendChallenge.setPort(helloReceive.getPort());
    			System.out.println("Sending CHALLENGE");
    			ds.send(sendChallenge);
    			
    			//Receiving RESPONSE
    			helloReceive = new DatagramPacket(receive, receive.length);
        		ds.receive(helloReceive);
        		dataString =  new String(helloReceive.getData(), 0, helloReceive.getLength());
        		
        		if(dataString.equals(rand+"")) {
    				System.out.println("AUTH_SUCCESS: Authorization process completed");
    				receive = ("AUTH_SUCC").getBytes();
    				sendChallenge = new DatagramPacket(receive, receive.length);
        			sendChallenge.setAddress(helloReceive.getAddress());
        			sendChallenge.setPort(helloReceive.getPort());
        			
        			ds.send(sendChallenge); //Send AUTH_SUCC to Client
    				start(); //START TCP connection
    				
    			}else {
    				System.out.println("AUTH_FAIL: Authorization process failed");
    				break;
    			}
    		
    		
    		}
    	}
    }
    
    //Call the start method to start the server
    public void start() throws Exception
    {
        listening = true;
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Started");

            //Welcoming socket that accepts connections from a client then spins off into separate thread
            while(listening)
            {
                Socket socket = serverSocket.accept();
                DataInputStream inServer = new DataInputStream(socket.getInputStream());
                DataOutputStream outServer = new DataOutputStream(socket.getOutputStream());
        
              
                //CONNECTED
                System.out.println("CONNECTED: Received connection from client");
                threadPool.execute(new ClientConnection(socket));
            }
        }
        catch(IOException e)
        {
            System.out.println(e.toString());
        }
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
