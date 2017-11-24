package Implementation.TBGP_Protocol;

import Implementation.Tokenizer.StringMessage;
import Interfaces.ServerProtocol;
import Interfaces.ServerProtocolFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TBGP_ProtocolFactory implements ServerProtocolFactory<StringMessage> {
    protected Map<String, Player> players;
    protected Map<String, GameRoom> gameRooms;
    protected List<String> supportedGames;

    public TBGP_ProtocolFactory() {
        players = new HashMap<String, Player>();
        gameRooms = new HashMap<String, GameRoom>();
        supportedGames = new ArrayList<String>();

        supportedGames.add("Bluffer");
    }

    @Override
    public ServerProtocol<StringMessage> create() {
        return new TBGP_Protocol(players, gameRooms, supportedGames);
    }

}
