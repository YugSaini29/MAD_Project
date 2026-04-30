package com.example.momentum;

import android.os.Bundle;
import android.util.Log;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class GoalsActivity extends AppCompatActivity {

    Spinner prioritySpinner;

    EditText goalTitle;
    NumberPicker monthPicker;
    Button addGoalBtn;

    RecyclerView recyclerView;
    GoalAdapter adapter;

    List<Goal> goalList = AppData.getInstance().goalList;

    FirebaseFirestore db;
    String userId = "test_user";

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
            int duration = monthPicker.getValue();

            if (!title.isEmpty()) {

                // create goal object locally
                Goal newGoal = new Goal(title, priority, duration);
                goalList.add(newGoal);
                adapter.notifyItemInserted(goalList.size() - 1);

                // prepare data for Firebase
                Map<String, Object> goalMap = new HashMap<>();
                goalMap.put("title", title);
                goalMap.put("priority", priority);
                goalMap.put("duration", duration);

                // save to Firestore
                db.collection("users")
                        .document(userId)
                        .collection("goals")
                        .add(goalMap)
                        .addOnSuccessListener(documentReference -> {
                            Log.d("FIREBASE", "Goal saved!");
                        })
                        .addOnFailureListener(e -> {
                            Log.e("FIREBASE", "Error saving goal", e);
                        });

                goalTitle.setText("");
            }
        });

        monthPicker = findViewById(R.id.monthPicker);

        monthPicker.setMinValue(1);
        monthPicker.setMaxValue(12);
        monthPicker.setValue(6); // default valie


        // setting firebase stuff

        db = FirebaseFirestore.getInstance();

        fetchGoalsFromFirebase();

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void fetchGoalsFromFirebase() {

        db.collection("users")
                .document(userId)
                .collection("goals")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {

                    goalList.clear(); // clear old list

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {

                        String title = document.getString("title");
                        String priority = document.getString("priority");
                        Long durationLong = document.getLong("duration");

                        int duration = durationLong != null ? durationLong.intValue() : 0;

                        Goal goal = new Goal(title, priority, duration);
                        goalList.add(goal);
                    }

                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    Log.e("FIREBASE", "Error fetching goals", e);
                });
    }
}