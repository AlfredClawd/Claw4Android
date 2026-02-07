package ai.openclaw.android.ui.chat;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.card.MaterialCardView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import ai.openclaw.android.R;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    public static class Message {
        @NonNull
        public final String text;
        public final boolean isUser;
        public final long timestamp;

        public Message(@NonNull String text, boolean isUser) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = System.currentTimeMillis();
        }

        public Message(@NonNull String text, boolean isUser, long timestamp) {
            this.text = text;
            this.isUser = isUser;
            this.timestamp = timestamp;
        }
    }

    private final List<Message> messages = Collections.synchronizedList(new ArrayList<>());
    private final SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

    public void addMessage(@NonNull Message message) {
        synchronized (messages) {
            messages.add(message);
            int position = messages.size() - 1;
            notifyItemInserted(position);
        }
    }

    public void setMessages(@NonNull List<Message> newMessages) {
        synchronized (messages) {
            messages.clear();
            messages.addAll(newMessages);
            notifyDataSetChanged();
        }
    }

    public void clearMessages() {
        synchronized (messages) {
            int count = messages.size();
            messages.clear();
            notifyItemRangeRemoved(0, count);
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message;
        synchronized (messages) {
            if (position < 0 || position >= messages.size()) {
                return;
            }
            message = messages.get(position);
        }
        holder.bind(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final TextView textMessage;
        private final TextView textTimestamp;
        private final MaterialCardView cardMessage;
        private final SimpleDateFormat timeFormat;

        MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            textMessage = itemView.findViewById(R.id.text_message);
            textTimestamp = itemView.findViewById(R.id.text_timestamp);
            cardMessage = itemView.findViewById(R.id.card_message);
            timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
        }

        void bind(@NonNull Message message) {
            textMessage.setText(message.text);
            textTimestamp.setText(timeFormat.format(new Date(message.timestamp)));

            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) cardMessage.getLayoutParams();

            if (message.isUser) {
                // User message - right aligned, primary color
                params.gravity = Gravity.END;
                params.setMargins(64, 4, 8, 4);

                cardMessage.setCardBackgroundColor(
                    itemView.getContext().getColor(R.color.primary_container));
                textMessage.setTextColor(
                    itemView.getContext().getColor(R.color.on_primary_container));
                textTimestamp.setTextColor(
                    itemView.getContext().getColor(R.color.on_primary_container));

            } else {
                // Bot message - left aligned, surface color
                params.gravity = Gravity.START;
                params.setMargins(8, 4, 64, 4);

                cardMessage.setCardBackgroundColor(
                    itemView.getContext().getColor(R.color.surface_variant));
                textMessage.setTextColor(
                    itemView.getContext().getColor(R.color.on_surface));
                textTimestamp.setTextColor(
                    itemView.getContext().getColor(R.color.on_surface_variant));
            }

            cardMessage.setLayoutParams(params);
        }
    }
}
