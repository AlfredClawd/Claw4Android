package ai.openclaw.android.model;

import com.google.gson.annotations.SerializedName;

public class QuotaResponse {
    @SerializedName("model")
    private String model;
    @SerializedName("percent_used")
    private float percentUsed;
    @SerializedName("remaining_tokens")
    private long remainingTokens;
    @SerializedName("total_tokens")
    private long totalTokens;
    @SerializedName("updated_at_ms")
    private long updatedAtMs;

    public float getPercentUsed() { return percentUsed; }
    public long getRemainingTokens() { return remainingTokens; }
}
