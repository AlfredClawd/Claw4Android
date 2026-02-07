package ai.openclaw.android.api;

import ai.openclaw.android.OpenClawApp;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class ApiClient {

    private static final int CONNECT_TIMEOUT_SECONDS = 15;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int WRITE_TIMEOUT_SECONDS = 30;

    private static volatile Retrofit retrofit;
    private static volatile Retrofit lostManagerRetrofit;
    private static volatile OpenClawApi apiService;
    private static volatile OpenClawApi lostManagerService;
    private static volatile String currentBaseUrl;
    private static final Object LOCK = new Object();

    @NonNull
    public static OpenClawApi getService() {
        if (apiService == null) {
            synchronized (LOCK) {
                if (apiService == null) {
                    String baseUrl = getStoredBaseUrl();
                    buildClient(baseUrl);
                }
            }
        }
        return apiService;
    }

    @NonNull
    public static OpenClawApi getLostManagerService() {
        if (lostManagerService == null) {
            synchronized (LOCK) {
                if (lostManagerService == null) {
                    String baseUrl = getStoredBaseUrl();
                    buildClient(baseUrl);
                }
            }
        }
        return lostManagerService;
    }

    public static void rebuild(@Nullable String baseUrl) {
        String normalizedUrl = normalizeUrl(baseUrl);
        synchronized (LOCK) {
            if (normalizedUrl.equals(currentBaseUrl)) {
                return;
            }
            buildClient(normalizedUrl);
        }
    }

    @NonNull
    public static String getCurrentBaseUrl() {
        return currentBaseUrl != null ? currentBaseUrl : getStoredBaseUrl();
    }

    private static void buildClient(@NonNull String baseUrl) {
        currentBaseUrl = baseUrl;

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .addInterceptor(chain -> {
                    Request original = chain.request();
                    Request.Builder builder = original.newBuilder();

                    String token = getAuthToken();
                    if (!token.isEmpty()) {
                        builder.header("Authorization", "Bearer " + token);
                    }

                    builder.header("Accept", "application/json");
                    builder.header("Content-Type", "application/json");

                    Request request = builder.build();
                    return chain.proceed(request);
                })
                .build();

        retrofit = new Retrofit.Builder()
                .baseUrl(baseUrl.endsWith("/") ? baseUrl : baseUrl + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        apiService = retrofit.create(OpenClawApi.class);

        // Build LostManager client (assumes port 8071 on same host for Quota/Status)
        String lmBaseUrl = baseUrl.replaceAll(":[0-9]+", ":8071");
        lostManagerRetrofit = new Retrofit.Builder()
                .baseUrl(lmBaseUrl.endsWith("/") ? lmBaseUrl : lmBaseUrl + "/")
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        lostManagerService = lostManagerRetrofit.create(OpenClawApi.class);
    }

    @NonNull
    private static String getStoredBaseUrl() {
        String url = OpenClawApp.getInstance().getPrefs().getString("gateway_url", "http://10.0.2.2:19001");
        return normalizeUrl(url);
    }

    @NonNull
    private static String getAuthToken() {
        return OpenClawApp.getInstance().getPrefs().getString("auth_token", "");
    }

    @NonNull
    private static String normalizeUrl(@Nullable String url) {
        if (url == null || url.isEmpty()) {
            return "http://10.0.2.2:19001";
        }
        url = url.trim();
        if (url.endsWith("/")) {
            url = url.substring(0, url.length() - 1);
        }
        return url;
    }
}