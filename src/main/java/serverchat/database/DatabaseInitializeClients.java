package main.java.serverchat.database;

import main.java.serverchat.SecretKeyGenerator;

//This class should be run to seed the database before the first run
public class DatabaseInitializeClients {
    public static void main(String[] args) {
        Database db = new Database();
        SecretKeyGenerator keyGen = new SecretKeyGenerator();

        // INIT 10 client
        for(char i = 'A'; i <= 'J'; i++) {
            System.out.println(db.createClient(("Client-ID-" + i), keyGen.keyToString(keyGen.generateKey())));
        }
    }
}
