package ai.openclaw.android.api.models;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import java.util.List;

public class AgentResponse {
    @SerializedName("output")
    public List<OutputItem> output;

    public static class OutputItem {
        @SerializedName("type")
        public String type;
        @SerializedName("content")
        public JsonElement content;
        @SerializedName("role")
        public String role;
    }

    public String getFirstContent() {
        if (output == null || output.isEmpty()) return null;

        for (OutputItem item : output) {
            if (item.content == null) continue;

            if (item.content.isJsonPrimitive()) {
                return item.content.getAsString();
            }

            if (item.content.isJsonArray()) {
                JsonArray array = item.content.getAsJsonArray();
                StringBuilder sb = new StringBuilder();
                for (JsonElement part : array) {
                    if (part.isJsonObject()) {
                        JsonObject obj = part.getAsJsonObject();
                        if (obj.has("text")) {
                            sb.append(obj.get("text").getAsString());
                        }
                    } else if (part.isJsonPrimitive()) {
                        sb.append(part.getAsString());
                    }
                }
                if (sb.length() > 0) return sb.toString();
            }
        }
        return null;
    }
}
