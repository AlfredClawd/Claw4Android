package ai.openclaw.android.ui.chat;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.StringRes;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.progressindicator.LinearProgressIndicator;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

import ai.openclaw.android.R;
import ai.openclaw.android.api.ApiClient;
import ai.openclaw.android.api.models.AgentRequest;
import ai.openclaw.android.api.models.AgentResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatFragment extends Fragment {

    private RecyclerView recyclerChat;
    private ChatAdapter chatAdapter;
    private ChatViewModel chatViewModel;
    private TextInputEditText editMessage;
    private MaterialButton btnSend;
    private LinearProgressIndicator progressBar;
    private Call<AgentResponse> currentCall;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        chatViewModel = new ViewModelProvider(requireActivity()).get(ChatViewModel.class);

        initViews(view);
        setupRecyclerView();
        setupListeners();
        
        // Observe messages
        chatViewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (chatAdapter.getItemCount() != messages.size()) {
                chatAdapter.setMessages(messages);
                scrollToBottom();
            }
        });
    }

    private void initViews(@NonNull View view) {
        recyclerChat = view.findViewById(R.id.recycler_chat);
        editMessage = view.findViewById(R.id.edit_message);
        btnSend = view.findViewById(R.id.btn_send);
        progressBar = view.findViewById(R.id.progress_bar);
    }

    private void setupRecyclerView() {
        chatAdapter = new ChatAdapter();
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        recyclerChat.setLayoutManager(layoutManager);
        recyclerChat.setAdapter(chatAdapter);
        recyclerChat.setItemAnimator(null);
    }

    private void setupListeners() {
        btnSend.setOnClickListener(v -> sendMessage());

        editMessage.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessage();
                return true;
            }
            return false;
        });
    }

    private void sendMessage() {
        if (isRequestInProgress()) {
            return;
        }

        String text = getMessageText();
        if (text.isEmpty()) {
            showError(R.string.error_empty_message);
            return;
        }

        addUserMessage(text);
        clearInput();
        showLoadingIndicator();

        currentCall = ApiClient.getService().sendAgentMessage(new AgentRequest(text, "me"));
        currentCall.enqueue(new Callback<AgentResponse>() {
            @Override
            public void onResponse(@NonNull Call<AgentResponse> call, @NonNull Response<AgentResponse> response) {
                hideLoadingIndicator();
                currentCall = null;

                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (response.isSuccessful() && response.body() != null) {
                    addBotMessage(response.body().getFirstContent());
                } else {
                    String errorMsg = getString(R.string.error_server, response.code());
                    addErrorMessage(errorMsg);
                }
            }

            @Override
            public void onFailure(@NonNull Call<AgentResponse> call, @NonNull Throwable t) {
                hideLoadingIndicator();
                currentCall = null;

                if (!isAdded() || getContext() == null) {
                    return;
                }

                if (!call.isCanceled()) {
                    String errorMsg = getString(R.string.error_connection, t.getMessage());
                    addErrorMessage(errorMsg);
                    showRetrySnackbar(t.getMessage());
                }
            }
        });
    }

    @NonNull
    private String getMessageText() {
        return editMessage.getText() != null ? editMessage.getText().toString().trim() : "";
    }

    private void addUserMessage(@NonNull String text) {
        ChatAdapter.Message msg = new ChatAdapter.Message(text, true);
        chatViewModel.addMessage(msg);
    }

    private void addBotMessage(@Nullable String text) {
        String message = text != null ? text : getString(R.string.error_unknown);
        chatViewModel.addMessage(new ChatAdapter.Message(message, false));
    }

    private void addErrorMessage(@NonNull String error) {
        chatViewModel.addMessage(new ChatAdapter.Message("⚠️ " + error, false));
    }

    private void clearInput() {
        editMessage.setText("");
        editMessage.clearFocus();
    }

    private void showLoadingIndicator() {
        btnSend.setEnabled(false);
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideLoadingIndicator() {
        btnSend.setEnabled(true);
        progressBar.setVisibility(View.GONE);
    }

    private boolean isRequestInProgress() {
        return currentCall != null && !currentCall.isCanceled();
    }

    private void scrollToBottom() {
        int position = chatAdapter.getItemCount() - 1;
        if (position >= 0) {
            recyclerChat.post(() -> recyclerChat.scrollToPosition(position));
        }
    }

    private void showError(@StringRes int messageRes) {
        if (getView() != null) {
            Snackbar.make(getView(), messageRes, Snackbar.LENGTH_SHORT).show();
        }
    }

    private void showRetrySnackbar(String error) {
        View view = getView();
        if (view != null) {
            Snackbar.make(view, getString(R.string.error_connection, error), Snackbar.LENGTH_LONG)
                    .setAction(R.string.retry, v -> sendMessage())
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
