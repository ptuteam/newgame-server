package websocket;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import game.Player;
import game.Question;
import game.Room;
import game.RoomManager;
import model.UserProfile;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.WebSocketException;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketClose;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketConnect;
import org.eclipse.jetty.websocket.api.annotations.OnWebSocketMessage;
import org.eclipse.jetty.websocket.api.annotations.WebSocket;
import websocket.messages.Message;
import websocket.messages.NewScoresMessage;
import websocket.messages.SimpleMessage;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


/**
 * alex on 24.10.15.
 */
@WebSocket
public class GameWebSocket {
    private static final int CODE_START = 1;
    private static final int CODE_UPDATE_SCORES = 2;
    private static final int CODE_GAME_OVER = 3;
    private static final int CODE_ENEMY_DISCONNECT = 4;
    private static final int CODE_NEW_QUESTION = 5;
    private static final int CODE_PLAYER_ANSWER = 6;
    private static final int CODE_NO_EMPTY_ROOMS = 7;
    private static final int CODE_NEW_PLAYER_CONNECT = 8;
    private static final int CODE_NEW_ROUND_START = 9;
    private static final int CODE_ANSWER_CORRECTNESS_RESPONSE = 10;
    private static final int CODE_LIST_PLAYERS_IN_ROOM = 11;

    private final UserProfile userProfile;
    private Session mySession;
    private Room room;
    private final RoomManager roomManager;

    public GameWebSocket(UserProfile userProfile, RoomManager roomManager) {
        this.userProfile = userProfile;
        this.roomManager = roomManager;
    }

    @SuppressWarnings("unused")
    @OnWebSocketConnect
    public void onOpen(Session session) {
        mySession = session;
        room = roomManager.connectUser(userProfile, this);
        if (room == null) {
            onNoEmptyRooms();
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketMessage
    public void onMessage(String data) {
        JsonObject message = new JsonParser().parse(data).getAsJsonObject();
        try {
            if (room.getState() == Room.States.PLAYING) {
                switch (message.get("code").getAsInt()) {
                    case CODE_PLAYER_ANSWER:
                        room.checkAnswer(userProfile, message.get("answer").getAsString());
                        break;
                    default:
                        break;
                }
            }
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("unused")
    @OnWebSocketClose
    public void onClose(int statusCode, String reason) {
        if (room != null) {
            room.disconnectUser(userProfile);
        }
    }

    private void sendMessage(Message message) {
        try {
            mySession.getRemote().sendString(message.getAsString());
        } catch (IOException | WebSocketException e) {
            e.printStackTrace();
        }
    }

    @SuppressWarnings("Duplicates")
    public void onStartGame(Collection<Player> players) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_START);
        parameters.put("description", "start");
        parameters.put("players", players);

        sendMessage(new SimpleMessage(parameters));
    }

    public void onNewScores(Map<String, Integer> scoresMap) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_UPDATE_SCORES);
        parameters.put("description", "new players scores");

        sendMessage(new NewScoresMessage(parameters, scoresMap));
    }

    @SuppressWarnings("Duplicates")
    public void onGameOver(Player winner) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_GAME_OVER);
        parameters.put("description", "finish");
        parameters.put("winner", winner.getUserEmail());

        sendMessage(new SimpleMessage(parameters));
    }

    @SuppressWarnings("Duplicates")
    public void onEnemyDisconnect(Player enemy) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_ENEMY_DISCONNECT);
        parameters.put("description", "player disconnect");
        parameters.put("player", enemy.getUserEmail());

        sendMessage(new SimpleMessage(parameters));
    }

    @SuppressWarnings("Duplicates")
    public void onNewQuestionAsk(Question question) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_NEW_QUESTION);
        parameters.put("description", "new question");
        parameters.put("question", question);

        sendMessage(new SimpleMessage(parameters));
    }

    public void onNoEmptyRooms() {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_NO_EMPTY_ROOMS);
        parameters.put("description", "no empty rooms");

        sendMessage(new SimpleMessage(parameters));
    }

    public void onNewPlayerConnect(Player newPlayer) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_NEW_PLAYER_CONNECT);
        parameters.put("description", "new player connect");
        parameters.put("player", newPlayer.getUserProfile());

        sendMessage(new SimpleMessage(parameters));
    }

    @SuppressWarnings("Duplicates")
    public void onNewRoundStart(int round) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_NEW_ROUND_START);
        parameters.put("description", "new round start");
        parameters.put("round", round);

        sendMessage(new SimpleMessage(parameters));
    }

    @SuppressWarnings("Duplicates")
    public void onCorrectAnswer(boolean correct) {
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_ANSWER_CORRECTNESS_RESPONSE);
        parameters.put("description", "is answer correct?");
        parameters.put("correct", correct);

        sendMessage(new SimpleMessage(parameters));
    }

    public void listPlayersInRoom(Collection<Player> players) {
        List<UserProfile> users = players.stream().map(Player::getUserProfile).collect(Collectors.toList());

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("code", CODE_LIST_PLAYERS_IN_ROOM);
        parameters.put("description", "players in room");
        parameters.put("players", users);

        sendMessage(new SimpleMessage(parameters));
    }
}
