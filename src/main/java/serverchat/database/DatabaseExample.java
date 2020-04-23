package main.java.serverchat.database;

import main.java.serverchat.SecretKeyGenerator;

import main.java.serverchat.database.Database;

import org.bson.Document;

/*
 * Assumption made:
 * - The clients will be created ahead of time with their private keys via DatabaseInitialize.java
 */

class DatabaseExample {
    public static void main(String[] args) {
        Database db = new Database();
        Document clientA, clientB, clientC;
        String sessionIDA, sessionIDB;
        SecretKeyGenerator keyGen = new SecretKeyGenerator();

        System.out.println("Create clients A, B, and C. Returns true on success, false on error.");
        System.out.println(db.createClient("Client-ID-A", keyGen.keyToString(keyGen.generateKey())));
        System.out.println(db.createClient("Client-ID-B", keyGen.keyToString(keyGen.generateKey())));
        System.out.println(db.createClient("Client-ID-C", keyGen.keyToString(keyGen.generateKey())));

        System.out.println("\nAttempt to create client C again, but already exists.");
        System.out.println(db.createClient("Client-ID-C", keyGen.keyToString(keyGen.generateKey())));

        System.out.println("\nGet Client A information.");
        clientA = db.getClient("Client-ID-A");
        System.out.println(clientA);                                    // All values of the client
        System.out.println(clientA.getString("privateKey"));        // A specific value from the client, privateKey

        System.out.println("\nAttempt to get Client D, which doesn't exist. Returns null.");
        clientA = db.getClient("Client-ID-D");
        System.out.println(clientA);

        System.out.println("\nMake Client B connectable.");
        System.out.println(db.makeClientConnectable("Client-ID-B"));
        clientB = db.getClient("Client-ID-B");
        System.out.println(clientB);

        System.out.println("\nTry to make a session between A and B, but A is not connectable.");
        System.out.println(db.connectClients("Client-ID-A", "Client-ID-B"));
        clientA = db.getClient("Client-ID-A");
        clientB = db.getClient("Client-ID-B");
        System.out.println(clientA);
        System.out.println(clientB);

        System.out.println("\nMake Client A and Client C connectable.");
        System.out.println(db.makeClientConnectable("Client-ID-A"));
        System.out.println(db.makeClientConnectable("Client-ID-C"));
        clientA = db.getClient("Client-ID-A");
        clientC = db.getClient("Client-ID-C");
        System.out.println(clientA);
        System.out.println(clientC);

        System.out.println("\nCreate a session between A and B for the first time.");
        sessionIDA = db.connectClients("Client-ID-A", "Client-ID-B");
        System.out.println(sessionIDA);
        clientA = db.getClient("Client-ID-A");
        clientB = db.getClient("Client-ID-B");
        System.out.println(clientA);
        System.out.println(clientB);

        System.out.println("\nEmulate A and B sending messages to each other");
        System.out.println(db.addChatHistory(sessionIDA, "Client-ID-A", "Message 1: Message from A"));
        System.out.println(db.addChatHistory(sessionIDA, "Client-ID-A", "Message 2: Message from A"));
        System.out.println(db.addChatHistory(sessionIDA, "Client-ID-B", "Message 3: Message from B"));
        System.out.println(db.addChatHistory(sessionIDA, "Client-ID-A", "Message 4: Message from A"));
        System.out.println(db.addChatHistory(sessionIDA, "Client-ID-B", "Message 5: Message from B"));

        System.out.println("\nDisconnect the client A and B from the session.");
        System.out.println(db.disconnectClients("Client-ID-A", "Client-ID-B"));
        clientA = db.getClient("Client-ID-A");
        clientB = db.getClient("Client-ID-B");
        System.out.println(clientA);
        System.out.println(clientB);

        System.out.println("\nCreate a session between A and B again, but in reverse order. This shows order does " +
                "not matter. The session is then ended.");
        System.out.println(db.connectClients("Client-ID-B", "Client-ID-A"));
        clientA = db.getClient("Client-ID-A");
        clientB = db.getClient("Client-ID-B");
        System.out.println(clientA);
        System.out.println(clientB);
        System.out.println(db.disconnectClients("Client-ID-A", "Client-ID-B"));

        System.out.println("\nCreate a session between B and C.");
        sessionIDB = db.connectClients("Client-ID-B", "Client-ID-C");
        System.out.println(sessionIDB);
        clientB = db.getClient("Client-ID-B");
        clientC = db.getClient("Client-ID-C");
        System.out.println(clientB);
        System.out.println(clientC);

        System.out.println("\nEmulate B and C sending messages to each other");
        System.out.println(db.addChatHistory(sessionIDB, "Client-ID-B", "Message 1: Message from B"));
        System.out.println(db.addChatHistory(sessionIDB, "Client-ID-C", "Message 2: Message from C"));
        System.out.println(db.addChatHistory(sessionIDB, "Client-ID-B", "Message 3: Message from B"));
        System.out.println(db.addChatHistory(sessionIDB, "Client-ID-C", "Message 4: Message from C"));
        System.out.println(db.addChatHistory(sessionIDB, "Client-ID-B", "Message 5: Message from B"));

        System.out.println("\nSession chat history between A and B:");
        for (Document chatItem : db.getChatHistory(sessionIDA)) {
            System.out.println(chatItem);
        }

        System.out.println("\nSession chat history between B and C:");
        for (Document chatItem : db.getChatHistory(sessionIDB)) {
            System.out.println(chatItem);
        }

        System.out.println("\nDelete the database.");
//        db.dropDatabase();
    }
}