package main.java.serverchat;

//Interface to implement message framing and
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
}
