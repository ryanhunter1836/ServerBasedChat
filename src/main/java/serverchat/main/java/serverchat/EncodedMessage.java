package main.java.serverchat;

import org.json.simple.*;

//Packages a message into the messaging protocol
public class EncodedMessage implements Message
{
    //Byte array of the encoded message
    private byte[] encodedMessage;

    public byte[] encodedMessage() { return encodedMessage; }

    public EncodedMessage(MessageType messageType, String message)
    {
        JSONObject jsonMessage = new JSONObject();
        jsonMessage.put("MessageType", messageType.ordinal());
        jsonMessage.put("Message", message);
        String payload = jsonMessage.toJSONString() + "\n";
        encodedMessage = payload.getBytes();
    }
}
