package com.example.momentum;

import android.content.Intent;
import android.icu.util.Calendar;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TimetableActivity extends AppCompatActivity {

    EditText startTime, endTime, type;
    Spinner daySpinner;
    Button addBtn;

    RecyclerView recyclerView;
    ScheduleAdapter adapter;
    List<ScheduleBlock> scheduleList = new ArrayList<>();
    List<ScheduleBlock> filteredList = new ArrayList<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_timetable);

        startTime = findViewById(R.id.startTime);
        endTime = findViewById(R.id.endTime);
        type = findViewById(R.id.type);
        daySpinner = findViewById(R.id.daySpinner);
        addBtn = findViewById(R.id.addBtn);

        String[] days = new String[]{"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, days);
        daySpinner.setAdapter(spinnerAdapter);

        daySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterScheduleByDay();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        recyclerView = findViewById(R.id.scheduleRecycler);

        adapter = new ScheduleAdapter(filteredList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        loadScheduleLocally();

        addBtn.setOnClickListener(v -> {
            String day = daySpinner.getSelectedItem().toString();
            String start = startTime.getText().toString();
            String end = endTime.getText().toString();
            String t = type.getText().toString();

            scheduleList.add(new ScheduleBlock(day, start, end, t));
            filterScheduleByDay();
            Toast.makeText(this, "Block Added", Toast.LENGTH_SHORT).show();
            startTime.setText("");
            endTime.setText("");
            type.setText("");
        });

        Button doneBtn = findViewById(R.id.doneBtn);

        doneBtn.setOnClickListener(v -> {

            Log.d("SIZE", "Schedule size: " + scheduleList.size());

            String currentDay = getCurrentDay();
            List<ScheduleBlock> currentDaySchedule = new ArrayList<>();
            for (ScheduleBlock block : scheduleList) {
                if (block.day != null && block.day.equals(currentDay)) {
                    currentDaySchedule.add(block);
                }
            }

            List<ScheduleBlock> freeSlots = calculateFreeTime(currentDaySchedule, currentDay);
            ScheduleBlock currentSlot = getCurrentFreeSlot(freeSlots);

            int minutes = 0;
            if (currentSlot != null) {

                minutes = getRemainingMinutes(currentSlot);

                Log.d("CURRENT_SLOT", currentSlot.startTime + " - " + currentSlot.endTime);
                Log.d("MINUTES", "Available: " + minutes);
                for (ScheduleBlock slot : freeSlots) {
                    Log.d("FREE_DEBUG", slot.startTime + " - " + slot.endTime);
                }

            } else {
                Log.d("STATUS", "No free slot right now");
            }
            saveScheduleLocally(scheduleList);

            Intent intent = new Intent(TimetableActivity.this, MainActivity.class);
            boolean hasFreeTime = currentSlot != null;

            intent.putExtra("free_minutes", minutes);
            intent.putExtra("has_free_time", hasFreeTime);

            startActivity(intent);

        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });



    }

    private void filterScheduleByDay() {
        if (daySpinner == null || daySpinner.getSelectedItem() == null) return;
        String selectedDay = daySpinner.getSelectedItem().toString();
        filteredList.clear();
        for (ScheduleBlock block : scheduleList) {
            if (block.day != null && block.day.equals(selectedDay)) {
                filteredList.add(block);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public List<ScheduleBlock> calculateFreeTime(List<ScheduleBlock> schedule, String currentDay) {

        List<ScheduleBlock> freeTime = new ArrayList<>();

        String dayStart = "08:00";
        String dayEnd = "22:00";

        // Step 1: Sort properly using minutes
        Collections.sort(schedule, (a, b) ->
                timeToMinutes(a.startTime) - timeToMinutes(b.startTime)
        );

        int prevEnd = timeToMinutes(dayStart);

        for (ScheduleBlock block : schedule) {

            int start = timeToMinutes(block.startTime);
            int end = timeToMinutes(block.endTime);

            // Skip invalid blocks
            if (end <= start) continue;

            // If gap exists → free time
            if (prevEnd < start) {
                freeTime.add(new ScheduleBlock(
                        currentDay,
                        minutesToTime(prevEnd),
                        minutesToTime(start),
                        "free"
                ));
            }

            // Move forward safely
            prevEnd = Math.max(prevEnd, end);
        }

        int dayEndMin = timeToMinutes(dayEnd);

        // Last free slot
        if (prevEnd < dayEndMin) {
            freeTime.add(new ScheduleBlock(
                    currentDay,
                    minutesToTime(prevEnd),
                    minutesToTime(dayEndMin),
                    "free"
            ));
        }

        return freeTime;
    }

    public String minutesToTime(int minutes) {
        int hour = minutes / 60;
        int min = minutes % 60;
        return String.format("%02d:%02d", hour, min);
    }
    public String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        int day = calendar.get(Calendar.DAY_OF_WEEK);
        switch (day) {
            case Calendar.SUNDAY: return "Sunday";
            case Calendar.MONDAY: return "Monday";
            case Calendar.TUESDAY: return "Tuesday";
            case Calendar.WEDNESDAY: return "Wednesday";
            case Calendar.THURSDAY: return "Thursday";
            case Calendar.FRIDAY: return "Friday";
            case Calendar.SATURDAY: return "Saturday";
        }
        return "Monday";
    }

    public String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return String.format("%02d:%02d", hour, minute);
    }

    public ScheduleBlock getCurrentFreeSlot(List<ScheduleBlock> freeSlots) {

        String currentTime = getCurrentTime();

        for (ScheduleBlock slot : freeSlots) {
            if (currentTime.compareTo(slot.startTime) >= 0 &&
                    currentTime.compareTo(slot.endTime) <= 0) {
                return slot;
            }
        }

        return null;
    }

    public int timeToMinutes(String time) {
        String[] parts = time.split(":");
        int hour = Integer.parseInt(parts[0]);
        int minute = Integer.parseInt(parts[1]);

        return hour * 60 + minute;
    }

    public int getRemainingMinutes(ScheduleBlock slot) {
        String currentTime = getCurrentTime();

        int now = timeToMinutes(currentTime);
        int end = timeToMinutes(slot.endTime);

        return end - now;
    }

    private void saveScheduleLocally(List<ScheduleBlock> scheduleList) {
        JSONArray arr = new JSONArray();

        try {
            for (ScheduleBlock block : scheduleList) {
                JSONObject obj = new JSONObject();
                obj.put("day", block.day);
                obj.put("startTime", block.startTime);
                obj.put("endTime", block.endTime);
                obj.put("type", block.type);
                arr.put(obj);
            }

            getSharedPreferences("app_data", MODE_PRIVATE)
                    .edit()
                    .putString("schedule", arr.toString())
                    .apply();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void loadScheduleLocally() {
        String json = getSharedPreferences("app_data", MODE_PRIVATE)
                .getString("schedule", "[]");

        try {
            JSONArray arr = new JSONArray(json);
            scheduleList.clear();

            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);

                String day = "Monday"; // default
                if (obj.has("day")) {
                    day = obj.getString("day");
                }

                scheduleList.add(new ScheduleBlock(
                        day,
                        obj.getString("startTime"),
                        obj.getString("endTime"),
                        obj.getString("type")
                ));
            }

            filterScheduleByDay();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

