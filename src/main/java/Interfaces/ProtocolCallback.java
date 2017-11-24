package Interfaces;

import java.io.IOException;

public interface ProtocolCallback<T> {
    void sendMessage(T msg) throws IOException;
}
