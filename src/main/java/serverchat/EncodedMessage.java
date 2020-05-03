package main.java.serverchat;

import org.json.simple.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

/**
 * Encoded Message
 * A class that packages a message into the messaging protocol
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class EncodedMessage implements Message
{
    private String jsonString;

    // Get jsonString
    public String message() { return jsonString; }
    public byte[] encodedMessage() { return jsonString.getBytes(); }

    /**
     * Encoded Message Constructor
     * @param messageType
     * @param clientId
     * @param message
     */
    public EncodedMessage(MessageType messageType, String clientId, String message)
    {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("MessageType", Integer.toString(messageType.ordinal()));
        jsonMessage.put("ClientID", clientId);
        jsonMessage.put("Message", message);
        jsonString = jsonMessage.toJSONString();
    }

    /**
     * Constructs an encrypted message with a variable number of entries
     * @param entries A Hashtable of entries (String, String)
     */
    public EncodedMessage(HashMap<String, String> entries) {
        JSONObject jsonMessage = new JSONObject(entries);
        jsonString = jsonMessage.toJSONString();
    }
}
