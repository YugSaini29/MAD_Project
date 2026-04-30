package com.example.momentum;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.ViewHolder> {

    List<Goal> list;

    public GoalAdapter(List<Goal> list) {
        this.list = list;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView goalName, goalPriority;

        public ViewHolder(View view) {
            super(view);
            goalName = view.findViewById(R.id.goalName);
            goalPriority = view.findViewById(R.id.goalPriority);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Goal g = list.get(position);

        holder.goalName.setText(g.title);
        holder.goalPriority.setText(
                "Priority: " + g.priority + " | " + g.durationMonths + " months"
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }
}