package Implementation.TBGP_Protocol;

import Implementation.Tokenizer.StringMessage;
import Interfaces.ProtocolCallback;

import java.io.IOException;

public class Player {
    public enum PlayerState {CONNECTED, LOGGED, INROOM, PLAYING}

    private String nickName;
    private ProtocolCallback callBack;
    private GameRoom gameRoom;
    private int score;
    private PlayerState currentState;

    public Player(){this(null);}

    public Player(ProtocolCallback callBack) {
        this.callBack = callBack;
        this.currentState = PlayerState.CONNECTED;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public ProtocolCallback getCallBack() {
        return callBack;
    }

    public void setCallBack(ProtocolCallback callBack) {
        this.callBack = callBack;
    }

    public GameRoom getGameRoom() {
        return gameRoom;
    }

    public void setGameRoom(GameRoom gameRoom) {
        this.gameRoom = gameRoom;
    }

    public PlayerState getState() {
        return currentState;
    }

    public void setState(PlayerState playerState) {
        currentState = playerState;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public void sendMessage(String text) {
        try {
            callBack.sendMessage(new StringMessage(text));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void acceptMessage(String message) {
        sendMessage("SYSMSG " + message + " ACCEPTED.");
    }

    public void acceptMessage(String message, String info) {
        sendMessage("SYSMSG " + message + " ACCEPTED. " + info);
    }

    public void rejectMessage(String message, String info) {
        sendMessage("SYSMSG " + message + " REJECTED. " + info);
    }

    public void unidentifiedMessage(String message, String info) {
        sendMessage("SYSMSG " + message + " UNIDENTIFIED. " + info);
    }
}
