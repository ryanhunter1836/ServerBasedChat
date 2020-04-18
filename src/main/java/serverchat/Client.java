package main.java.serverchat;

import java.net.*;
import java.io.*;

public class Client {
	public static void main(String args[]) throws Exception{
  
		//Initiate a client socket that ClientConnection can attempt to connect
		Socket clientSocket = new Socket("localhost", 80); //host and port number subject to change
		
		//New object ClientConnection that takes clientSocket
		ClientConnection client1 = new ClientConnection(clientSocket);
		client1.run(); //runs input and output
	
  clientSocket.close(); //terminate the connection
	}
}
