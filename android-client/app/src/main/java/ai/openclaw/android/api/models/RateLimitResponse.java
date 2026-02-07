package ai.openclaw.android.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.Map;

public class RateLimitResponse {
    @SerializedName("limits")
    public Map<String, ModelLimit> limits;

    public static class ModelLimit {
        @SerializedName("remaining")
        public int remaining;
        @SerializedName("limit")
        public int limit;
        @SerializedName("reset_time")
        public String resetTime;
    }
}
