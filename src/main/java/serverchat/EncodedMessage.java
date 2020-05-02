package main.java.serverchat;

import org.json.simple.*;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;

//Packages a message into the messaging protocol
public class EncodedMessage implements Message
{
    //Byte array of the encoded message
    private byte[] encodedMessage;

    public byte[] encodedMessage() { return encodedMessage; }

    public EncodedMessage(MessageType messageType, String clientId, String message)
    {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("MessageType", messageType.ordinal());
        jsonMessage.put("ClientID", clientId);
        jsonMessage.put("Message", message);
        encodedMessage = jsonMessage.toJSONString().getBytes();
    }

    /**
     * Constructs an encrypted message with a variable number of entries
     * @param entries A Hashtable of entries (String, String)
     */
    public EncodedMessage(HashMap<String, String> entries) {
        JSONObject jsonMessage = new JSONObject(entries);
        encodedMessage = jsonMessage.toJSONString().getBytes();
    }
}
