package main.java.serverchat;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.net.DatagramPacket;

/**
 * Decoded Message
 * A class to decode messages sent from the server to the client
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class DecodedMessage implements Message
{
    private MessageType messageType;
    private String clientId;
    private String message;
    private JSONObject json;

    public MessageType messageType() { return messageType; }
    public String clientId() { return clientId; }
    public String message() { return message; }
    public String getField(String field) { return (String) json.get(field); }

    /**
     * Decoded Message constructor
     * @param datagram
     * @param standard
     */
    public DecodedMessage(DatagramPacket datagram, boolean standard)
    {
        if(standard)
        {
            json = (JSONObject)JSONValue.parse(new String(datagram.getData(), 0, datagram.getData().length));
            int messageTypeIndex = Integer.parseInt((String)json.get("MessageType"));
            messageType = MessageType.values()[messageTypeIndex];

            // Attempt to get the "Message" field
            try {
                this.message = (String)json.get("Message");
            } catch (Exception e) {
                this.message = null;
            }

            // Attempt to get the "ClientID" field
            try {
                this.clientId = (String)json.get("ClientID");
            } catch (Exception e) {
                this.clientId = null;
            }
        }
        else
        {
            json = (JSONObject)JSONValue.parse(new String(datagram.getData(), 0, datagram.getLength()));
            int messageTypeIndex = Integer.parseInt((String)json.get("MessageType"));
            messageType = MessageType.values()[messageTypeIndex];
        }
    }

    /**
     * Decodes a json string from a message with multiple fields
     * @param message The message as a String
     */
    public DecodedMessage(String message, boolean standard)
    {
        if(standard)
        {
            json = (JSONObject)JSONValue.parse(message);
            int messageTypeIndex = Integer.parseInt((String)json.get("MessageType"));
            messageType = MessageType.values()[messageTypeIndex];

            // Attempt to get the "Message" field
            try {
                this.message = (String)json.get("Message");
            } catch (Exception e) {
                this.message = null;
            }

            // Attempt to get the "ClientID" field
            try {
                this.clientId = (String)json.get("ClientID");
            } catch (Exception e) {
                this.clientId = null;
            }
        }
        else
        {
            json = (JSONObject)JSONValue.parse(message);
            int messageTypeIndex = Integer.parseInt((String)json.get("MessageType"));
            messageType = MessageType.values()[messageTypeIndex];
        }

    }
}
