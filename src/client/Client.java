import java.net.*;
import java.io.*;

public class Client {
	
	public static void main(String argv[]) throws Exception{
		String textFromServer; //text from the server
		Socket clientSocket = new Socket("localhost", 80); //host and port number subject to change
		
		//Read Data from the Server
		InputStream clientInput = clientSocket.getInputStream();
		BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientInput));
		textFromServer= clientReader.readLine();
		System.out.println(textFromServer); //print out data from server
		
		//Send Data to server
		OutputStream output = clientSocket.getOutputStream();
		PrintWriter writer = new PrintWriter(output, true);
		writer.println("This will be written to the server."); //write message to 
		
		clientSocket.close(); //terminate the connection
		
	}
}
