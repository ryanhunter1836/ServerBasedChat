package main.java.serverchat.database;

import main.java.serverchat.SecretKeyGenerator;

import java.io.FileWriter;

/**
 * Database Initialize Clients
 * A class that should be run to seed the database before the first run
 * @author Eric Van
 * @author Ryan Hunter
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class DatabaseInitializeClients {

    /**
     * Driver function for client generation
     * @param args Arguments (not used)
     */
    public static void main(String[] args) {
        Database db = new Database();
        SecretKeyGenerator keyGen = new SecretKeyGenerator();

        // INIT 10 client
        for(char i = 'A'; i <= 'J'; i++) {
            String clientID = "Client-ID-" + i;
            String privateKey = keyGen.keyToString(keyGen.generateKey());
            System.out.println(db.createClient((clientID), privateKey));

            try {
                FileWriter writer = new FileWriter(("Client-ID-" + i + "-key"));
                writer.write(clientID + "\n");
                writer.write(privateKey);
                writer.close();
            }
            catch(Exception e) {
                System.out.println("Could not generate key file for Client " + i);
            }
        }
    }
}
