package group.serverchat;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

//Instance of a connection between server and client
public class ClientConnection implements Runnable
{
    private Socket socket;

    public ClientConnection(Socket socket)
    {
        this.socket = socket;
    }

    //Required for the runnable interface
    public void run()
    {
        try
        {
            //Set up input and output byte streams
            BufferedReader inputStream = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter outputStream = new PrintWriter(socket.getOutputStream(), true);

            String inputLine = "";
            String outputLine = "";

            while ((inputLine = inputStream.readLine()) != null) {
                outputLine = inputLine + "\r";
                outputStream.println(outputLine);
                if (inputLine.contains("Log off"))
                {
                    break;
                }
                //Exit if the server is stopped and interrupts the thread
                if(Thread.interrupted())
                {
                    break;
                }
            }
            System.out.println("Closing connection");
            socket.close();
            inputStream.close();
        }
        catch (IOException e)
        {
            System.out.println(e);
        }
    }
}
