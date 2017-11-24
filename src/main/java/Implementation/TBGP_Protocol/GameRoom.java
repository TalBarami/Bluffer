package Implementation.TBGP_Protocol;

import Implementation.Games.BlufferGame;
import Interfaces.Game;

import javax.naming.InvalidNameException;
import java.util.ArrayList;
import java.util.List;

/**
 * This class represents a game room in the TBGP server.
 */
public class GameRoom {
    /**
     * The current room's name.
     */
    private String roomName;
    /**
     * List of players in the specific room.
     */
    private List<Player> players;
    /**
     * The game which is being played in the current room. null if there is no active game.
     */
    private Game game;

    /**
     * Constructor accepts name for the game room and initialize a new list of players.
     * @param roomName the name of the room.
     */
    public GameRoom(String roomName) {
        this.roomName = roomName;
        this.players = new ArrayList<>();
    }

    public String getRoomName() {
        return roomName;
    }

    public List<Player> getPlayers() {
        return players;
    }

    public boolean isActive() {
        return game != null;
    }

    /**
     * Adding a player to the current room.
     * @param player is the player to be added.
     */
    public void addPlayer(Player player) {
        player.setGameRoom(this);
        player.setState(Player.PlayerState.INROOM);
        players.add(player);
    }

    /**
     * Removes a player from the current room.
     * @param player is the player to be removed.
     */
    public void removePlayer(Player player) {
        player.setGameRoom(null);
        player.setState(Player.PlayerState.LOGGED);
        players.remove(player);
    }

    /**
     * The game room will start a new game via the TBGP server, locking the room by changing it's player's status to "Playing" and creating a new game instance.
     * @param gameName is the game to start.
     * @throws InvalidNameException in case the game was not exists in the database.
     */
    public synchronized void startGame(String gameName) throws InvalidNameException {
        for (Player p : players)
            p.setState(Player.PlayerState.PLAYING);
        if (gameName.equals("Bluffer"))
            game = new BlufferGame(this);
        else throw new InvalidNameException("Invalid game name.");;
    }

    /**
     * Unlocks the room by changing it's player's status back to "In room" and inform the players.
     */
    public void endGame() {
        sendAll("SYSMSG Game over.");
        for (Player p : players)
            p.setState(Player.PlayerState.INROOM);
        game = null;
    }

    public Game getGame() {
        return game;
    }

    /**
     * Sends message to every player in the current room.
     * @param text is the message to be sent.
     */
    public void sendAll(String text) {
        for (Player p : players)
            p.sendMessage(text);
    }

    public boolean isEmpty() {
        return players.isEmpty();
    }
}