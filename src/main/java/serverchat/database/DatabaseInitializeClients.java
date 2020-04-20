package main.java.serverchat.database;

public class DatabaseInitializeClients {
    public static void main(String[] args) {
        Database db = new Database();
        // Add a new line per new client to create
        System.out.println(db.createClient("Client-ID-A", "somekey"));
    }
}
