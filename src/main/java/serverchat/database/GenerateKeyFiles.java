package main.java.serverchat.database;

import org.bson.Document;

import java.io.FileWriter;
import java.io.IOError;
import java.io.IOException;

//Generates the private key files to be used by the clients
public class GenerateKeyFiles {
    public static void main(String args[]) {
        Database db = new Database();

        for(char i = 'A'; i <= 'J'; i++)
        {
            try {
                FileWriter writer = new FileWriter(("Client-ID-" + i + "-key"));
                Document client = db.getClient(("Client-ID-" + i));
                writer.write(client.getString("_id") + "\n");
                writer.write(client.getString("privateKey"));
                writer.close();
            }
            catch(IOException e) {
                System.out.println("Could not generate key file for Client " + i);
            }
        }
    }
}
