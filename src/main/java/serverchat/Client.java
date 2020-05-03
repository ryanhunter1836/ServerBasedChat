package main.java.serverchat;

import java.net.*;
import java.io.*;
import java.util.*;

/**
 * Client
 * A class that establishes, authenticates, and connects clients
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class Client implements Message
{
	//Port number of authentication server
	private int portNumber;
	private int sessionPortNumber;
	private int randCookie;
	private boolean inChat;
	private String sessionID;

	private final String userID;
	private final String secretKey;
	private final String serverIp;
	private AES aes;

	private volatile Socket clientSocket;
	private volatile BufferedReader inputFromServer;
	private PrintWriter outputToServer;
	private Thread chatListener;
	private Thread chatInterface;


	/**
	 * Client constructor
	 * @param port
	 * @param keyfilePath
	 * @param serverIp
	 * @throws FileNotFoundException
	 */
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

	/**
	 * Starts client authentication process
	 * @throws IOException
	 */
	public void startClient() throws IOException
	{
    	boolean authenticated = authenticate(userID);
    	if(authenticated)
		{
			//Connect to the TPC socket for the chat session
			startChatSession();
		}
	}

	/**
	 * Authenticates client with the server
	 * @param clientId
	 * @return
	 * @throws IOException
	 */
	private boolean authenticate(String clientId) throws IOException
	{
		DatagramSocket ds = new DatagramSocket();
		InetAddress serverIp = InetAddress.getLocalHost();
		byte buffer[] = new byte[Message.PacketLength];

		//Create an authentication request
		EncodedMessage encodedMessage = MessageFactory.encode(MessageType.HELLO, clientId, userID); // get clientid from database

		//And send to the server
		DatagramPacket serverDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length, serverIp, portNumber);
		ds.send(serverDatagram);

		//Set up the datagram to receive data
		serverDatagram = new DatagramPacket(buffer, buffer.length);
		ds.receive(serverDatagram);

		DecodedMessage decodedMessage = MessageFactory.decode(serverDatagram);

		//Check to make sure it is a challenge message
		if(decodedMessage.messageType() != MessageType.CHALLENGE)
		{
			return false;
		}

		// Get random number from challenge message
		int randNum = Integer.parseInt(decodedMessage.message());

		// Get RES using random num and private key
		String RES = SecretKeyGenerator.hash1(randNum+secretKey); // get user input to verify
		//Also generate the encryption key
		String encryptionKey = SecretKeyGenerator.hash2(randNum+secretKey);
		aes = new AES(encryptionKey);

		encodedMessage = MessageFactory.encode(MessageType.RESPONSE, userID, RES);

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

	/**
	 * Starts chat session once clients are authenticated
	 */
	private void startChatSession ()
	{
		//Connect to the server on the designated port
		try
		{
			clientSocket = new Socket(InetAddress.getByName(serverIp), sessionPortNumber);

			// Inputs/outputs from/to the TCP connection
			outputToServer = new PrintWriter(clientSocket.getOutputStream(), true);

			//Send CONNECT message
			EncodedMessage connectedMessage = MessageFactory.encode(MessageType.CONNECT, Integer.toString(randCookie));
			outputToServer.println(aes.encrypt(connectedMessage.message()));
			inputFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

			// Start a new chat listener thread
			chatListener = new Thread(new ClientListener(this, inputFromServer, aes));
			chatListener.start();

			// Start new chat interface thread
			chatInterface = new Thread(new ClientInterface(this));
			chatInterface.start();

		} catch(IOException e) {
			System.out.println(e);
		}
	}

	/**
	 * Parses the response from the server
	 * @param response The response to parse
	 */
	public void parseResponse(DecodedMessage response) {
		System.out.println("Parse response " + response.messageType());
		switch (response.messageType()) {
			case CHAT_STARTED:
				System.out.println("A chat has started with " + response.getField("ClientID"));
				inChat = true;
				sessionID = response.getField("SessionID");
				break;

			case UNREACHABLE:
				System.out.println(response.getField("ClientID") + " is currently not reachable.");
				break;

			case END_NOTIF:
				System.out.println("The chat has ended");
				inChat = false;
				sessionID = "";
				break;

			case CHAT:
			case HISTORY_RESP:
				System.out.println(response.getField("Message"));
				break;

		}
	}

	/**
	 * Sends a chat request to the server
	 * @param clientID The ID of the client to chat to
	 */
	private void sendChatRequest(String clientID) {
		HashMap<String, String> chatRequest = new HashMap<>();
		chatRequest.put("MessageType", Integer.toString(MessageType.CHAT_REQUEST.ordinal()));
		chatRequest.put("ClientID", clientID);
		EncodedMessage encodedMessage = MessageFactory.encode(chatRequest);
		outputToServer.println(aes.encrypt(encodedMessage.message()));
	}

	/**
	 * Sends an end request to the server
	 */
	private void endChat() {
		HashMap<String, String> endRequest = new HashMap<>();
		endRequest.put("MessageType", Integer.toString(MessageType.END_REQUEST.ordinal()));
		endRequest.put("SessionID", sessionID);
		EncodedMessage encodedMessage = MessageFactory.encode(endRequest);
		outputToServer.println(aes.encrypt(encodedMessage.message()));
		inChat = false;
		sessionID = "";
	}

	/**
	 * Sends a chat message to the server
	 * @param message The message to send
	 */
	private void sendChat(String message) {
		HashMap<String, String> chatMessage = new HashMap<>();
		chatMessage.put("MessageType", Integer.toString(MessageType.CHAT.ordinal()));
		chatMessage.put("SessionID", sessionID);
		chatMessage.put("Message", message);
		EncodedMessage encodedMessage = MessageFactory.encode(chatMessage);
		outputToServer.println(aes.encrypt(encodedMessage.message()));
	}

	/**
	 * Sends a request to the server for the chat history
	 * @param clientID The ID of the other client to get history for
	 */
	private void getChatHistory(String clientID) {
		HashMap<String, String> historyMessage = new HashMap<>();
		historyMessage.put("MessageType", Integer.toString(MessageType.HISTORY_REQ.ordinal()));
		historyMessage.put("ClientID", clientID);
		EncodedMessage encodedMessage = MessageFactory.encode(historyMessage);
		outputToServer.println(aes.encrypt(encodedMessage.message()));
	}

	/**
	 * Parses the input from the user and sends a crafted message accordingly
	 * @param request The request
	 * @throws IOException
	 */
	public void parseRequest(String request) throws IOException {
		// Check if this is a chat request
		if (!inChat)
		{
			StringTokenizer tokenizer = new StringTokenizer(request);
			String token = tokenizer.nextToken();
			// The user wants to start a chat
			if (token.contains("Chat"))
			{
				String clientId = tokenizer.nextToken();
				sendChatRequest(clientId);
			}
			// The user wants to obtain the chat history
			else if (token.contains("History"))
			{
				String clientId = tokenizer.nextToken();
				getChatHistory(clientId);
			}
			// If the user is to log off
			else if (request.equals("Log off"))
			{
				sendChat(request);
			}
			// Otherwise, it's invalid
		}
		// In chat logic
		else {
			// End of chat
			if (request.equals("End chat")) {
				endChat();
			}
			// User wants to log off
			else if (request.equals("Log off")) {
				endChat();
			}
			// Otherwise, it's a chat
			else {
				sendChat(request);
			}

		}
	}

	/**
	 * Sets the timeout of the socket
	 * @param length The length of time before the timeout expired
	 */
	public void setSocketTimeout(int length)
	{
		try
		{
			clientSocket.setSoTimeout(length);
		} catch (SocketException e)
		{
			e.printStackTrace();
		}
	}

	/**
	 * Closes the TCP socket and respective readers/writers.
	 */
	public void closeTCPSocket()
	{
		try
		{
			chatListener.stop();
			chatInterface.stop();
			inputFromServer.close();
			outputToServer.close();
			clientSocket.close();
		}
		catch(IOException i)
		{
			System.out.println(i);
		}
	}
}

