package main.java.serverchat;

import main.java.serverchat.database.Database;

import java.net.*;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.*;

import java.io.*;

/**
 * Server
 * A class that contains a server that runs our chat service
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class Server implements Message
{
    int portNumber;
    private boolean listening = false;
    private DatagramSocket ds;
    private Random random;
    private static Hashtable<String, ServerToClientConnectionInstance> connectedClients;
    private Database database;

    private final ExecutorService threadPool;

    /**
     * Server constructor
     * @param port
     */
    public Server(int port)
    {
        portNumber = port;
        //Create the thread pool
        threadPool = Executors.newFixedThreadPool(10);
        database = new Database();
        random = new Random();
        connectedClients = new Hashtable<>();
    }

    /**
     * Entry point for the UDP authentication server
     * @throws IOException
     * @throws UnknownHostException
     */
    public void start() throws IOException, UnknownHostException
    {
        System.out.println("IP Address of Server: " + InetAddress.getLocalHost());
        listening = true;
        ds = new DatagramSocket(portNumber);

        //Welcoming socket that accepts connections from a client then spins off into separate thread for chat session
        while(listening)
        {
            //Start listening for incoming authentication requests
            byte[] messageBuffer = new byte[Message.PacketLength];

            //UDP packet received from a client
            DatagramPacket clientDatagram = new DatagramPacket(messageBuffer, messageBuffer.length);
            ds.receive(clientDatagram);

            //Decode the message
            DecodedMessage message = MessageFactory.decode(clientDatagram);
            //Make sure the message is a hello message
            if(message.messageType() != MessageType.HELLO)
            {
                System.out.println("No client currently in authentication process");
                //Just start the AUTH process over again
                continue;
            }

            String XRES;

            //Generate the challenge
            int rand = (random.nextInt()*10000) + 10000; //generates a random number to confirm
            try
            {
                XRES = generateAndSendChallenge(clientDatagram, message, rand);
            }
            catch(IOException e)
            {
                System.out.println("Error sending packet");
                sendAuthResult(clientDatagram, false, -1);
                continue;
            }

            // If XRES could not be generated because client doesn't exist, return AUTH_FAIL and continue
            if (XRES.equals(""))
            {
                sendAuthResult(clientDatagram, false, -1);
                continue;
            }

            //Reset the buffer and datagram
            messageBuffer = new byte[Message.PacketLength];
            clientDatagram = new DatagramPacket(messageBuffer, messageBuffer.length);

            //Wait for the client to come back with a response
            ds.receive(clientDatagram);
            message = MessageFactory.decode(clientDatagram);
            if(message.messageType() != MessageType.RESPONSE)
            {
                System.out.println("Another client is currently in the connection phase");
                sendAuthResult(clientDatagram, false, -1);
            }

            String RES = message.message();
            
            // Compare the client response to server response
            if(RES.equals(XRES))
            {
                try {
                    sendAuthResult(clientDatagram, true, rand);
                }
                catch(IOException e) {
                    System.out.println("Error sending packet");
                    continue;
                }
            }
            //Client is not authenticated
            else
            {
                try {
                    sendAuthResult(clientDatagram, false, -1);
                }
                catch(IOException e) {
                    System.out.println("Error sending packet");
                    continue;
                }
            }
        }

        stop();
    }

    /**
     * Generates the challenge that is used in 2.2
     * @param datagram The datagram received from the client
     * @param message The decoded message from the datagram
     * @return A string with the expected result (XRES)
     * @throws IOException
     */
    private String generateAndSendChallenge(DatagramPacket datagram, DecodedMessage message, int rand) throws IOException
    {
        // Obtain the client's private key from the database
        String clientPrivateKey;
        try
        {
            clientPrivateKey = database.getClient(message.clientId()).getString("privateKey");
        } catch (Exception e)
        {
            System.out.println("Unable to obtain private key for client: " + message.clientId());
            return "";
        }

        //Generate the hash
        String XRES = SecretKeyGenerator.hash1(rand+clientPrivateKey);

        //Encode the random string as a challenge message
        EncodedMessage encodedMessage = MessageFactory.encode(MessageType.CHALLENGE, Integer.toString(rand));

        //Send the challenge back to the client
        DatagramPacket challengeDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length);
        challengeDatagram.setAddress(datagram.getAddress());
        challengeDatagram.setPort(datagram.getPort());
        ds.send(challengeDatagram);
        return XRES;
    }

    /**
     * Sends the authentication results to the clients for verification
     * @param datagram
     * @param authSuccessful
     * @param rand
     * @throws IOException
     */
    private void sendAuthResult(DatagramPacket datagram, boolean authSuccessful, int rand) throws IOException {
        DatagramPacket authDatagram;
        EncodedMessage message;
        int clientPortNumber = -1;
        byte[] encryptedMessage = null;

        // Craft a return message and start the TCP listener
        if(authSuccessful)
        {
            //Get successful auth encoded message
            //Client port number will be between 5000 - 6000
            clientPortNumber = random.nextInt(1000) + 5000;

            // Generate a random cookie and respective encryption key
            // The cookie will be between 10,000 - 20,000
            int cookie = (random.nextInt()*10000) + 10000;

            DecodedMessage clientMessage = (DecodedMessage)MessageFactory.decode(datagram);
            String clientPrivateKey = database.getClient(clientMessage.clientId()).getString("privateKey");
            String encryptionKey = SecretKeyGenerator.hash2(rand+clientPrivateKey);
            database.setClientEncryptionKey(clientMessage.clientId(), encryptionKey);

            //Start a thread to wait for a connection from the client over TCP
            ServerToClientConnectionInstance task = new ServerToClientConnectionInstance(
                    this, clientPortNumber, clientMessage.clientId(), encryptionKey);

            //Add the task to the list of connected clients for easy access later
            connectedClients.put(clientMessage.clientId(), task);

            threadPool.execute(task);

            // Craft message
            HashMap<String, String> messageMap = new HashMap<>();
            messageMap.put("MessageType", Integer.toString(MessageType.AUTH_SUCCESS.ordinal()));
            messageMap.put("RandCookie", Integer.toString(cookie));
            messageMap.put("PortNumber", Integer.toString(clientPortNumber));
            message = MessageFactory.encode(messageMap);

            //Encrypt the message
            encryptedMessage = new AES(encryptionKey).encrypt(message.message()).getBytes();
        }
        else
        {
            message = (EncodedMessage)MessageFactory.encode(MessageType.AUTH_FAIL, "");
        }

        //Send the result to the client
        authDatagram = new DatagramPacket(encryptedMessage == null ? message.encodedMessage() : encryptedMessage,
                encryptedMessage == null ? message.encodedMessage().length : encryptedMessage.length);
        authDatagram.setAddress(datagram.getAddress());
        authDatagram.setPort(datagram.getPort());
        ds.send(authDatagram);
    }

    /**
     * Returns the routing information for the request client
     * @param clientId
     * @return
     */
    private ServerToClientConnectionInstance getClientTask(String clientId)
    {
        return connectedClients.get(clientId);
    }

    /**
     * Sends a message to a specific client
     * @param message The message to send
     * @param clientID The ID of the client to send to
     */
    public void sendMessageToClient(EncodedMessage message, String clientID) {
        ServerToClientConnectionInstance client = getClientTask(clientID);
        System.out.println("Send message to client " + clientID);
        System.out.println("Send message to client " + message.message() + "\n");
        client.receiveMessage(message);
    }

    /**
     * Method called when a connection terminates
     * @param clientId
     */
    public void disconnect(String clientId)
    {
        connectedClients.remove(clientId);
    }

    /**
     * Call the stop method to gracefully shutdown the server
     */
    public void stop()
    {
        shutdownAndAwaitTermination(threadPool);
    }

    /**
     * Dispose of the thread pool and close any lingering connections
     * @param pool
     */
    private void shutdownAndAwaitTermination(ExecutorService pool)
    {
        pool.shutdown(); // Disable new tasks from being submitted
        try
        {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS))
            {
                pool.shutdownNow();
                // Cancel currently executing tasks

                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                {
                    System.err.println("Pool did not terminate");
                }
            }
        }
        catch (InterruptedException ie)
        {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}
