package main.java.serverchat.database;

import main.java.serverchat.SecretKeyGenerator;

/**
 * Database Initialize Clients
 * A class that should be run to seed the database before the first run
 * @author Eric Van
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class DatabaseInitializeClients {

    /**
     * Driver function for client generation
     * @param args
     */
    public static void main(String[] args) {
        Database db = new Database();
        SecretKeyGenerator keyGen = new SecretKeyGenerator();

        // INIT 10 client
        for(char i = 'A'; i <= 'J'; i++) {
            System.out.println(db.createClient(("Client-ID-" + i), keyGen.keyToString(keyGen.generateKey())));
        }
    }
}
