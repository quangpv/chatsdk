package ps.billyphan.chatsdk;

import android.arch.lifecycle.Observer;
import android.support.annotation.NonNull;
import android.support.v4.util.Consumer;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import ps.billyphan.chatsdk.models.Contact;

class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ViewHolder> {
    private List<Contact> mItems;
    private Consumer<Contact> mOnItemClickListener;

    public ContactAdapter(RecyclerView view) {
        view.setAdapter(this);
        mItems = new ArrayList<>();
    }

    private void remove(Contact item) {
        mItems.remove(item);
        notifyDataSetChanged();
    }

    public void add(Contact contact) {
        mItems.add(contact);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(Consumer<Contact> onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, int i) {
        viewHolder.bind(mItems.get(i));
    }

    @Override
    public void onViewRecycled(@NonNull ViewHolder holder) {
        super.onViewRecycled(holder);
        holder.onRecycled();
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void addAll(List<Contact> contacts) {
        mItems.clear();
        mItems.addAll(contacts);
        notifyDataSetChanged();
    }

    class ViewHolder extends RecyclerView.ViewHolder implements Observer<Contact> {
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
            btnRemoveContact.setOnClickListener(v -> remove(mItem));
        }

        public void bind(Contact contact) {
            contact.addObserver(this);
            mItem = contact;
            txtName.setText(contact.contactId);
            txtNumOfInComing.setText(contact.getNumOfUnread());
        }

        @Override
        public void onChanged(Contact contact) {
            txtNumOfInComing.setText(contact.getNumOfUnread());
        }

        public void onRecycled() {
            mItem.removeObserver(this);
        }
    }
}
