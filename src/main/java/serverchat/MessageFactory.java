package main.java.serverchat;

import java.net.DatagramPacket;

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

    public static Message decode(DatagramPacket datagram)
    {
        return new DecodedMessage(datagram);
    }
}
