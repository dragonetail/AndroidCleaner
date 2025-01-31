package com.blackharry.androidcleaner.contacts.ui;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.RecyclerView;
import com.blackharry.androidcleaner.R;
import com.blackharry.androidcleaner.contacts.data.ContactEntity;
import com.blackharry.androidcleaner.common.utils.FormatUtils;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ContactsAdapter extends ListAdapter<ContactEntity, ContactsAdapter.ViewHolder> {
    private final OnItemClickListener listener;
    private final Set<String> selectedItems = new HashSet<>();

    public ContactsAdapter(OnItemClickListener listener) {
        super(new DiffUtil.ItemCallback<ContactEntity>() {
            @Override
            public boolean areItemsTheSame(@NonNull ContactEntity oldItem, @NonNull ContactEntity newItem) {
                return String.valueOf(oldItem.getId()).equals(String.valueOf(newItem.getId()));
            }

            @Override
            public boolean areContentsTheSame(@NonNull ContactEntity oldItem, @NonNull ContactEntity newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_contact, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContactEntity contact = getItem(position);
        holder.bind(contact, listener, selectedItems.contains(String.valueOf(contact.getId())));
    }

    public void toggleSelection(String id) {
        if (selectedItems.contains(id)) {
            selectedItems.remove(id);
        } else {
            selectedItems.add(id);
        }
        notifyDataSetChanged();
    }

    public void clearSelection() {
        selectedItems.clear();
        notifyDataSetChanged();
    }

    public Set<String> getSelectedItems() {
        return new HashSet<>(selectedItems);
    }

    public interface OnItemClickListener {
        void onItemClick(ContactEntity contact);
        void onItemLongClick(ContactEntity contact);
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView photoView;
        private final TextView nameText;
        private final TextView phoneText;
        private final ImageView starIcon;
        private final View itemView;

        ViewHolder(View view) {
            super(view);
            itemView = view;
            photoView = view.findViewById(R.id.image_photo);
            nameText = view.findViewById(R.id.text_name);
            phoneText = view.findViewById(R.id.text_phone);
            starIcon = view.findViewById(R.id.icon_star);
        }

        void bind(ContactEntity contact, OnItemClickListener listener, boolean isSelected) {
            // 设置头像
            photoView.setImageResource(R.drawable.ic_person);

            // 设置名称
            nameText.setText(contact.getName());

            // 设置电话号码
            List<String> phoneNumbers = contact.getPhones();
            if (phoneNumbers != null && !phoneNumbers.isEmpty()) {
                phoneText.setVisibility(View.VISIBLE);
                phoneText.setText(phoneNumbers.get(0));
                if (phoneNumbers.size() > 1) {
                    phoneText.append(String.format(" (+%d)", phoneNumbers.size() - 1));
                }
            } else {
                phoneText.setVisibility(View.GONE);
            }

            // 设置星标
            starIcon.setVisibility(contact.isSafeZone() ? View.VISIBLE : View.GONE);

            // 设置选中状态
            itemView.setSelected(isSelected);
            itemView.setBackgroundResource(
                isSelected ? R.drawable.bg_item_selected : R.drawable.bg_item_normal
            );

            // 设置点击事件
            itemView.setOnClickListener(v -> listener.onItemClick(contact));
            itemView.setOnLongClickListener(v -> {
                listener.onItemLongClick(contact);
                return true;
            });
        }
    }
} 