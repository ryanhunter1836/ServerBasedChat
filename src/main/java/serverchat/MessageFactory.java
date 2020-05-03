package main.java.serverchat;

import java.net.DatagramPacket;
import java.util.HashMap;

/**
 * Message Factory
 * A class that acts as a factory to encode and decode messages
 * @version 1
 * @since 1.0-SNAPSHOT
 */
public class MessageFactory implements Message
{
    /**
     * Encodes the message with specified message type for a client
     * @param messageType
     * @param clientId
     * @param content
     * @return
     */
    public static EncodedMessage encode(MessageType messageType, String clientId, String content)
    {
        return new EncodedMessage(messageType, clientId, content);
    }

    /**
     * Encodes the message with specified message type but not for specific client
     * @param messageType
     * @param content
     * @return
     */
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

    /**
     * Decodes a datagram packet to a readable decoded message
     * @param datagram
     * @return
     */
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
