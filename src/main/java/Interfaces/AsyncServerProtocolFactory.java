package Interfaces;

public interface AsyncServerProtocolFactory<T> extends ServerProtocolFactory<T> {
    AsyncServerProtocol<T> create();
}
