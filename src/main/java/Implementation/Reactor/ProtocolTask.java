package Implementation.Reactor;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.util.logging.Logger;

import Implementation.Tokenizer.MessageTokenizer;
import Interfaces.ServerProtocol;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 */

public class ProtocolTask<T> implements Runnable {

    private static final Logger logger = Logger.getLogger("edu.spl.reactor");
    private final ServerProtocol<T> _protocol;
    private final MessageTokenizer<T> _tokenizer;
    private final ConnectionHandler<T> _handler;

    public ProtocolTask(final ServerProtocol<T> protocol, final MessageTokenizer<T> tokenizer, final ConnectionHandler<T> h) {
        this._protocol = protocol;
        this._tokenizer = tokenizer;
        this._handler = h;
    }

    // we synchronize on ourselves, in case we are executed by several threads
    // from the thread pool.
    public synchronized void run() {
        // go over all complete messages and process them.
        while (_tokenizer.hasMessage()) {
            T msg = _tokenizer.nextMessage();
            logger.info("Received " + msg);
            this._protocol.processMessage(msg, receievedMessage -> {
                if (receievedMessage != null) {
                    try {
                        ByteBuffer bytes = _tokenizer.getBytesForMessage(receievedMessage);
                        this._handler.addOutData(bytes);
                    } catch (CharacterCodingException e) {
                        e.printStackTrace();
                    }
                }
            });
            if(_protocol.isEnd(msg)) {
                logger.info("Client disconnected." + _handler._sChannel.socket().getRemoteSocketAddress());
                break;
            }
        }
    }

    public void addBytes(ByteBuffer b) {
        _tokenizer.addBytes(b);
    }
}