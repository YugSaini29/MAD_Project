package com.example.momentum;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimetableActivity extends AppCompatActivity {

    EditText startTime, endTime, type;
    Button addBtn;

    List<ScheduleBlock> scheduleList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        type = findViewById(R.id.type);
        addBtn = findViewById(R.id.addBtn);

        addBtn.setOnClickListener(v -> {
            String start = startTime.getText().toString();
            String end = endTime.getText().toString();
            String t = type.getText().toString();

            scheduleList.add(new ScheduleBlock(start, end, t));
            Toast.makeText(this, "Block Added", Toast.LENGTH_SHORT).show();
            startTime.setText("");
            endTime.setText("");
            type.setText("");
        });

        Button doneBtn = findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(v -> {

            Log.d("SIZE", "Schedule size: " + scheduleList.size());

            List<ScheduleBlock> freeSlots = calculateFreeTime(scheduleList);

            for (ScheduleBlock slot : freeSlots) {
                Log.d("FREE", slot.startTime + " - " + slot.endTime);
            }

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    public List<ScheduleBlock> calculateFreeTime(List<ScheduleBlock> schedule) {

        List<ScheduleBlock> freeTime = new ArrayList<>();

        // Step 1: Sort by start time
        Collections.sort(schedule, (a, b) -> a.startTime.compareTo(b.startTime));

        String dayStart = "08:00";
        String dayEnd = "22:00";

        String prevEnd = dayStart;

        for (ScheduleBlock block : schedule) {

            // If gap exists → free time
            if (prevEnd.compareTo(block.startTime) < 0) {
                freeTime.add(new ScheduleBlock(prevEnd, block.startTime, "free"));
            }

            // Move forward
            prevEnd = block.endTime;
        }

        // Check after last block
        if (prevEnd.compareTo(dayEnd) < 0) {
            freeTime.add(new ScheduleBlock(prevEnd, dayEnd, "free"));
        }

        return freeTime;
    }
}

