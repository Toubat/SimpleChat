package com.example.simplechat;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.MessageViewHolder> {

    public static final int MESSAGE_OUTGOING = 123;
    public static final int MESSAGE_INCOMING = 321;

    private List<Message> messages;
    private Context mContext;
    private String userId;

    public ChatAdapter(Context context, String userId, List<Message> messages) {
        this.messages = messages;
        this.userId = userId;
        this.mContext = context;
    }

    @Override
    public int getItemViewType(int position) {
        Message message = messages.get(position);
        if (message.getUserId() != null && message.getUserId().equals(userId)) {
            return MESSAGE_OUTGOING;
        } else {
            return MESSAGE_INCOMING;
        }
    }

    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);

        if (viewType == MESSAGE_INCOMING) {
            View view = inflater.inflate(R.layout.message_incoming, parent, false);
            return new IncomingMessageViewHolder(view);
        } else if (viewType == MESSAGE_OUTGOING) {
            View view = inflater.inflate(R.layout.message_outgoing, parent, false);
            return new OutgoingMessageViewHolder(view);
        } else {
            throw new IllegalArgumentException("Unknown view type");
        }
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messages.get(position);
        holder.bindMessage(message);
    }

    @Override
    public int getItemCount() {
        return messages.size();
    }

    public abstract static class MessageViewHolder extends RecyclerView.ViewHolder {

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        abstract void bindMessage(Message message);
    }

    public class IncomingMessageViewHolder extends MessageViewHolder {

        ImageView imageOther;
        TextView body;
        TextView name;

        public IncomingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageOther = itemView.findViewById(R.id.ivProfileOther);
            body = itemView.findViewById(R.id.tvBody);
            name = itemView.findViewById(R.id.tvName);
        }

        @Override
        void bindMessage(Message message) {
            Glide.with(mContext).load(getProfileUrl(message.getUserId())).circleCrop().into(imageOther);
            body.setText(message.getBody());
            name.setText(message.getUserId());
        }
    }

    public class OutgoingMessageViewHolder extends MessageViewHolder {

        ImageView imageMe;
        TextView body;

        public OutgoingMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMe = itemView.findViewById(R.id.ivProfileMe);
            body = itemView.findViewById(R.id.tvBody);
        }

        @Override
        void bindMessage(Message message) {
            Glide.with(mContext).load(getProfileUrl(message.getUserId())).circleCrop().into(imageMe);
            body.setText(message.getBody());
        }
    }

    private static String getProfileUrl(String userId) {
        String hex = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("MD5");
            final byte[] hash = digest.digest(userId.getBytes());
            final BigInteger bigInt = new BigInteger(hash);
            hex = bigInt.abs().toString(16);
        } catch (Exception e){
            e.printStackTrace();
        }
        return "https://www.gravatar.com/avatar/" + hex + "?d=identicon";
    }
}
