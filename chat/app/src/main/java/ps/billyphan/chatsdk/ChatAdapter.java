package ps.billyphan.chatsdk;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kantek.chatsdk.models.MessageEntry;
import com.kantek.chatsdk.models.PageAdapter;

class ChatAdapter extends PageAdapter<MessageEntry> {
    private static final int TYPE_TYPING = 0;
    private static final int TYPE_ITEM = 1;
    private boolean mTyping = false;

    public ChatAdapter(RecyclerView view) {
        view.setAdapter(this);
    }

    public void typing(boolean isTyping) {
        if (isTyping == mTyping) return;
        mTyping = isTyping;
        int itemCount = super.getItemCount();
        if (mTyping) {
            notifyItemInserted(itemCount);
        } else {
            notifyItemRemoved(itemCount);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return (mTyping && (position == super.getItemCount())) ? TYPE_TYPING : TYPE_ITEM;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        if (i == TYPE_TYPING) return new TypingViewHolder(viewGroup);
        return new ViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull PageHolder pageHolder, int i) {
        if (!(pageHolder instanceof TypingViewHolder)) super.onBindViewHolder(pageHolder, i);
    }

    @Override
    public int getItemCount() {
        return super.getItemCount() + (mTyping ? 1 : 0);
    }

    private class TypingViewHolder extends PageHolder<String> {
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

    private class ViewHolder extends PageHolder<MessageEntry> {
        private final TextView txtMessage;
        private final TextView txtReceipt;

        public ViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_view_chat, viewGroup, false));
            txtMessage = itemView.findViewById(R.id.txtMessage);
            txtReceipt = itemView.findViewById(R.id.txtReceipt);
        }

        @Override
        public void bind(MessageEntry item) {
            super.bind(item);
            txtMessage.setText(item.getBody());
            if (!item.isFriendMessage()) {
                txtReceipt.setVisibility(View.VISIBLE);
                txtReceipt.setText(item.getReceiptText());
            } else {
                txtReceipt.setVisibility(View.GONE);
            }
        }

    }
}
