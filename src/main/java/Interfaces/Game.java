package Interfaces;

import Implementation.TBGP_Protocol.Player;

/**
 * An interface for a TBGP server's game.
 */
public interface Game {
    /**
     * Orders the game to operate according to message received by the player.
     * @param message the received message.
     * @param player the player sent the message.
     */
    void processMessage(String message, Player player);

    /**
     * Indicates wheather this game supports a specific type of message.
     * @param command the type of message received.
     * @return true if the received command is supported by this game.
     */
    boolean contains(String command);

    /**
     * Orders the game to terminate itself.
     */
    void endGame();
}
