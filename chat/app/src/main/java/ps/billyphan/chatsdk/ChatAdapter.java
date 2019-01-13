package ps.billyphan.chatsdk;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ps.billyphan.chatsdk.models.MessageEntry;
import ps.billyphan.chatsdk.models.StateEntry;

class ChatAdapter extends RecyclerView.Adapter {
    private static final int TYPE_TYPING = 0;
    private static final int TYPE_ITEM = 1;
    private Map<String, MessageEntry> mMapItems = new HashMap<>();
    private List<MessageEntry> mItems = new ArrayList<>();
    private StateEntry mTyping;

    public ChatAdapter(RecyclerView view) {
        view.setAdapter(this);
    }

    public void add(MessageEntry message) {
        mMapItems.put(message.getId(), message);
        mItems.add(message);
        notifyDataSetChanged();
    }

    public void addAll(List<MessageEntry> messages) {
        for (MessageEntry message : messages) {
            mMapItems.put(message.getId(), message);
            mItems.add(message);
        }
        notifyDataSetChanged();
    }

    public void addOrUpdate(MessageEntry message) {
        if (mMapItems.containsKey(message.getId())) {
            mMapItems.get(message.getId()).setReceipt(message.getReceipt());
        } else {
            mMapItems.put(message.getId(), message);
            mItems.add(message);
        }
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return (isTyping() && (position == getItemCount() - 1)) ? TYPE_TYPING : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_TYPING) return new TypingViewHolder(viewGroup);
        return new ViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i) {
        if (!(viewHolder instanceof TypingViewHolder))
            ((ViewHolder) viewHolder).bind(mItems.get(i));
    }

    @Override
    public int getItemCount() {
        return mItems.size() + (isTyping() ? 1 : 0);
    }

    private boolean isTyping() {
        return (mTyping != null && mTyping.isTyping());
    }

    public void typing(StateEntry message) {
        mTyping = message;
        notifyDataSetChanged();
    }

    private class TypingViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtMessage;
        private final TextView txtReceipt;

        public TypingViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_view_chat, viewGroup, false));
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtReceipt = itemView.findViewById(R.id.txtReceipt);
            txtReceipt.setVisibility(View.GONE);
            txtMessage.setText("Typing...");
        }
    }

    private class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView txtMessage;
        private final TextView txtReceipt;

        public ViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_view_chat, viewGroup, false));
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtReceipt = itemView.findViewById(R.id.txtReceipt);
        }

        public void bind(MessageEntry messageEntry) {
            txtMessage.setText(messageEntry.getBody());
            txtReceipt.setText(messageEntry.getReceiptText());
        }
    }
}
