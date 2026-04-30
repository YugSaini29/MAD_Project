package com.example.momentum;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;


public class GoalsActivity extends AppCompatActivity {

    Spinner prioritySpinner;

    EditText goalTitle;
    NumberPicker monthPicker;
    Button addGoalBtn;

    RecyclerView recyclerView;
    GoalAdapter adapter;

    List<Goal> goalList = AppData.getInstance().goalList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_goals);


        goalTitle = findViewById(R.id.goalTitle);
        prioritySpinner = findViewById(R.id.prioritySpinner);
        addGoalBtn = findViewById(R.id.addGoalBtn);
        recyclerView = findViewById(R.id.goalsRecycler);

        // setup recycler
        adapter = new GoalAdapter(goalList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        String[] priorities = {"High", "Medium", "Low"};

        ArrayAdapter<String> adapterSpinner = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                priorities
        );

        prioritySpinner.setAdapter(adapterSpinner);



        addGoalBtn.setOnClickListener(v -> {

            String title = goalTitle.getText().toString().trim();
            String priority = prioritySpinner.getSelectedItem().toString();

            if (!title.isEmpty()) {

                int duration = monthPicker.getValue();

                goalList.add(new Goal(title, priority, duration));
                adapter.notifyItemInserted(goalList.size() - 1);

                goalTitle.setText("");
            }
        });

        monthPicker = findViewById(R.id.monthPicker);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(6); // default valie


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
}