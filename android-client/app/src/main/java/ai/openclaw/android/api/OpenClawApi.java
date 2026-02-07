package ai.openclaw.android.api;

import ai.openclaw.android.api.models.AgentRequest;
import ai.openclaw.android.api.models.AgentResponse;
import ai.openclaw.android.api.models.StatusResponse;
import ai.openclaw.android.model.QuotaResponse;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;

public interface OpenClawApi {
    @GET("/")
    Call<ResponseBody> testConnection();

    @GET("/api/status")
    Call<StatusResponse> getSystemStatus();

    @GET("/api/v1/quota")
    Call<QuotaResponse> getQuota();

    @POST("/v1/responses")
    Call<AgentResponse> sendAgentMessage(@Body AgentRequest request);
}
