package ai.openclaw.android.ui.chat;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import java.util.ArrayList;
import java.util.List;

public class ChatViewModel extends ViewModel {
    private final MutableLiveData<List<ChatAdapter.Message>> messages = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<ChatAdapter.Message>> getMessages() {
        return messages;
    }

    public void addMessage(ChatAdapter.Message message) {
        List<ChatAdapter.Message> currentList = messages.getValue();
        if (currentList != null) {
            currentList.add(message);
            messages.setValue(currentList);
        }
    }

    public void clearMessages() {
        messages.setValue(new ArrayList<>());
    }
}
