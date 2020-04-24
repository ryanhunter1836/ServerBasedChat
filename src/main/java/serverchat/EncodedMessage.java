package main.java.serverchat;

import org.json.simple.*;

import java.nio.charset.StandardCharsets;

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
}
