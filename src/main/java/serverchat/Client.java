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
	 private final String serverIp;
	 private AES aes;

	public Client(int port, String keyfilePath, String serverIp) throws FileNotFoundException
	{
		portNumber = port;
		sessionPortNumber = -1;
		this.serverIp = serverIp;

		//Load the keyfile
		File file = new File(keyfilePath);
		Scanner scanner = new Scanner(file);
		userID = scanner.nextLine();
		secretKey = scanner.nextLine();
	}
	
	public void startClient() throws IOException
	{
    	boolean authenticated = authenticate(userID);
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
			return false;
		}

		int randNum = Integer.parseInt(decodedMessage.message());
		
		// Get XRES using random num and private key
		String RES = SecretKeyGenerator.hash1(randNum+secretKey); // get user input to verify
		//Also generate the encryption key
		String encryptionKey = SecretKeyGenerator.hash2(randNum+secretKey);
		aes = new AES(encryptionKey);

		encodedMessage = MessageFactory.encode(MessageType.RESPONSE, clientId, RES);

		//Send the challenge back to the client
		DatagramPacket respDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length, serverIp, portNumber);
		ds.send(respDatagram);

		//Reset the buffer and datagram
		buffer = new byte[Message.PacketLength];
		serverDatagram = new DatagramPacket(buffer, buffer.length);
		ds.receive(serverDatagram);

		//Get a nonstandard decoded message.  This message is also encoded
		String decodedString = aes.decrypt(new String(serverDatagram.getData(), 0, serverDatagram.getLength()));
		decodedMessage = MessageFactory.getDecodedMessageObj(decodedString);

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

			clientSocket = new Socket(InetAddress.getByName(serverIp), sessionPortNumber);

			//Read input from the console
			scanner = new Scanner(System.in);
			output = new PrintWriter(clientSocket.getOutputStream(), true);
			input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));


			//Send CONNECT message
			EncodedMessage connectedMessage = MessageFactory.encode(MessageType.CONNECT, Integer.toString(randCookie));
			output.println(aes.encrypt(connectedMessage.message()));
			//Receive CONNECTED message
			String decodedString = aes.decrypt(input.readLine());
			if(MessageFactory.decode(decodedString).messageType() != MessageType.CONNECTED)
			{
				return;
			}

			// String to read message from input
			String line = "";
			boolean inChat = false;

			// keep reading until "Log off" is input
			while (!line.equals("Log off"))
			{
				line = scanner.nextLine();

				//Check if this is a chat request
				if(!inChat)
				{
					StringTokenizer tokenizer = new StringTokenizer(line);
					if(tokenizer.nextToken().contains("Chat")) {
						String clientId = tokenizer.nextToken();
						EncodedMessage message = MessageFactory.encode(MessageType.CHAT_REQUEST, clientId);
						output.println(aes.encrypt(message.message()));

						String test = input.readLine();
						test = aes.decrypt(test);

						DecodedMessage chatResp = MessageFactory.decode(test);
						if(chatResp.messageType() == MessageType.UNREACHABLE) {
							System.out.println("Client is currently not online");
						}
						else {
							inChat = true;
						}
					}
				}
			}
		}
		catch(UnknownHostException u)
		{
			System.out.println(u);
		}
		catch(IOException i)
		{
			System.out.println(i);
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

