package main.java.serverchat;
	
import java.net.*;
import java.io.*;
import java.util.*;

public class Client {
	 int portNumber;
	 
	public Client(int port) {
		portNumber = port;
		
	}
	
	public void startClient() throws Exception{
		//Initiate a client socket that ClientConnection can attempt
		Scanner input = new Scanner(System.in);
		
		System.out.print("What is your user name/Client ID? ");
        String userID = input.nextLine(); 
        
        System.out.println("Your userID is: "+userID);
        DatagramSocket clientUDP = new DatagramSocket();
       
        InetAddress clientIP = InetAddress.getLocalHost();
        byte buffer[] = null;
      
    	String inputLine;
    	
        while(true) {
        	inputLine = input.nextLine();
        	if(inputLine.contains("hello")) {	
        	//HELLO
        		buffer = inputLine.getBytes();
        		DatagramPacket clientHello = new DatagramPacket(buffer, buffer.length, clientIP, portNumber);
        		clientUDP.send(clientHello);
        		
        		//receives CHALLENGE
        		byte buffer2[] = new byte[1000];
        		DatagramPacket clientReceive = null;
        		clientReceive = new DatagramPacket(buffer2, buffer2.length);
        		clientUDP.receive(clientReceive);
           		String dataString =  new String(clientReceive.getData(), 0, clientReceive.getLength()) ;	
        		System.out.println("Challenge number: "+ dataString);
        		
        		//RESPONSE
        		buffer = input.nextLine().getBytes();
        		DatagramPacket clientResponse = new DatagramPacket(buffer, buffer.length, clientIP, portNumber);
        		clientUDP.send(clientResponse);
        		
        	}
        }
	}
	
        public void clientAccepted () throws Exception {
		
		Socket clientSocket = new Socket("localhost", portNumber); //host and port number subject to change
		
		//New object ClientConnection that takes clientSocket
		ClientConnection client1 = new ClientConnection(clientSocket);
		client1.run(); //runs input and output
		clientSocket.close(); //terminate the connection
	}
}
