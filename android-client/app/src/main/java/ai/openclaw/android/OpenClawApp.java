package ai.openclaw.android;

import android.app.Application;
import android.content.SharedPreferences;

public class OpenClawApp extends Application {
    
    private static OpenClawApp instance;
    private SharedPreferences prefs;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        prefs = getSharedPreferences("openclaw_prefs", MODE_PRIVATE);
    }

    public static OpenClawApp getInstance() {
        return instance;
    }

    public SharedPreferences getPrefs() {
        return prefs;
    }
}
