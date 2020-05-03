package main.java.serverchat;

/**
 * Message
 * An interface that packages a message into the messaging protocol
 */
public interface Message
{
    public enum MessageType
    {
        HELLO,
        CHALLENGE,
        RESPONSE,
        AUTH_SUCCESS,
        AUTH_FAIL,
        CONNECT,
        CONNECTED,
        CHAT_REQUEST,
        CHAT_STARTED,
        UNREACHABLE,
        END_REQUEST,
        END_NOTIF,
        CHAT,
        HISTORY_REQ,
        HISTORY_RESP
    };

    public int PacketLength = 4196;
}
