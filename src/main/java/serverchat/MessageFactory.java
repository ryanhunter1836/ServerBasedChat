package main.java.serverchat;

import java.net.DatagramPacket;
import java.util.HashMap;

//Factory to encode and decode messages
public class MessageFactory implements Message
{
    public static Message encode(MessageType messageType, String clientId, String content)
    {
        return new EncodedMessage(messageType, clientId, content);
    }

    public static Message encode(MessageType messageType, String content)
    {
        return new EncodedMessage(messageType, "_", content);
    }

    /**
     * Encodes a hashmap to an EncodedMessage
     * @param entries The hashmap to retrieve entries from
     * @return The EncodedMessage
     */
    public static EncodedMessage encode(HashMap<String, String> entries) {
        return new EncodedMessage(entries);
    }

    public static Message decode(DatagramPacket datagram)
    {
        return new DecodedMessage(datagram);
    }

    /**
     * Decodes a json string to a Message object
     * @param message The message formatted as a json String
     * @return The message formatted as a Message
     */
    public static DecodedMessage decode(String message)
    {
        return new DecodedMessage(message);
    }
}
