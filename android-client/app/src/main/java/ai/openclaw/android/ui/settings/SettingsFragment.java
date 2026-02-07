package ai.openclaw.android.ui.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import java.util.Objects;
import java.util.regex.Pattern;

import ai.openclaw.android.OpenClawApp;
import ai.openclaw.android.R;
import ai.openclaw.android.api.ApiClient;
import ai.openclaw.android.api.models.RateLimitResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsFragment extends Fragment {

    private static final String PREFS_GATEWAY_URL = "gateway_url";
    private static final String PREFS_AUTH_TOKEN = "auth_token";
    private static final String DEFAULT_GATEWAY_URL = "http://10.0.2.2:19001";
    private static final Pattern URL_PATTERN = Pattern.compile(
            "^(https?://)([\\w.-]+)(:[0-9]+)?(/.*)?$"
    );

    private TextInputEditText editGatewayUrl;
    private TextInputEditText editAuthToken;
    private MaterialButton btnSave;
    private MaterialButton btnTestConnection;
    private Call<?> currentCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        loadSettings();
        setupListeners();
    }

    private void initViews(@NonNull View view) {
        editGatewayUrl = view.findViewById(R.id.edit_gateway_url);
        editAuthToken = view.findViewById(R.id.edit_auth_token);
        btnSave = view.findViewById(R.id.btn_save);
        btnTestConnection = view.findViewById(R.id.btn_test_connection);
    }

    private void loadSettings() {
        SharedPreferences prefs = getPreferences();
        String gatewayUrl = prefs.getString(PREFS_GATEWAY_URL, DEFAULT_GATEWAY_URL);
        String authToken = prefs.getString(PREFS_AUTH_TOKEN, "");

        editGatewayUrl.setText(gatewayUrl);
        editAuthToken.setText(authToken);
    }

    private void setupListeners() {
        btnSave.setOnClickListener(v -> saveSettings());
        btnTestConnection.setOnClickListener(v -> testConnection());
    }

    private void saveSettings() {
        String url = getGatewayUrl();
        String token = getAuthToken();

        if (!validateUrl(url)) {
            editGatewayUrl.setError(getString(R.string.settings_error_url));
            editGatewayUrl.requestFocus();
            return;
        }

        String normalizedUrl = normalizeUrl(url);

        SharedPreferences.Editor editor = getPreferences().edit();
        editor.putString(PREFS_GATEWAY_URL, normalizedUrl);
        editor.putString(PREFS_AUTH_TOKEN, token);
        editor.apply();

        ApiClient.rebuild(normalizedUrl);

        showSuccess(R.string.settings_saved);
    }

    private void testConnection() {
        String url = getGatewayUrl();

        if (!validateUrl(url)) {
            editGatewayUrl.setError(getString(R.string.settings_error_url));
            editGatewayUrl.requestFocus();
            return;
        }

        String normalizedUrl = normalizeUrl(url);
        ApiClient.rebuild(normalizedUrl);

        setLoadingState(true);

        Call<okhttp3.ResponseBody> testCall = ApiClient.getService().testConnection();
        currentCall = testCall;
        testCall.enqueue(new Callback<okhttp3.ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Response<okhttp3.ResponseBody> response) {
                setLoadingState(false);
                currentCall = null;

                if (!isAdded()) {
                    return;
                }

                // If we get ANY response from the gateway, it means we reached it.
                showSuccess(R.string.connection_success);
            }

            @Override
            public void onFailure(@NonNull Call<okhttp3.ResponseBody> call, @NonNull Throwable t) {
                setLoadingState(false);
                currentCall = null;

                if (!isAdded()) {
                    return;
                }

                String message = getString(R.string.connection_failed, t.getMessage());
                showError(message);
            }
        });
    }

    @NonNull
    private String getGatewayUrl() {
        return Objects.requireNonNullElse(editGatewayUrl.getText(), "").toString().trim();
    }

    @NonNull
    private String getAuthToken() {
        return Objects.requireNonNullElse(editAuthToken.getText(), "").toString().trim();
    }

    private boolean validateUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return false;
        }
        return URL_PATTERN.matcher(url).matches();
    }

    @NonNull
    private String normalizeUrl(@NonNull String url) {
        if (url.endsWith("/")) {
            return url.substring(0, url.length() - 1);
        }
        return url;
    }

    @NonNull
    private SharedPreferences getPreferences() {
        return OpenClawApp.getInstance().getPrefs();
    }

    private void setLoadingState(boolean loading) {
        btnTestConnection.setEnabled(!loading);
        btnSave.setEnabled(!loading);
        btnTestConnection.setText(loading ? getString(R.string.message_loading) : getString(R.string.test_connection));
    }

    private void showSuccess(@StringRes int messageRes) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, messageRes, Snackbar.LENGTH_SHORT)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_green_dark))
                    .show();
        }
    }

    private void showError(@NonNull String message) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, message, Snackbar.LENGTH_LONG)
                    .setBackgroundTint(requireContext().getColor(android.R.color.holo_red_dark))
                    .show();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (currentCall != null && !currentCall.isCanceled()) {
            currentCall.cancel();
        }
        currentCall = null;
    }
}