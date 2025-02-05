package com.example.sukasolat;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class PrayerTimeAdapter extends RecyclerView.Adapter<PrayerTimeAdapter.PrayerTimeViewHolder> {
    private List<PrayerTimeItem> prayerTimeItems = new ArrayList<>();

    @NonNull
    @Override
    public PrayerTimeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.prayer_time_item, parent, false);
        return new PrayerTimeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PrayerTimeViewHolder holder, int position) {
        PrayerTimeItem item = prayerTimeItems.get(position);
        holder.imageViewPrayerIcon.setImageResource(item.getIconResId());
        holder.textViewPrayerName.setText(item.getName());
        holder.textViewPrayerTime.setText(item.getTime());

        // Apply highlighting for next prayer time
        if (item.isHighlighted()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.next_highlight_color));
        }
        // Apply highlighting for current prayer time
        else if (item.isCurrentHighlighted()) {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.current_highlight_color));
        }
        // No highlighting
        else {
            holder.itemView.setBackgroundColor(holder.itemView.getContext().getResources().getColor(android.R.color.transparent));
        }
    }

    @Override
    public int getItemCount() {
        return prayerTimeItems.size();
    }

    public void setPrayerTimeItems(List<PrayerTimeItem> prayerTimeItems) {
        this.prayerTimeItems = prayerTimeItems;
        notifyDataSetChanged();
    }

    static class PrayerTimeViewHolder extends RecyclerView.ViewHolder {
        ImageView imageViewPrayerIcon;
        TextView textViewPrayerName;
        TextView textViewPrayerTime;

        PrayerTimeViewHolder(View itemView) {
            super(itemView);
            imageViewPrayerIcon = itemView.findViewById(R.id.image_view_prayer_icon);
            textViewPrayerName = itemView.findViewById(R.id.text_view_prayer_name);
            textViewPrayerTime = itemView.findViewById(R.id.text_view_prayer_time);
        }
    }
}