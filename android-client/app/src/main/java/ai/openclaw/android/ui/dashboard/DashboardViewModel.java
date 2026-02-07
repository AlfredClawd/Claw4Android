package ai.openclaw.android.ui.dashboard;

import android.app.Application;
import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import ai.openclaw.android.api.ApiClient;
import ai.openclaw.android.api.models.StatusResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DashboardViewModel extends AndroidViewModel {

    private final MutableLiveData<StatusResponse> statusData;
    private final MutableLiveData<String> errorMessage;

    public DashboardViewModel(@NonNull Application application) {
        super(application);
        statusData = new MutableLiveData<>();
        errorMessage = new MutableLiveData<>();
        fetchLimits();
    }

    public LiveData<StatusResponse> getStatusData() {
        return statusData;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void fetchLimits() {
        // If we already have data, we only refresh manually
        if (statusData.getValue() != null && !isManualRefresh()) {
            return;
        }

        ApiClient.getLostManagerService().getSystemStatus().enqueue(new Callback<StatusResponse>() {
            @Override
            public void onResponse(@NonNull Call<StatusResponse> call, @NonNull Response<StatusResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    statusData.setValue(response.body());
                } else {
                    errorMessage.setValue("Error HTTP " + response.code());
                }
            }

            @Override
            public void onFailure(@NonNull Call<StatusResponse> call, @NonNull Throwable t) {
                errorMessage.setValue("Connection failed: " + t.getMessage());
            }
        });
    }

    private boolean isManualRefresh() {
        // In a real app we'd pass a flag, for now we keep it simple
        return true; 
    }
}
