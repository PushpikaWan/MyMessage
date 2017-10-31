package com.example.pushpika.mymessage;

import android.content.Context;
import android.support.v4.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactVH> {

    private Context context;
    private List<Contact> contacts;
    private OnClickListener clickListener;

    public ContactAdapter(Context context, List<Contact> contacts) {
        this.context = context;
        if (contacts != null) {
            this.contacts = contacts;
        } else {
            this.contacts = new ArrayList<>();
        }
    }

    @Override
    public ContactVH onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ContactVH(LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false), clickListener);
    }

    @Override
    public void onBindViewHolder(ContactVH holder, int position) {
        holder.bind(contacts.get(position));
    }

    @Override
    public int getItemCount() {
        return contacts.size();
    }

    public List<Contact> getContacts() {
        return contacts;
    }

    public void setOnClickListener(OnClickListener clickListener) {
        this.clickListener = clickListener;
    }

    public class ContactVH extends RecyclerView.ViewHolder implements View.OnClickListener {

        private TextView nameText;
        private TextView curMessage;
        private OnClickListener clickListener;

        public ContactVH(View itemView, OnClickListener clickListener) {
            super(itemView);
            nameText = (TextView) itemView.findViewById(R.id.name);
            this.clickListener = clickListener;

            itemView.setOnClickListener(this);
        }

        public void bind(Contact contact) {
            nameText.setText(contact.getFname()+ " " +contact.getLname());
        }

        @Override
        public void onClick(View v) {
            if (clickListener != null) {
                clickListener.onClick(getAdapterPosition());
            }
        }
    }

    public interface OnClickListener {
        void onClick(int position);
    }
}
