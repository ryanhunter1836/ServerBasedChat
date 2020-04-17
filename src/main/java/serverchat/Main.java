package main.java.serverchat;

import java.io.PrintStream;
import java.util.Scanner;

public class Main
{
    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);
        Server server = new Server(4000);
        server.start();

        //Terminate server if user presses key
        //scanner.nextLine();
        //server.stop();
    }
}