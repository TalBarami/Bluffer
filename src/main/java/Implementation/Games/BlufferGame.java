package Implementation.Games;

import Implementation.TBGP_Protocol.GameRoom;
import Implementation.TBGP_Protocol.Player;
import Implementation.Tools.JsonCreateObject;
import Implementation.Tools.JsonParser;
import Interfaces.Game;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by Tal on 13/01/2016.
 */
public class BlufferGame implements Game {
    /**
     * This constant represents the amount of questions per game.
     */
    private final int QUESTIONS_PER_GAME = 3;
    /**
     * Reference to the related game room holding this game.
     */
    private GameRoom gameRoom;
    /**
     * Connects between players and the TESTRESP's received from them (their suggested answer)
     */
    private ConcurrentHashMap<Player, String> playersResponds;
    /**
     * Connects between each index and it's related answer.
     */
    private ConcurrentHashMap<Integer, String> choicesIndexes;
    /**
     * Contains the list of the players chosen each index by using SELECTRESP.
     */
    private ConcurrentHashMap<Integer, List<Player>> playersChoices;
    /**
     * Array of questions to be asked during the game.
     */
    private BlufferQuestion[] questions;
    /**
     * Indicates the amount of questions that still needs to be asked.
     */
    private int questionsLeft;
    /**
     * Indicates how much responses received by the players.
     */
    private int selectedResponses;
    /**
     * Indicates the index of the real answer.
     */
    private int answerIndex;
    /**
     * Indicates whether the game is waiting for TEXTRESP or SELECTRESP from the players.
     * False: TEXTRESP , True: SELECTRESP
     */
    private boolean state; // False: waiting for TEXTRESP; True: waiting for SELECTRESP.

    /**
     * Constructor receives the game room related to this game.
     * @param gameRoom which created this game.
     */
    public BlufferGame(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
        questionsLeft = QUESTIONS_PER_GAME;
        playersResponds = new ConcurrentHashMap<>();
        playersChoices = new ConcurrentHashMap<>();
        choicesIndexes = new ConcurrentHashMap<>();
        if(getQuestions())
            startRound();
        else gameRoom.endGame();
    }

    /**
     * This method is called each time a new round needs to be start.
     * It will prepare the variables and databases for a new round and activate the askText() method.
     */
    private void startRound() {
        playersResponds.clear();
        playersChoices.clear();
        choicesIndexes.clear();
        selectedResponses = 0;
        state = false;
        askText();
    }

    @Override
    public synchronized void processMessage(String message, Player player) {
        String[] messageParts = message.split(" ");
        if (messageParts.length <= 1 || messageParts[1].isEmpty())
            player.unidentifiedMessage(messageParts[0], state ? "Please select an one of the numbers above using 'SELECTRESP'." : "Please suggest an answer using 'TEXTRESP'.");
        else if (messageParts[0].equals("TEXTRESP") && !state) {
            String resp = messageParts[1].toLowerCase();
            if (playersResponds.containsValue(resp) || resp.equals(questions[QUESTIONS_PER_GAME - questionsLeft].realAnswer))
                player.rejectMessage(messageParts[0], "You can't choose this answer because this is the real answer or it was already selected by another player.");
            else if (playersResponds.keySet().contains(player))
                player.rejectMessage(messageParts[0], "You already suggested an answer.");
            else {
                player.acceptMessage(messageParts[0]);
                textResp(resp, player);
            }
        } else if (messageParts[0].equals("SELECTRESP") && state && isValid(messageParts[1])) {
            boolean acceptCommand = true;
            for (List<Player> list : playersChoices.values())
                if (list.contains(player)) {
                    player.rejectMessage(messageParts[0], "You already chose an answer.");
                    acceptCommand = false;
                }
            if (acceptCommand) {
                player.acceptMessage(messageParts[0]);
                selectResp(messageParts[1], player);
            }
        } else {
            player.unidentifiedMessage(messageParts[0], state ? "Please select an one of the numbers above using 'SELECTRESP'." : "Please suggest an answer using 'TEXTRESP'.");
        }
    }

    public boolean contains(String command) {
        return command.equals("TEXTRESP") || command.equals("SELECTRESP");
    }

    public void endGame() {
        String endMessage = "GAMEMSG Summary: ";
        for (Player p : gameRoom.getPlayers())
            endMessage += p.getNickName() + ": " + p.getScore() + " points, ";
        endMessage = endMessage.substring(0, endMessage.length() - 2);
        gameRoom.sendAll(endMessage);
        gameRoom.endGame();
    }

    /**
     * The game will ask the players to suggest an fake-answer for the presented question.
     */
    private void askText() {
        gameRoom.sendAll("ASKTXT " + questions[QUESTIONS_PER_GAME - questionsLeft].questionText);
    }

