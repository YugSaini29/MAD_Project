package com.example.momentum;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    TextView timeText, freeTimeText, suggestionText;
    Button getSuggestionBtn, goalsBtn, timeTableBtn, openChatBtn;

    int freeMinutes;
    boolean hasFreeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        timeText = findViewById(R.id.timeText);
        freeTimeText = findViewById(R.id.freeTimeText);
        suggestionText = findViewById(R.id.suggestionText);
        getSuggestionBtn = findViewById(R.id.getSuggestionBtn);
        goalsBtn = findViewById(R.id.goalsActivity);
        timeTableBtn = findViewById(R.id.GotoTimeTable);
        openChatBtn = findViewById(R.id.openChatBtn);

        freeMinutes = getIntent().getIntExtra("free_minutes", 0);
        hasFreeTime = getIntent().getBooleanExtra("has_free_time", false);

        timeText.setText("Current Time: " + getCurrentTime());

        if (!hasFreeTime || freeMinutes <= 0) {
            freeTimeText.setText("Free Time: No active free slot");
        } else {
            freeTimeText.setText("Free Time: " + freeMinutes + " mins");
        }

        getSuggestionBtn.setOnClickListener(v -> {
            suggestionText.setText("Thinking...");

            String context;

            if (!hasFreeTime || freeMinutes <= 0) {
                context = "User has no active free time slot right now. If it is late, suggest rest or something light.";
            } else {
                context = "User has " + freeMinutes + " minutes of free time right now. Suggest the best task.";
            }

            sendToAI(context);
        });

        goalsBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, GoalsActivity.class);
            startActivity(intent);
        });

        timeTableBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, TimetableActivity.class);
            startActivity(intent);
        });

        openChatBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra("free_minutes", freeMinutes);
            intent.putExtra("has_free_time", hasFreeTime);
            startActivity(intent);
        });
    }

    private void sendToAI(String userMessage) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                String apiKey = "EnterGeminiKey";

                JSONArray goalsArray = new JSONArray();
                List<Goal> goalList = AppData.getInstance().goalList;

                for (Goal g : goalList) {
                    JSONObject obj = new JSONObject();
                    obj.put("title", g.title);
                    obj.put("priority", g.priority);
                    obj.put("duration_months", g.durationMonths);
                    goalsArray.put(obj);
                }

                JSONObject userData = new JSONObject();

                userData.put("current_time", getCurrentTime());
                userData.put("has_free_time", hasFreeTime);
                userData.put("free_time_minutes", freeMinutes);
                userData.put("goals", goalsArray);

                // tasks array
                JSONArray tasks = new JSONArray();

                tasks.put(new JSONObject()
                        .put("title", "CNN Basics")
                        .put("duration", 60)
                        .put("goal", "Deep Learning")
                        .put("completed", false));

                tasks.put(new JSONObject()
                        .put("title", "Backpropagation")
                        .put("duration", 120)
                        .put("goal", "Deep Learning")
                        .put("completed", false));

                tasks.put(new JSONObject()
                        .put("title", "Leetcode Problem")
                        .put("duration", 45)
                        .put("goal", "DSA")
                        .put("completed", false));

                userData.put("tasks", tasks);

                String prompt = "You are an intelligent productivity assistant.\n" +
                        "Decide what the user should do RIGHT NOW.\n\n" +

                        "Rules:\n" +
                        "- Suggest only ONE task\n" +
                        "- Prioritize high priority goals\n" +
                        "- Respect available time\n" +
                        "- If no free time is available, suggest rest or light revision.\n" +
                        "User Data:\n" + userData.toString() + "\n\n" +
                        "User Message:\n" + userMessage;

                JSONObject json = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject contentObj = new JSONObject();
                JSONArray parts = new JSONArray();

                parts.put(new JSONObject().put("text", prompt));
                contentObj.put("parts", parts);
                contents.put(contentObj);
                json.put("contents", contents);

                RequestBody body = RequestBody.create(
                        json.toString(),
                        MediaType.get("application/json")
                );

                Request request = new Request.Builder()
                        .url("https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey)
                        .post(body)
                        .addHeader("Content-Type", "application/json")
                        .build();

                Response response = client.newCall(request).execute();
                String responseBody = response.body().string();

                if (response.isSuccessful()) {
                    JSONObject obj = new JSONObject(responseBody);

                    String reply = obj.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text");

                    runOnUiThread(() -> suggestionText.setText(reply));
                } else {
                    runOnUiThread(() -> suggestionText.setText("Error: " + response.code()));
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> suggestionText.setText("Network or parsing error"));
            }
        }).start();
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return String.format("%02d:%02d", hour, minute);
    }
}