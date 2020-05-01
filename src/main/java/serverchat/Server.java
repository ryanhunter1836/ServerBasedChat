package main.java.serverchat;

import main.java.serverchat.database.Database;

import java.net.*;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Random;
import java.util.concurrent.*;

import java.io.*;

public class Server implements Message
{
    int portNumber;
    private boolean listening = false;
    private DatagramSocket ds;
    private Random random;
    private static Dictionary connectedClients;
    private Database database;

    private final ExecutorService threadPool;

    public Server(int port)
    {
        portNumber = port;
        //Create the thread pool
        threadPool = Executors.newFixedThreadPool(10);
        database = new Database();
        random = new Random();
        connectedClients = new Hashtable();
    }

    //Entry point for the UDP authentication server
    public void start() throws IOException
    {
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
            DecodedMessage message = ((DecodedMessage)MessageFactory.decode(clientDatagram));
            //Make sure the message is a hello message
            if(message.messageType() != MessageType.HELLO)
            {
                System.out.println("No client currently in authentication process");
                //Just start the AUTH process over again
                continue;
            }

            String XRES;
            try
            {
                XRES = generateAndSendChallenge(clientDatagram, message);
            }
            catch(IOException e)
            {
                System.out.println("Error sending packet");
                sendAuthResult(clientDatagram, false);
                continue;
            }

            // If XRES could not be generated because client doesn't exist, return AUTH_FAIL and continue
            if (XRES.equals(""))
            {
                sendAuthResult(clientDatagram, false);
                continue;
            }

            //Reset the buffer and datagram
            messageBuffer = new byte[Message.PacketLength];
            clientDatagram = new DatagramPacket(messageBuffer, messageBuffer.length);

            //Wait for the client to come back with a response
            ds.receive(clientDatagram);
            message = ((DecodedMessage) MessageFactory.decode(clientDatagram));
            if(message.messageType() != MessageType.RESPONSE)
            {
                System.out.println("Another client is currently in the connection phase");
                //Ignore the request
                // Why is a request being ignored? At least send something back so the client knows to not wait anymore.
                // AUTH_FAIL is not ideal in this situation, but at least it's something.
                sendAuthResult(clientDatagram, false);
            }

            // Either needs to be the line above (if client just puts in key) or line below ifthe client has to run the algorithm themselves
            String RES = message.message();
            
            // Compare the client response to server response
            if(RES.equals(XRES))
            {
                try
                {
                    sendAuthResult(clientDatagram, true);
                }
                catch(IOException e)
                {
                    System.out.println("Error sending packet");
                    continue;
                }
            }
            //Client is not authenticated
            else
            {
                try
                {
                    sendAuthResult(clientDatagram, false);
                }
                catch(IOException e)
                {
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
    private String generateAndSendChallenge(DatagramPacket datagram, DecodedMessage message) throws IOException
    {
        //Generate the challenge
        int rand = (int)(Math.random()*100); //generates a random number to confirm

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
        
        //Generate Encryption Key and assign to client
        String CK = SecretKeyGenerator.hash2(rand+clientPrivateKey);
        database.setClientEncryptionKey(message.clientId(), CK);

        //Encode the random string as a challenge message
        EncodedMessage encodedMessage = (EncodedMessage)MessageFactory.encode(MessageType.CHALLENGE, Integer.toString(rand));

        //Send the challenge back to the client
        DatagramPacket challengeDatagram = new DatagramPacket(encodedMessage.encodedMessage(), encodedMessage.encodedMessage().length);
        challengeDatagram.setAddress(datagram.getAddress());
        challengeDatagram.setPort(datagram.getPort());
        ds.send(challengeDatagram);
        return XRES;
    }

    private void sendAuthResult(DatagramPacket datagram, boolean authSuccessful) throws IOException
    {
        DatagramPacket authDatagram;
        EncodedMessage message;
        int clientPortNumber = -1;

        if(authSuccessful)
        {
            //NEED TO GENERATE A RANDOM COOKIE TO SEND TO THE CLIENT

            //Get successful auth encoded message
            //Client port number will be between 5000 - 6000
            clientPortNumber = random.nextInt(1000) + 5000;
            message = (EncodedMessage)MessageFactory.encode(MessageType.AUTH_SUCCESS, Integer.toString(clientPortNumber));
        }
        else
        {
            message = (EncodedMessage)MessageFactory.encode(MessageType.AUTH_FAIL, "");
        }

        //Send the result to the client
        authDatagram = new DatagramPacket(message.encodedMessage(), message.encodedMessage().length);
        authDatagram.setAddress(datagram.getAddress());
        authDatagram.setPort(datagram.getPort());
        ds.send(authDatagram);

        //Start a new TCP listener is auth successful
        if(authSuccessful)
        {
            //Start a thread to wait for a connection from the client over TCP
            Runnable task = new ServerToClientConnectionInstance(clientPortNumber, "");

            //Add the task to the list of connected clients for easy access later
            //connectedClients.put(clientId, task);

            threadPool.execute(task);
        }
    }

    //Returns the routing information for the request client
    public Runnable getClientTask(String clientId)
    {
        return (Runnable)connectedClients.get(clientId);
    }

    //Method called when a connection terminates
    public static void disconnect(String clientId)
    {
        connectedClients.remove(clientId);
    }
    
    //Call the stop method to gracefully shutdown the server
    public void stop()
    {
        shutdownAndAwaitTermination(threadPool);
    }

    //Dispose of the thread pool and close any lingering connections.  Copied from documentation
    private void  shutdownAndAwaitTermination(ExecutorService pool)
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
