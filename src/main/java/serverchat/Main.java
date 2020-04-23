package main.java.serverchat;

public class Main
{
    public static void main(String[] args)
    {
        //Run as a server
        if(args[0].contains("server"))
        {
            int portNumber = Integer.parseInt(args[1]);
            System.out.println("Starting server on port " + portNumber);
            Server server = new Server(portNumber);
            try
            {
                server.start();
            }
            catch(Exception e)
            {

            }

        }

        //Run as a client
        else if(args[0].contains("client"))
        {
            int portNumber = Integer.parseInt(args[1]);
            System.out.println("Client connecting on port " + portNumber);
            Client client = new Client(portNumber);
            try
            {
                client.startClient();
            }
            catch(Exception e)
            {

            }
        }
    }
}
