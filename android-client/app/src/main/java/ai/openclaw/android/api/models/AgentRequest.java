package ai.openclaw.android.api.models;

import com.google.gson.annotations.SerializedName;

public class AgentRequest {
    @SerializedName("model")
    public String model = "openclaw:main";
    @SerializedName("input")
    public String input;
    @SerializedName("user")
    public String user;

    public AgentRequest(String input, String user) {
        this.input = input;
        this.user = user;
    }
}
