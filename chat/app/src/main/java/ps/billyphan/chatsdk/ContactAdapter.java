package ps.billyphan.chatsdk;

import android.support.annotation.NonNull;
import android.support.v4.util.Consumer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kantek.chatsdk.models.Contact;
import com.kantek.chatsdk.models.PageAdapter;


class ContactAdapter extends PageAdapter<Contact> {
    private Consumer<Contact> mOnItemClickListener;

    protected ContactAdapter(RecyclerView view) {
        view.setAdapter(this);
    }

    public void setOnItemClickListener(Consumer<Contact> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public PageHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(viewGroup);
    }

    public class ViewHolder extends PageHolder<Contact> {
        private final TextView txtName;
        private final TextView txtNumOfInComing;
        private final View btnRemoveContact;
        private Contact mItem;

        ViewHolder(ViewGroup viewGroup) {
            super(LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.item_view_contact, viewGroup, false));
            txtName = itemView.findViewById(R.id.txtName);
            txtNumOfInComing = itemView.findViewById(R.id.txtNumOfInComing);
            btnRemoveContact = itemView.findViewById(R.id.btnRemoveContact);
            itemView.setOnClickListener(v -> {
                if (mOnItemClickListener != null) mOnItemClickListener.accept(mItem);
            });
        }

        @Override
        public void bind(Contact item) {
            super.bind(item);
            mItem = item;
            txtName.setText(item.getContactId());
            txtNumOfInComing.setText(item.getNumOfUnread() + "");
        }
    }
}
