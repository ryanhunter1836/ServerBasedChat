package group.serverchat;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

//Code is based off the documentation: https://docs.oracle.com/javase/7/docs/api/java/util/concurrent/ExecutorService.html

public class Server
{
    int portNumber;
    private boolean listening = false;
    private ServerSocket serverSocket = null;

    private final ExecutorService threadPool;

    public Server(int port)
    {
        portNumber = port;

        //Create the thread pool
        threadPool = Executors.newFixedThreadPool(10);
    }

    //Call the start method to start the server
    public void start()
    {
        listening = true;
        try {
            serverSocket = new ServerSocket(portNumber);
            System.out.println("Server Started");

            //Welcoming socket that accepts connections from a client then spins off into separate thread
            while(listening)
            {
                Socket socket = serverSocket.accept();
                System.out.println("Received connection from client");
                threadPool.execute(new ClientConnection(socket));
                //new ClientConnection(socket).run();
            }
        }
        catch(IOException e)
        {
            System.out.println(e.toString());
        }
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
        try {
            // Wait a while for existing tasks to terminate
            if (!pool.awaitTermination(60, TimeUnit.SECONDS))
            {
                pool.shutdownNow();
                // Cancel currently executing tasks
                // Wait a while for tasks to respond to being cancelled
                if (!pool.awaitTermination(60, TimeUnit.SECONDS))
                    System.err.println("Pool did not terminate");
            }
        } catch (InterruptedException ie)
        {
            // (Re-)Cancel if current thread also interrupted
            pool.shutdownNow();
            // Preserve interrupt status
            Thread.currentThread().interrupt();
        }
    }
}

