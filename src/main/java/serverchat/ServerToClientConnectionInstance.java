package main.java.serverchat;

import main.java.serverchat.database.Database;
import org.bson.Document;

import java.io.*;
import java.net.*;
import java.util.HashMap;

//Instance of a connection between server and client
public class ServerToClientConnectionInstance implements Runnable, Message
{
    private int portNumber;
    private String userName;
    private String[] sessionInfo; // 0: sessionID, 1: the other client's ID
    private boolean clientConnected;
    private AES aes;
    private Server server;
    private Database database;
    PrintWriter out = null;

    //Object to hold a connection between the server and the client
    public ServerToClientConnectionInstance(Server server, int portNumber, String clientID, String key)
    {
        this.portNumber = portNumber;
        this.userName = clientID;
        this.sessionInfo = new String[]{"", ""};
        this.clientConnected = false;
        this.aes = new AES(key);
        this.server = server;
        this.database = new Database();
    }

    public String getUserName() {
    	return this.userName;
    }
    public boolean connected() { return clientConnected; }

    @Override
    //Required for the runnable interface
    public void run()
    {
        //Open a TCP socket on a unique port number and wait for the client to connect
        try
        {
            ServerSocket serverSocket = new ServerSocket(portNumber);
            Socket socket = serverSocket.accept();
            clientConnected = true;
            database.makeClientConnectable(userName);

            //Setup the input and output streams for the socket
            out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            //This is just a test method that echoes input back to the client.
            // ADD CHAT SESSION LOGIC HERE
            String inputLine = "";
            //Read until the client disconnects
            while ((inputLine = in.readLine()) != null)
            {
                String decryptedInput = aes.decrypt(inputLine);
                DecodedMessage message = MessageFactory.decode(decryptedInput);

                if (message.message().equals("Log off"))
                {
                    break;
                }

                switch(message.messageType())
                {
                    case CHAT_REQUEST:
                        startChatSession(message.getField("ClientID"));
                    case END_REQUEST:
                        endChatSession();
                    case CHAT:
                        sendChatMessage(message.getField("Message"));
                    case HISTORY_REQ:
                        sendChatHistory(message.getField("ClientID"));
                }

            }
            in.close();
            out.close();
            socket.close();
            server.disconnect(userName);
            database.makeClientUnconnectable(userName);
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }

    /**
     * Sends a message destined to this specific client
     * @param message The EncodedMessage to receive and send to the client
     */
    public void receiveMessage(EncodedMessage message)
    {
        //Guard against sending a message to a disconnected client
        if(clientConnected && out != null)
        {
            // Encrypt the message and send the message
            out.println(aes.encrypt(message.encodedMessage()));
        }
    }

    public void startChatSession(String clientID)
    {
        if (database.getClient(clientID).getBoolean("connectable"))
        {
            // Mark the clients in the database as connected to each other
            sessionInfo[0] = database.connectClients(userName, clientID);
            sessionInfo[1] = clientID;

            // Send a CHAT_STARTED response to both clients indicating a connection
            HashMap<String, String> startedMessage = new HashMap<>();
            startedMessage.put("MessageType", Integer.toString(MessageType.CHAT_STARTED.ordinal()));
            startedMessage.put("SessionID", this.sessionInfo[0]);
            startedMessage.put("ClientID", userName);
            EncodedMessage encodedMessage = MessageFactory.encode(startedMessage);

            // Send to the other client
            server.sendMessageToClient(encodedMessage, clientID);

            // Send to this client
            startedMessage.replace("ClientID", clientID);
            encodedMessage = MessageFactory.encode(startedMessage);
            receiveMessage(encodedMessage);
        }
        else
        {
            // Send an UNREACHABLE response to the client if the other client is not accessible
            HashMap<String, String> unreachableMessage = new HashMap<>();
            unreachableMessage.put("MessageType", Integer.toString(MessageType.UNREACHABLE.ordinal()));
            unreachableMessage.put("ClientID", userName);
            EncodedMessage encodedMessage = MessageFactory.encode(unreachableMessage);
            receiveMessage(encodedMessage);
        }
    }

    /**
     * Ends the chat session between this client and the other connected client
     */
    public void endChatSession() {
        // Create and send the message to the other client
        HashMap<String, String> disconnectMessage = new HashMap<>();
        disconnectMessage.put("MessageType", Integer.toString(MessageType.END_NOTIF.ordinal()));
        disconnectMessage.put("SessionID", this.sessionInfo[0]);
        EncodedMessage encodedMessage = MessageFactory.encode(disconnectMessage);
        server.sendMessageToClient(encodedMessage, sessionInfo[1]);

        // Disconnect the clients in the database
        database.disconnectClients(userName, sessionInfo[1]);
        sessionInfo[0] = "";
        sessionInfo[1] = "";
    }

    /**
     * Sends a message to the other connected client
     * @param message The message to send
     */
    private void sendChatMessage(String message)
    {
        HashMap<String, String> chatMessage = new HashMap<>();
        chatMessage.put("MessageType", Integer.toString(MessageType.CHAT.ordinal()));
        chatMessage.put("SessionID", this.sessionInfo[0]);
        chatMessage.put("ClientID", userName);
        chatMessage.put("Message", message);
        EncodedMessage encodedMessage = MessageFactory.encode(chatMessage);

        server.sendMessageToClient(encodedMessage, sessionInfo[1]);
        database.addChatHistory(this.sessionInfo[0], userName, message);
    }

    /**
     * Sends the chat history between this client and a different client to this client
     * @param clientID The ID of the different client
     */
    private void sendChatHistory(String clientID)
    {
        String sessionID = database.getSession(userName, clientID).get("_id").toString();
        for (Document history : database.getChatHistory(sessionID)) {

            String message = "<" + history.getString("sessionID") + "> " +
                    "from: " + history.getString("ClientID") + " " +
                    history.getString("message");

            HashMap<String, String> chatMessage = new HashMap<>();
            chatMessage.put("MessageType", Integer.toString(MessageType.HISTORY_RESP.ordinal()));
            chatMessage.put("ClientID", history.getString("ClientID"));
            chatMessage.put("Message", message);
            EncodedMessage encodedMessage = MessageFactory.encode(chatMessage);

            receiveMessage(encodedMessage);
        }
    }

    //Implementation of chat sessions
    public void chat(BufferedReader in, PrintWriter out, String recipient)
    {
    	boolean isChatSessionActive = true;
    	System.out.println("You are now in a chat with " + recipient);
    	while(isChatSessionActive)
    	{
    		 String outputLine = getUserName()+": "+ in + "\r";
             out.println(outputLine);
    	}
    }
}
