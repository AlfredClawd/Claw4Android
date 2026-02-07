package ai.openclaw.android.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import ai.openclaw.android.R;
import ai.openclaw.android.api.models.StatusResponse;

public class DashboardFragment extends Fragment {

    private DashboardViewModel dashboardViewModel;
    private TextView textGatewayStatus, textDiskRoot, textDiskData;
    private LinearLayout layoutModelContainer;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        dashboardViewModel = new ViewModelProvider(this).get(DashboardViewModel.class);
        View root = inflater.inflate(R.layout.fragment_dashboard, container, false);
        
        initViews(root);
        setupObservers();
        
        root.findViewById(R.id.btn_refresh).setOnClickListener(v -> {
            dashboardViewModel.fetchLimits();
        });
        
        return root;
    }

    private void initViews(View root) {
        textGatewayStatus = root.findViewById(R.id.text_gateway_status);
        textDiskRoot = root.findViewById(R.id.text_disk_root);
        textDiskData = root.findViewById(R.id.text_disk_data);
        layoutModelContainer = root.findViewById(R.id.layout_model_container);
    }

    private void setupObservers() {
        dashboardViewModel.getStatusData().observe(getViewLifecycleOwner(), this::updateUi);
        dashboardViewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null && getView() != null) {
                Snackbar.make(getView(), error, Snackbar.LENGTH_LONG).show();
            }
        });
    }

    private void updateUi(StatusResponse data) {
        textGatewayStatus.setText("Gateway: " + data.gateway);
        textDiskRoot.setText("Root Disk: " + data.diskRoot);
        textDiskData.setText("Data Disk: " + data.diskData);

        layoutModelContainer.removeAllViews();

        // Show provider errors (e.g. Anthropic 401)
        if (data.errors != null && !data.errors.isEmpty()) {
            for (String error : data.errors) {
                layoutModelContainer.addView(createErrorCard(error));
            }
        }

        if (data.models != null) {
            for (StatusResponse.ModelInfo model : data.models) {
                View card = createModelCard(model);
                layoutModelContainer.addView(card);
            }
        }
    }

    private View createErrorCard(String error) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardBackgroundColor(requireContext().getColor(android.R.color.holo_red_dark));
        card.setCardElevation(dpToPx(1));

        TextView text = new TextView(requireContext());
        text.setText("⚠️ " + error);
        text.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));
        text.setTextColor(requireContext().getColor(android.R.color.white));
        text.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodyMedium);

        card.addView(text);
        return card;
    }

    private View createModelCard(StatusResponse.ModelInfo model) {
        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 16);
        card.setLayoutParams(cardParams);
        card.setRadius(dpToPx(12));
        card.setCardElevation(dpToPx(1));

        LinearLayout layout = new LinearLayout(requireContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(dpToPx(16), dpToPx(16), dpToPx(16), dpToPx(16));

        TextView name = new TextView(requireContext());
        name.setText(model.name);
        name.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_TitleMedium);
        name.setTextColor(requireContext().getColor(R.color.claw_red));

        TextView usage = new TextView(requireContext());
        usage.setText("Remaining: " + model.remaining + "%");
        usage.setPadding(0, 8, 0, 0);

        TextView reset = new TextView(requireContext());
        reset.setText("Resets in: " + model.reset);
        reset.setTextAppearance(com.google.android.material.R.style.TextAppearance_Material3_BodySmall);

        layout.addView(name);
        layout.addView(usage);
        layout.addView(reset);
        card.addView(layout);

        return card;
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round((float) dp * density);
    }
}
