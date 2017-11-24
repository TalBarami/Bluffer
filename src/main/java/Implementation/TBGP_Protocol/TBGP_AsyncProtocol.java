package Implementation.TBGP_Protocol;

import Implementation.Tokenizer.StringMessage;
import Interfaces.AsyncServerProtocol;

import java.util.List;
import java.util.Map;


public class TBGP_AsyncProtocol extends TBGP_Protocol implements AsyncServerProtocol<StringMessage> {
    private boolean termination;

    public TBGP_AsyncProtocol(Map<String, Player> players, Map<String, GameRoom> gameRooms, List<String> supportedGames) {
        super(players, gameRooms, supportedGames);
        termination = false;
    }

    @Override
    public boolean shouldClose() {
        return termination;
    }

    @Override
    public void connectionTerminated() {
        removePlayer();
        termination = true;
    }
}
