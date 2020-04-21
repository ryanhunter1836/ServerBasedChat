import java.net.*;
import java.io.*;


public class Client {
	 int portNumber;
	 
	public Client(int port) {
		portNumber = port;
		
	}
	public void startClient() throws Exception{
		//Initiate a client socket that ClientConnection can attempt
		Socket clientSocket = new Socket("localhost", portNumber); //host and port number subject to change
		
		//New object ClientConnection that takes clientSocket
		ClientConnection client1 = new ClientConnection(clientSocket);
		client1.run(); //runs input and output
		clientSocket.close(); //terminate the connection
	}
}
