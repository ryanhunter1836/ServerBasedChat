package main.java.serverchat;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

public class DecodedMessage implements Message
{
    private MessageType messageType;
    private String message;
    public MessageType messageType() { return messageType; }
    public String message() { return message; }

    public DecodedMessage(byte[] messageBytes)
    {
        String message = new String(messageBytes);
        JSONObject jsonObject = (JSONObject)JSONValue.parse(new String(messageBytes));
        message = (String)jsonObject.get("Message");
        int messageTypeIndex = Math.toIntExact((long)jsonObject.get("MessageType"));
        messageType = MessageType.values()[messageTypeIndex];


    }
}
