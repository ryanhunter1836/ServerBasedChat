package main.java.serverchat;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import java.net.DatagramPacket;

public class DecodedMessage implements Message
{
    private MessageType messageType;
    private String clientId;
    private String message;
    public MessageType messageType() { return messageType; }
    public String clientId() { return clientId; }
    public String message() { return message; }

    public DecodedMessage(DatagramPacket datagram)
    {
        JSONObject jsonObject = (JSONObject)JSONValue.parse(new String(datagram.getData(), 0, datagram.getLength()));
        message = (String)jsonObject.get("Message");
        clientId = (String)jsonObject.get("ClientID");
        int messageTypeIndex = Math.toIntExact((long)jsonObject.get("MessageType"));
        messageType = MessageType.values()[messageTypeIndex];
    }
}
