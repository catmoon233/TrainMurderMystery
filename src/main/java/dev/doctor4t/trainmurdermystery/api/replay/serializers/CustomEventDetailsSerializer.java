package dev.doctor4t.trainmurdermystery.api.replay.serializers;

import com.google.gson.*;

import dev.doctor4t.trainmurdermystery.TMM;
import dev.doctor4t.trainmurdermystery.api.replay.ReplayEventTypes.CustomEventDetails;
import net.minecraft.network.chat.Component;

import java.lang.reflect.Type;

public class CustomEventDetailsSerializer
        implements JsonSerializer<CustomEventDetails>, JsonDeserializer<CustomEventDetails> {
    @Override
    public JsonElement serialize(CustomEventDetails src, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject jsonObject = new JsonObject();
        if (TMM.SERVER != null) {
            var provider = TMM.SERVER.registryAccess();
            if (provider != null) {
                try {
                    jsonObject.addProperty("message", Component.Serializer.toJson(src.Message(), provider));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                jsonObject.addProperty("message", "");
            }
        } else {
            jsonObject.addProperty("message", "");
        }

        return jsonObject;
    }

    @Override
    public CustomEventDetails deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
            throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        String data = jsonObject.get("message").getAsString();
        if (TMM.SERVER != null) {
            var provider = TMM.SERVER.registryAccess();
            if (provider != null) {
                return new CustomEventDetails(Component.Serializer.fromJson(data, provider));
            }
        }

        return new CustomEventDetails(null);
    }
}