package com.example.momentum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.momentum.R;
import com.example.momentum.ScheduleBlock;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {

    List<ScheduleBlock> list;

    public ScheduleAdapter(List<ScheduleBlock> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeText, typeText;

        public ViewHolder(View view) {
            super(view);
            timeText = view.findViewById(R.id.timeText);
            typeText = view.findViewById(R.id.typeText);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        ScheduleBlock block = list.get(position);

        holder.timeText.setText(block.startTime + " - " + block.endTime);
        holder.typeText.setText(block.type);
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}