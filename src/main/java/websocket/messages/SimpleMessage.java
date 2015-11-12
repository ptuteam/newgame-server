package websocket.messages;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import game.Player;
import game.PlayerSerialiser;
import model.UserProfile;
import model.UserProfileSerialiser;

import java.util.Map;

/**
 * alex on 11.11.15.
 */
public class SimpleMessage implements Message {

    private final Map<String, Object> parameters;

    public SimpleMessage(Map<String, Object> parameters) {
        this.parameters = parameters;
    }

    @Override
    public JsonObject getAsJsonObject() {
        GsonBuilder builder = new GsonBuilder();
        builder.registerTypeAdapter(UserProfile.class, new UserProfileSerialiser());
        builder.registerTypeAdapter(Player.class, new PlayerSerialiser());
        Gson gson = builder.create();
        return gson.toJsonTree(parameters).getAsJsonObject();
    }

    @Override
    public String getAsString() {
        return getAsJsonObject().toString();
    }
}
