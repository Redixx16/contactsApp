package com.upn.contactsapp.adapters;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
// Importa los siguientes paquetes
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.upn.contactsapp.R;
import com.upn.contactsapp.activities.ContactDetailActivity;
import com.upn.contactsapp.entities.Contact;

import java.util.List;

public class ContactAdaptar extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<Contact> data;


    private static final int VIEW_TYPE_ITEM = 0;
    private static final int VIEW_TYPE_LOADING = 1;

    public ContactAdaptar(List<Contact> data) {
        this.data = data;
    }

    @Override
    public int getItemViewType(int position) {
        return data.get(position) == null ? VIEW_TYPE_LOADING : VIEW_TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());

        if (viewType == VIEW_TYPE_ITEM) {
            View view = inflater.inflate(R.layout.item_contact, parent, false);
            return new ContactViewHolder(view);
        } else {
            View view = inflater.inflate(R.layout.item_loading, parent, false);
            return new LoadingViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Contact item = data.get(position);

        if (holder instanceof ContactViewHolder) {
            ContactViewHolder contactHolder = (ContactViewHolder) holder;

            contactHolder.tvName.setText(item.name);
            contactHolder.tvNumber.setText(item.phone);



            contactHolder.itemView.setOnClickListener(view -> {
                Intent intent = new Intent(view.getContext(), ContactDetailActivity.class);
                intent.putExtra("CONTACT_ID", item.id);
                view.getContext().startActivity(intent);
            });
        } else if (holder instanceof LoadingViewHolder) {

        }
    }

    @Override
    public int getItemCount() {
        return data.size();
    }


    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        TextView tvName, tvNumber;
        ImageView ivPhoto;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            tvName = itemView.findViewById(R.id.tvName);
            tvNumber = itemView.findViewById(R.id.tvNumber);
            ivPhoto = itemView.findViewById(R.id.ivPhoto);
        }
    }


    public static class LoadingViewHolder extends RecyclerView.ViewHolder {

        ProgressBar progressBar;

        public LoadingViewHolder(@NonNull View itemView) {
            super(itemView);

            progressBar = itemView.findViewById(R.id.progressBar);
        }
    }


    public void addLoading() {
        data.add(null);
        notifyItemInserted(data.size() - 1);
    }


    public void removeLoading() {
        int position = data.size() - 1;
        if (position >= 0 && data.get(position) == null) {
            data.remove(position);
            notifyItemRemoved(position);
        }
    }
}
