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
	 private final String userID;
	 private final String secretKey;
	 private final String serverIp;
	 private AES aes;

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
		EncodedMessage encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.HELLO, userID, this.userID); // get clientid from database

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
		Socket clientSocket = null;
		PrintWriter output = null;
		BufferedReader input = null;
		Scanner scanner = new Scanner(System.in);

		//Connect to the server on the designated port
		try
		{
			// String to read message from input
			String line = "";
			boolean inChat = false;
			boolean connected = false;

			// keep reading until "Log off" is input
			while(true) {
				try {
					line = scanner.nextLine();

					//Wait for the user to type "Log on"
					if(!connected) {
						if(line.equals("Log on")) {
							//Go through the UDP authentication session
							if(authenticate()) {

								//Go through the TCP portion of the authentication
								clientSocket = new Socket(InetAddress.getByName(serverIp), sessionPortNumber);

								output = new PrintWriter(clientSocket.getOutputStream(), true);
								input = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

								//Send CONNECT message
								EncodedMessage connectedMessage = MessageFactory.encode(MessageType.CONNECT, Integer.toString(randCookie));
								output.println(aes.encrypt(connectedMessage.message()));
								//Receive CONNECTED message
								String decodedString = aes.decrypt(input.readLine());
								if(MessageFactory.decode(decodedString).messageType() != MessageType.CONNECTED) {
									return;
								}
								connected = true;
								System.out.println("Connected");
								clientSocket.setSoTimeout(60000);
								continue;
							}
						}
					}

					//Check if this is a chat request
					if (!inChat) {
						if(line.contains("Chat")) {
							StringTokenizer tokenizer = new StringTokenizer(line);
							if (tokenizer.nextToken().contains("Chat")) {
								String clientId = tokenizer.nextToken();
								EncodedMessage message = MessageFactory.encode(MessageType.CHAT_REQUEST, clientId, "");
								output.println(aes.encrypt(message.message()));
								DecodedMessage chatResp = MessageFactory.decode(aes.decrypt(input.readLine()));
								if (chatResp.messageType() == MessageType.UNREACHABLE) {
									System.out.println("Correspondent unreachable");
								} else {
									inChat = true;
									System.out.println("Chat started");
								}
							}
						}
					}

					if(inChat) {
						if(line.equals("End chat")) {
							EncodedMessage message = MessageFactory.encode(MessageType.END_REQUEST, "");
							output.println(aes.encrypt(message.message()));
							System.out.println("Chat ended");
							break;
						}
						//Send chat message to server
						else {
							EncodedMessage message = MessageFactory.encode(MessageType.CHAT, line);
							output.println(aes.encrypt(message.message()));
						}
					}

					if(line.contains("History")) {
						StringTokenizer tokenizer = new StringTokenizer(line);
						if(tokenizer.nextElement().equals("History")) {
							EncodedMessage message = MessageFactory.encode(MessageType.HISTORY_REQ, tokenizer.nextToken(), "");
							output.println(aes.encrypt(message.message()));
							DecodedMessage historyResp = MessageFactory.decode(aes.decrypt(input.readLine()));
							if(historyResp.messageType() != MessageType.HISTORY_RESP) {
								break;
							}
							else {
								System.out.println("History response");
							}
						}
					}

					if(line.equals("Log off")) {
						break;
					}
				}
				catch(SocketException e) {
					System.out.println("Socket Timeout. Logging off...");
				}
			}
		}
		catch(UnknownHostException u) {
			System.out.println(u);
		}
		catch(IOException i) {
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

