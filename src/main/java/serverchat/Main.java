package main.java.serverchat;

import java.io.FileNotFoundException;
import java.io.IOException;

/**
 * Main
 * A class that acts as the main driver for the project
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class Main
{
    /**
     * Driver function
     * @param args
     */
    public static void main(String[] args)
    {
        //Run as a server
        if(args[0].contains("server"))
        {
            if(args.length != 2)
            {
                System.out.println("Incorrect number of arguments supplied");
            }

            int portNumber = Integer.parseInt(args[1]);

            // Initialize and start server
            System.out.println("Starting server on port " + portNumber);
            Server server = new Server(portNumber);
            try
            {
                server.start();
            }
            catch(IOException e)
            {
                System.out.println(e.toString());
            }
        }

        //Run as a client
        else if(args[0].contains("client"))
        {
            if(args.length != 4)
            {
                System.out.println("Incorrect number of arguments supplied");
            }

            int portNumber = Integer.parseInt(args[1]);
            String keyfilePath = args[2];
            String serverIp = args[3];
            Client client;
            try
            {
                client = new Client(portNumber, keyfilePath, serverIp);
                client.startClient();
            }
            catch(IOException e)
            {
                System.out.println("Error during authentication process");
            }
        }
        else
        {
            System.out.println("Please specify client or server");
        }
    }
}