    /**
     * The game will ask the players to choose the index of the answer they think is real.
     */
    private void askChoices() {
        answerIndex = (int) (Math.random() * playersResponds.size());
        int index = 0;
        String askChoices = "ASKCHOICES ";

        for (Player p : playersResponds.keySet()) {
            if (index == answerIndex) {
                askChoices += index + ". " + questions[QUESTIONS_PER_GAME - questionsLeft].realAnswer + "    ";
                choicesIndexes.put(index, questions[QUESTIONS_PER_GAME - questionsLeft].realAnswer);
                playersChoices.put(index, new ArrayList<>());
                index++;
            }
            askChoices += index + ". " + playersResponds.get(p) + "    ";
            choicesIndexes.put(index, playersResponds.get(p));
            playersChoices.put(index, new ArrayList<>());
            index++;
        }
        gameRoom.sendAll(askChoices);
    }

    /**
     * Adds the fake-answer suggested by the player to the database.
     * @param message is the fake-answer=.
     * @param player is the player who offered this answer.
     */
    private void textResp(String message, Player player) {
        playersResponds.put(player, message);
        if (playersResponds.size() == gameRoom.getPlayers().size()) {
            askChoices();
            state = !state;
        }
    }

    /**
     * Adds the player to the list of the players chosen the specific answer.
     * If all the players had done choosing their answer, the game will finish the round and, if necessary, start a new one.
     * @param message is the answer's index.
     * @param player is the player chosen the answer.
     */
    private void selectResp(String message, Player player) {
        int ans = Integer.parseInt(message);
        playersChoices.get(ans).add(player);
        selectedResponses++;
        if (selectedResponses == (choicesIndexes.size() - 1)) {
            gameRoom.sendAll("GAMEMSG The correct answer is: " + questions[QUESTIONS_PER_GAME - questionsLeft].realAnswer);
            for (Player p : gameRoom.getPlayers()) {
                String gameMessage = "GAMEMSG ";
                if (correctAnswer(p))
                    gameMessage += "correct! +";
                else gameMessage += "wrong! +";
                gameMessage += calculateScore(p) + " points.";
                p.sendMessage(gameMessage);
            }
            questionsLeft--;
            if (questionsLeft == 0) {
                endGame();
            } else startRound();
        }
    }

    /**
     * Indicates whether a given string is a valid integer between 0 and the amount of suggested answers.
     * @param s is the given string.
     * @return true if s represents an integer in the fixed value explained above.
     */
    private boolean isValid(String s) {
        try {
            int x = Integer.parseInt(s);
            if (x >= choicesIndexes.size() || x < 0)
                return false;
        } catch (NumberFormatException e) {
            return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    /**
     * The game will calculate the score for the given player.
     * @param player the give player.
     * @return the current score of the given player.
     */
    private int calculateScore(Player player) {
        int score = 0;
        if (correctAnswer(player))
            score = score + 10;

        String response = playersResponds.get(player);
        int choiceIndex = 0;
        for (int i : choicesIndexes.keySet()) {
            if (choicesIndexes.get(i).equals(response))
                choiceIndex = i;
        }
        score = score + 5 * playersChoices.get(choiceIndex).size();
        player.setScore(player.getScore() + score);
        return score;
    }

    /**
     * Indicates whether the player's choice was the correct answer.
     * @param player the player who chosen the answer.
     * @return true if the player's choice was the correct answer.
     */
    private boolean correctAnswer(Player player) {
        return playersChoices.get(answerIndex).contains(player);
    }

    /**
     * The game will prepare the array of questions that needs to be asked during the game.
     * The game will load the questions randomly from a json file.
     * This method is called only once per game during the initialization.
     * @return
     */
    private boolean getQuestions() {
        JsonParser json = new JsonParser();
        JsonCreateObject data = json.reader("bluffer.json");
        if (data.questions.length < QUESTIONS_PER_GAME) {
            gameRoom.sendAll("Json file must contains at least " + QUESTIONS_PER_GAME + " questions.");
            return false;
        }
        ArrayList<BlufferQuestion> dataAsList = new ArrayList<>(Arrays.asList(data.questions));
        questions = new BlufferQuestion[QUESTIONS_PER_GAME];
        for (int i = 0; i < QUESTIONS_PER_GAME; i++) {
            int index = (int) (Math.random() * dataAsList.size());
            BlufferQuestion b = dataAsList.get(index);
            b.realAnswer = b.realAnswer.toLowerCase();
            dataAsList.remove(index);
            questions[i] = b;
        }
        return true;
    }
}
