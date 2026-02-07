package ai.openclaw.android.ui;

import ai.openclaw.android.service.QuotaMonitorService;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import ai.openclaw.android.R;
import ai.openclaw.android.ui.dashboard.DashboardFragment;
import ai.openclaw.android.ui.settings.SettingsFragment;

public class MainActivity extends AppCompatActivity {

    private final Fragment mDashboardFragment = new DashboardFragment();
    private final Fragment mSettingsFragment = new SettingsFragment();
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment active = mDashboardFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Start Quota Monitor Service
        Intent serviceIntent = new Intent(this, QuotaMonitorService.class);
        startForegroundService(serviceIntent);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            fm.beginTransaction().add(R.id.fragment_container, mSettingsFragment, "2").hide(mSettingsFragment).commit();
            fm.beginTransaction().add(R.id.fragment_container, mDashboardFragment, "1").commit();
        }

        BottomNavigationView navView = findViewById(R.id.nav_view);
        navView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();
                
                if (id == R.id.navigation_dashboard) {
                    fm.beginTransaction().hide(active).show(mDashboardFragment).commit();
                    active = mDashboardFragment;
                    return true;
                } else if (id == R.id.navigation_settings) {
                    fm.beginTransaction().hide(active).show(mSettingsFragment).commit();
                    active = mSettingsFragment;
                    return true;
                }
                return false;
            }
        });
    }
}
