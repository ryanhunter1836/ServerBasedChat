package main.java.serverchat;

//Factory to encode and decode messages
public class MessageFactory implements Message
{
    public static Message encode(MessageType messageType, String content)
    {
        return new EncodedMessage(messageType, content);
    }

    public static Message decode(byte[] messageBytes)
    {
        return new DecodedMessage(messageBytes);
    }
}
