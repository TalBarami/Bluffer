package Implementation.TBGP_Protocol;

import Implementation.Tokenizer.StringMessage;
import Interfaces.ProtocolCallback;
import Interfaces.ServerProtocol;

import javax.naming.InvalidNameException;
import java.util.List;
import java.util.Map;



public class TBGP_Protocol implements ServerProtocol<StringMessage> {
    /**
     * Map of players currently playing in the server.
     */
    private Map<String, Player> players;
    /**
     * Map of game rooms currently active in the server.
     */
    private Map<String, GameRoom> gameRooms;
    /**
     * List of supported games.
     */
    private List<String> supportedGames;
    /**
     * The player related to this protocol.
     */
    private Player player;

    public TBGP_Protocol(Map<String, Player> players, Map<String, GameRoom> gameRooms, List<String> supportedGames) {
        this.players = players;
        this.gameRooms = gameRooms;
        this.supportedGames = supportedGames;
        if (player == null) {
            Player player = new Player();
            this.player = player;
        }
    }

    @Override
    public void processMessage(StringMessage msg, ProtocolCallback<StringMessage> callback) {
        if(player.getCallBack() == null)
            player.setCallBack(callback);
        String message = msg.getMessage();
        String[] messageParts = message.split(" ");
        if (message.isEmpty())
            player.unidentifiedMessage(messageParts[0], "Command was not found.");
        else if (player.getState() == Player.PlayerState.CONNECTED && !messageParts[0].equals("QUIT")) {
            if (messageParts[0].equals("NICK")) {
                if (messageParts.length == 1)
                    player.rejectMessage(messageParts[0], "Usage: \"NICK <Nickname>\".");
                else if (players.containsKey(messageParts[1]))
                    player.rejectMessage(messageParts[0], "This nick is already exists.");
                else {
                    players.put(messageParts[1], player);
                    player.setNickName(messageParts[1]);
                    player.setState(Player.PlayerState.LOGGED);
                    player.acceptMessage(messageParts[0]);
                }
            } else
                player.unidentifiedMessage(messageParts[0], "Please choose a nickname first.");
        } else if (messageParts[0].equals("QUIT")) {
                removePlayer();
                player.acceptMessage(messageParts[0]);
        } else if (player.getState() == Player.PlayerState.PLAYING && player.getGameRoom().getGame().contains(messageParts[0])) {
            player.getGameRoom().getGame().processMessage(message, player);
        } else {
            switch (messageParts[0]) {
                case "NICK": {
                    player.rejectMessage(messageParts[0], "Unable to change nickname after it's set.");
                    break;
                }
                case "JOIN": {
                    if (messageParts.length == 1)
                        player.rejectMessage(messageParts[0], "Usage: \"JOIN <Room Name>\".");
                    else if ((gameRooms.containsKey(messageParts[1]) && gameRooms.get(messageParts[1]).isActive()) ||
                            (player.getState() == Player.PlayerState.PLAYING))
                        player.rejectMessage(messageParts[0], "Game is already in progress.");
                    else {
                        if (player.getGameRoom() != null)
                            leaveRoom();
                        if (!gameRooms.containsKey(messageParts[1]))
                            gameRooms.put(messageParts[1], new GameRoom(messageParts[1]));
                        gameRooms.get(messageParts[1]).addPlayer(player);
                        player.acceptMessage(messageParts[0]);
                    }
                    break;
                }
                case "MSG": {
                    if (messageParts.length == 1)
                        player.rejectMessage(messageParts[0], "Usage: \"MSG <Message>\".");
                    else if (player.getState() == Player.PlayerState.LOGGED)
                        player.rejectMessage(messageParts[0], "You must join a game room first.");
                    else {
                        String playerMessage = message.substring(4);
                        for (Player p : player.getGameRoom().getPlayers())
                            if (player != p)
                                p.sendMessage("USRMSG From " + player.getNickName() + ": " + playerMessage);
                        player.acceptMessage(messageParts[0]);
                    }
                    break;
                }
                case "LISTGAMES":
                    String listGamesMessage = "Supported Games: ";
                    for (String s : supportedGames)
                        listGamesMessage += s + ", ";
                    listGamesMessage = listGamesMessage.substring(0, listGamesMessage.length() - 2);
                    player.acceptMessage(messageParts[0], listGamesMessage);
                    break;
                case "STARTGAME": {
                    if (messageParts.length == 1)
                        player.rejectMessage(messageParts[0], "Usage: \"STARTGAME <Game Name>\".");
                    else if (player.getState() == Player.PlayerState.LOGGED)
                        player.rejectMessage(messageParts[0], "You must join a game room first.");
                    else if (player.getState() == Player.PlayerState.PLAYING)
                        player.rejectMessage(messageParts[0], "Game is already running.");
                    else if (!supportedGames.contains(messageParts[1]))
                        player.rejectMessage(messageParts[0], "No such game.");
                    else {
                        player.acceptMessage(messageParts[0]);
                        try {
                            player.getGameRoom().startGame(messageParts[1]);
                        } catch (InvalidNameException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                }
                default: {
                    player.unidentifiedMessage(messageParts[0], "Command was not found.");
                    break;
                }
            }
        }
    }

    @Override
    public boolean isEnd(StringMessage msg) {
        if (player.getState() == Player.PlayerState.PLAYING)
            return false;
        if(msg.equals("QUIT")) {
            player.sendMessage("Press any key to exit.");
            return true;
        }
        return false;
    }

    protected void removePlayer() {
        if (player.getState() != Player.PlayerState.CONNECTED) {
            if (player.getGameRoom() != null)
                leaveRoom();
            players.remove(player.getNickName());
        }
    }

    private void leaveRoom() {
        GameRoom room = player.getGameRoom();
        room.removePlayer(player);
        if(room.isActive()) {
            room.sendAll(player.getNickName() + " has disconnected.");
            room.endGame();
        }
        if (room.isEmpty())
            gameRooms.remove(room.getRoomName());
    }
}
