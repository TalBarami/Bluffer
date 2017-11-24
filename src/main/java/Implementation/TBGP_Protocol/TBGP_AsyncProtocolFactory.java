package Implementation.TBGP_Protocol;

import Implementation.Tokenizer.StringMessage;
import Interfaces.AsyncServerProtocol;
import Interfaces.AsyncServerProtocolFactory;


public class TBGP_AsyncProtocolFactory extends TBGP_ProtocolFactory implements AsyncServerProtocolFactory<StringMessage> {
    public TBGP_AsyncProtocolFactory() {
        super();
    }

    @Override
    public AsyncServerProtocol<StringMessage> create() {
        return new TBGP_AsyncProtocol(players, gameRooms, supportedGames);
    }
}