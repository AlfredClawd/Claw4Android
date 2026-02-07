package ai.openclaw.android.api.models;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class StatusResponse {
    @SerializedName("gateway")
    public String gateway;
    
    @SerializedName("disk_root")
    public String diskRoot;
    
    @SerializedName("disk_data")
    public String diskData;
    
    @SerializedName("models")
    public List<ModelInfo> models;

    @SerializedName("errors")
    public List<String> errors;

    public static class ModelInfo {
        @SerializedName("name")
        public String name;
        @SerializedName("remaining")
        public int remaining;
        @SerializedName("reset")
        public String reset;
    }
}
