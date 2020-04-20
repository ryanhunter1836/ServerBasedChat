package main.java.serverchat.database;

import com.mongodb.MongoWriteException;
import com.mongodb.ServerAddress;

import com.mongodb.MongoClientSettings;
import com.mongodb.client.*;
import com.mongodb.client.result.InsertOneResult;
import static com.mongodb.client.model.Filters.*;

import org.bson.Document;

import java.util.Arrays;

/**
 * Database
 * A class which focuses on abstracting database related functions. This will
 * focus on functionality in regards to the server obtaining data rather than
 * database functions itself. The underlying database is MongoDB.
 * @author Eric Van
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class Database {
    private MongoClient mongoClient;

    /**
     * Initializes the client with a connection to localhost
     * with the port number as default, 27017.
     */
    public Database() {
        mongoClient = MongoClients.create();
    }

    /**
     * Initializes the client with a connection to the provided
     * server and port.
     * @param server The string representation of a server name/ip
     * @param port The integer representation of a port number
     */
    public Database(String server, int port) {
        mongoClient = MongoClients.create(
                MongoClientSettings.builder()
                        .applyToClusterSettings(builder ->
                                builder.hosts(Arrays.asList(new ServerAddress(server, port))))
                        .build());
    }

    /**
     * Drops the database.
     */
    public void dropDatabase() {
        MongoDatabase database = mongoClient.getDatabase("chatServer");
        database.drop();
    }

    /**
     * Obtains the client collection in the database chatServer.
     * @return The MongoCollection object correlating to the client collection
     */
    private MongoCollection<Document> getClientCollection() {
        MongoDatabase database = mongoClient.getDatabase("chatServer");
        return database.getCollection("client");
    }

    /**
     * Obtains the history collection in the database chatServer.
     * @return The MongoCollection object correlating to the history collection
     */
    private MongoCollection<Document> getHistoryCollection() {
        MongoDatabase database = mongoClient.getDatabase("chatServer");
        return database.getCollection("history");
    }

    /**
     * Obtains the session collection in the database chatServer.
     * @return The MongoCollection object correlating to the session collection
     */
    private MongoCollection<Document> getSessionCollection() {
        MongoDatabase database = mongoClient.getDatabase("chatServer");
        return database.getCollection("session");
    }

    /**
     * Creates a client in the database chatServer
     * @param clientID The id of the client, in the format of Client-ID-clientID
     * @param privateKey The private key to assign to the client
     * @return true if the client was created
     */
    public boolean createClient(String clientID, String privateKey) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document client = new Document("_id", clientID)
                .append("privateKey", privateKey)
                .append("connectable", false)
                .append("currentSessionID", "");

        try {
            clientCollection.insertOne(client);
        } catch (MongoWriteException writeException) {
            System.out.println(writeException);
            return false;
        }

        return true;
    }

    /**
     * Obtains a client if it exists. Otherwise returns null.
     * @param clientID The id of the client, in the format of Client-ID-clientID
     * @return A document of the client with the following fields:
     * - secretKey (String)
     * - connectable (boolean)
     * - currentSessionID (String)
     */
    public Document getClient(String clientID) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document client = clientCollection.find(eq("_id", clientID)).first();
        return client;
    }

    /**
     * Creates a session with the given two client IDs and provides their session ID. On failure, returns null.
     * @param clientAID The first client's ID
     * @param clientBID The second client's ID
     * @return The _id of the session
     */
    private String createSession(String clientAID, String clientBID) {
        MongoCollection<Document> sessionCollection = getSessionCollection();
        Document session = new Document("clientA", clientAID)
                .append("clientB", clientBID);

        InsertOneResult result;
        try {
            result = sessionCollection.insertOne(session);
        } catch (MongoWriteException writeException) {
            System.out.println(writeException);
            return null;
        }

        return result.getInsertedId().asObjectId().getValue().toString();
    }

    /**
     * Sets the provided client id to a connectable state, such as when the client first connects / ends a chat.
     * @param ClientID The ID of the client
     * @return true if the operation succeeds
     */
    public boolean makeClientConnectable(String ClientID) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document updateDocument = new Document("connectable", true).append("currentSessionID", "");

        try {
            clientCollection.findOneAndUpdate(eq("_id", ClientID),
                    new Document("$set", updateDocument));
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return true;
    }

    /**
     * Sets the provided client id to an unconnectable state, such as when the client disconnects
     * @param ClientID The ID of the client
     * @return true if the operation succeeds
     */
    public boolean makeClientUnconnectable(String ClientID) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document updateDocument = new Document("connectable", false).append("currentSessionID", "");

        try {
            clientCollection.findOneAndUpdate(eq("_id", ClientID),
                    new Document("$set", updateDocument));
        }
        catch (Exception e) {
            System.out.println(e);
            return false;
        }

        return true;
    }

    /**
     * Marks clients as connected if both clients exist. On failure, returns null.
     * The order of the clients does not matter.
     * @param clientAID The ID of the first client
     * @param clientBID The ID of the second client
     * @return The ID of the session
     */
    public String connectClients(String clientAID, String clientBID) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document clientA = clientCollection.find(eq("_id", clientAID)).first();
        Document clientB = clientCollection.find(eq("_id", clientBID)).first();

        if (clientA != null && clientB != null &&
                clientA.getBoolean("connectable") && clientB.getBoolean("connectable")) {

            MongoCollection<Document> sessionCollection = getSessionCollection();
            Document session = sessionCollection.find(
                    or(
                        and(eq("clientA", clientAID), eq("clientB", clientBID)),
                        and(eq("clientA", clientBID), eq("clientB", clientAID))
                    )
            ).first();

            String sessionID;
            if (session == null) {
                sessionID = createSession(clientAID, clientBID);
            }
            else {
                sessionID = session.get("_id").toString();
            }

            Document updateDocument = new Document("connectable", false).append("currentSessionID", sessionID);
            clientCollection.findOneAndUpdate(eq("_id", clientAID),
                    new Document("$set", updateDocument));
            clientCollection.findOneAndUpdate(eq("_id", clientBID),
                    new Document("$set", updateDocument));

            return sessionID;
        }
        return null;
    }

    /**
     * Mark the clients as disconnected from each other, effectively just connectable.
     * @param clientAID The ID of the first client
     * @param clientBID THe ID of the second client
     * @return true if successful
     */
    public boolean disconnectClients(String clientAID, String clientBID) {
        MongoCollection<Document> clientCollection = getClientCollection();
        Document clientA = clientCollection.find(eq("_id", clientAID)).first();
        Document clientB = clientCollection.find(eq("_id", clientBID)).first();

        if (clientA != null && clientB != null &&
                !clientA.getBoolean("connectable") && !clientB.getBoolean("connectable") &&
                clientA.getString("currentSessionID").equals(clientB.getString("currentSessionID"))) {

            boolean clientAConnectable = makeClientConnectable(clientAID);
            boolean clientBConnectable = makeClientConnectable(clientBID);

            return clientAConnectable && clientBConnectable;
        }
        return false;
    }

    /**
     * Adds a chat message to history
     * @param sessionID The session to add history for
     * @param clientID The client that sent the message
     * @param message The message that was sent
     * @return true of the message was saved
     */
    public boolean addChatHistory(String sessionID, String clientID, String message) {
        MongoCollection<Document> historyCollection = getHistoryCollection();
        Document history = new Document("sessionID", sessionID)
                .append("ClientID", clientID)
                .append("message", message);

        try {
            historyCollection.insertOne(history);
        } catch (MongoWriteException writeException) {
            System.out.println(writeException);
            return false;
        }

        return true;
    }

    /**
     * Obtains the chat history.
     * @param sessionID The sessionID to get chat history for
     * @return An iterable containing Documents
     */
    public Iterable<Document> getChatHistory(String sessionID) {
        MongoCollection<Document> historyCollection = getHistoryCollection();
        return historyCollection.find(eq("sessionID", sessionID));
    }

}
