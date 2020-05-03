package main.java.serverchat;

import java.net.DatagramPacket;
import java.util.HashMap;

//Factory to encode and decode messages
public class MessageFactory implements Message
{
    public static EncodedMessage encode(MessageType messageType, String clientId, String content)
    {
        return new EncodedMessage(messageType, clientId, content);
    }

    public static EncodedMessage encode(MessageType messageType, String content)
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

    public static DecodedMessage getDecodedMessageObj(DatagramPacket datagram) { return new DecodedMessage(datagram, false); }
    public static DecodedMessage getDecodedMessageObj(String message) { return new DecodedMessage(message, false); }

    public static DecodedMessage decode(DatagramPacket datagram)
    {
        String message = new String(datagram.getData(), 0, datagram.getLength());
        return new DecodedMessage(message, true);
    }

    /**
     * Decodes a json string to a Message object
     * @param message The message formatted as a json String
     * @return The message formatted as a Message
     */
    public static DecodedMessage decode(String message)
    {
        return new DecodedMessage(message, true);
    }
}
