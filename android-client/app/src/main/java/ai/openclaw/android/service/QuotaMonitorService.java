package ai.openclaw.android.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import androidx.core.app.NotificationCompat;
import ai.openclaw.android.R;
import ai.openclaw.android.api.ApiClient;
import ai.openclaw.android.model.QuotaResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuotaMonitorService extends Service {
    private static final String CHANNEL_ID = "QuotaMonitorChannel";
    private static final int NOTIFICATION_ID = 1001;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        startForeground(NOTIFICATION_ID, getNotification("Lade Quoten..."));
        
        runnable = new Runnable() {
            @Override
            public void run() {
                fetchQuota();
                handler.postDelayed(this, 60000); // Minute update
            }
        };
        handler.post(runnable);
    }

    private void fetchQuota() {
        ApiClient.getLostManagerService().getQuota().enqueue(new Callback<QuotaResponse>() {
            @Override
            public void onResponse(Call<QuotaResponse> call, Response<QuotaResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    QuotaResponse q = response.body();
                    String text = "Flash: " + q.getPercentUsed() + "% | Frei: " + (q.getRemainingTokens()/1000) + "k | Reset: " + q.getResetTime();
                    updateNotification(text);
                }
            }
            @Override
            public void onFailure(Call<QuotaResponse> call, Throwable t) {}
        });
    }

    private Notification getNotification(String content) {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("OpenClaw Quota Guard")
                .setContentText(content)
                .setSmallIcon(R.drawable.ic_refresh)
                .setOngoing(true)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void updateNotification(String content) {
        NotificationManager manager = getSystemService(NotificationManager.class);
        manager.notify(NOTIFICATION_ID, getNotification(content));
    }

    private void createNotificationChannel() {
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID, "Quota Monitor", NotificationManager.IMPORTANCE_LOW);
        getSystemService(NotificationManager.class).createNotificationChannel(channel);
    }

    @Override
    public IBinder onBind(Intent intent) { return null; }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_STICKY;
    }
}
