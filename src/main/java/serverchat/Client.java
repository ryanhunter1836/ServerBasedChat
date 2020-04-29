package main.java.serverchat;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client implements Message
{
	//Port number of authentication server
	 int portNumber;
	 int sessionPortNumber;

	public Client(int port)
	{
		portNumber = port;
		sessionPortNumber = -1;
	}
	
	public void startClient() throws IOException
	{
		/*
		//NOTE: CLIENT ID IS NOT OPTIONAL, IT IS PRESET
		System.out.print("What is your user name/Client ID? ");
        String userID = input.nextLine();
        System.out.println("Your userID is: "+userID);

		 */

		String userID = "test"; // need to figure out a way to get access to database from server
    	boolean authenticated;
    	//Need to delay the client thread for a second to give the server time to boot up
    	try
		{
			Thread.sleep(1000);
		}
    	catch(InterruptedException e) {}

    	authenticated = authenticate(userID);
    	if(authenticated)
		{
			//Connect to the TPC socket for the chat session
			startChatSession();
		}
	}

	private boolean authenticate(String clientId) throws IOException
	{
		DatagramSocket ds = new DatagramSocket();
		InetAddress serverIp = InetAddress.getLocalHost();
		byte buffer[] = new byte[Message.PacketLength];

		//Create an authentication request
		EncodedMessage encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.HELLO, clientId, "_"); // get clientid from database

		//And send to the server
		DatagramPacket serverDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length, serverIp, portNumber);
		ds.send(serverDatagram);

		//Set up the datagram to receive data
		serverDatagram = new DatagramPacket(buffer, buffer.length);
		ds.receive(serverDatagram);

		DecodedMessage decodedMessage = ((DecodedMessage) MessageFactory.decode(serverDatagram));
		//Check to make sure it is a challenge message
		if(decodedMessage.messageType() != MessageType.CHALLENGE)
		{
			//This is a pretty serious problem because the server is now in a weird state.
			//Need to implement some sort of auth timeout on the server
			return false;
		}

		int randNum = Integer.parseInt(decodedMessage.message());
		
		// Get XRES using random num and private key 
		// String XRES = SecretKeyGenerator.hash1(randNum+""); // private key from database given clientID
		//Run the hashing algorithm to get a response
		String RES = SecretKeyGenerator.hash1(randNum+""); // get user input to verify

		encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.RESPONSE, clientId, RES);

		//Send the challenge back to the client
		DatagramPacket respDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length, serverIp, portNumber);
		ds.send(respDatagram);

		//Reset the buffer and datagram
		buffer = new byte[Message.PacketLength];
		serverDatagram = new DatagramPacket(buffer, buffer.length);
		ds.receive(serverDatagram);

		decodedMessage = (DecodedMessage)MessageFactory.decode(serverDatagram);
		if(decodedMessage.messageType() == MessageType.AUTH_SUCCESS)
		{
			System.out.println("Authentication success");
			sessionPortNumber = Integer.parseInt(decodedMessage.message());
			return true;
		}
		else
		{
			System.out.println("Authentication failure");
			return false;
		}
	}

	private void startChatSession ()
	{
		Socket clientSocket = null;
		PrintWriter output = null;
		BufferedReader input = null;
		Scanner scanner = null;

		//Connect to the server on the designated port
		try
		{
			clientSocket = new Socket("localhost", sessionPortNumber);

			//Read input from the console
			scanner = new Scanner(System.in);

			//Input and output streams for the socket
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
		}
		catch(UnknownHostException u)
		{
			System.out.println(u);
		}
		catch(IOException i)
		{
			System.out.println(i);
		}

		// string to read message from input
		String line = "";
		// keep reading until "Over" is input
		while (!line.equals("Log off"))
		{
			try
			{
				line = scanner.nextLine();
				output.println(line);
				//Receive an echo from the server
				String response = "";
				if((response = input.readLine()) == null)
				{
					//Socket is closed
					break;
				}
				System.out.println(response);
			}
			catch(IOException i)
			{
				System.out.println(i);
			}
		}

		// close the connection
		try
		{
			input.close();
			output.close();
			clientSocket.close();
		}
		catch(IOException i)
		{
			System.out.println(i);
		}
	}
}

