package main.java.serverchat;

import java.net.*;
import java.io.*;
import java.util.*;

public class Client implements Message
{
	//Port number of authentication server
	 private int portNumber;
	 private int sessionPortNumber;
	 private int randCookie;
	 private final String userID;
	 private final String secretKey;
	private AES aes;

	public Client(int port, String keyfilePath) throws FileNotFoundException
	{
		portNumber = port;
		sessionPortNumber = -1;

		//Load the keyfile
		File file = new File(keyfilePath);
		Scanner scanner = new Scanner(file);
		userID = scanner.nextLine();
		secretKey = scanner.nextLine();
	}
	
	public void startClient() throws IOException
	{
    	boolean authenticated = false;
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
		EncodedMessage encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.HELLO, clientId, userID); // get clientid from database

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
		String RES = SecretKeyGenerator.hash1(randNum+secretKey); // get user input to verify
		//Also generate the encryption key
		String encryptionKey = SecretKeyGenerator.hash2(randNum+secretKey);
		aes = new AES(encryptionKey);

		encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.RESPONSE, clientId, RES);

		//Send the challenge back to the client
		DatagramPacket respDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length, serverIp, portNumber);
		ds.send(respDatagram);

		//Reset the buffer and datagram
		buffer = new byte[Message.PacketLength];
		serverDatagram = new DatagramPacket(buffer, buffer.length);
		ds.receive(serverDatagram);

		//Get a nonstandard decoded message.  This message is also encoded

		String decodedString = aes.decrypt(new String(serverDatagram.getData(), serverDatagram.getLength()));
		decodedMessage = (DecodedMessage)MessageFactory.getDecodedMessageObj(decodedString);

		if(decodedMessage.messageType() == MessageType.AUTH_SUCCESS)
		{
			System.out.println("Authentication success");
			sessionPortNumber = Integer.parseInt(decodedMessage.getField("PortNumber"));
			randCookie = Integer.parseInt(decodedMessage.getField("RandCookie"));
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

		// String to read message from input
		String line = "";
		boolean inChat = false;

		// keep reading until "Log off" is input
		while (!line.equals("Log off"))
		{
		try {
			clientSocket.setSoTimeout(60000);
			line = scanner.nextLine();
			//Check if this is a chat request
			if(!inChat)
			{
				StringTokenizer tokenizer = new StringTokenizer(line);
				if(tokenizer.nextToken().contains("Chat")) {
					//Get the client ID
					String clientId = tokenizer.nextToken();
					EncodedMessage message = (EncodedMessage)MessageFactory.encode(MessageType.CHAT_REQUEST, clientId);
					//Send the message to the server
					output.println(message);
					//Wait for the chat response
					}
				}
				}catch(SocketException e) {
					System.out.println("Socket Timeout. Logging off...");
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

