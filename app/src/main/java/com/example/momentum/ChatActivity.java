package com.example.momentum;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ChatActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText input;
    Button sendBtn;

    List<Message> messageList;
    ChatAdapter adapter;

    int freeMinutes;
    boolean hasFreeTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        recyclerView = findViewById(R.id.chatRecycler);
        input = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        freeMinutes = getIntent().getIntExtra("free_minutes", 0);
        hasFreeTime = getIntent().getBooleanExtra("has_free_time", false);

        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();

            if (!text.isEmpty()) {
                messageList.add(new Message(text, true));
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);

                input.setText("");

                String context;

                if (!hasFreeTime || freeMinutes <= 0) {
                    context = "Context: User has no active free time slot right now.";
                } else {
                    context = "Context: User has " + freeMinutes + " minutes of free time right now.";
                }

                sendToAI(text + "\n\n" + context);
            }
        });
    }

    private void sendToAI(String userMessage) {
        new Thread(() -> {
            try {
                OkHttpClient client = new OkHttpClient();

                String apiKey = "AIzaSyAON5Uv9Sb1iDNzmE8k3RzOnm-MBgGos4w";

                JSONArray goalsArray = new JSONArray();
                List<Goal> goalList = AppData.getInstance().goalList;

                for (Goal g : goalList) {
                    JSONObject obj = new JSONObject();
                    obj.put("title", g.title);
                    obj.put("priority", g.priority);
                    obj.put("duration_months", g.durationMonths);
                    goalsArray.put(obj);
                }

                String structuredData = "{\n" +
                        "\"current_time\": \"" + getCurrentTime() + "\",\n" +
                        "\"has_free_time\": " + hasFreeTime + ",\n" +
                        "\"free_time_minutes\": " + freeMinutes + ",\n" +
                        "\"goals\": " + goalsArray.toString() + ",\n" +
                        "\"todays_schedule\": " + getTodaysSchedule().toString() + ",\n" +
                        "\"tasks\": [\n" +
                        "  {\"title\": \"CNN Basics\", \"duration\": 60, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Backpropagation\", \"duration\": 120, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Leetcode Problem\", \"duration\": 45, \"goal\": \"DSA\", \"completed\": false}\n" +
                        "]\n" +
                        "}";

                String prompt = "You are Momentum.ai, an elite productivity coach, academic advisor, and strict time-management assistant for students.\n" +
                        "Your goal is to guide the user towards their goals, keep them on track with their schedule, and provide high-quality advice.\n\n" +
                        "Guidelines:\n" +
                        "1. Be encouraging, highly analytical, and decisive.\n" +
                        "2. When the user asks 'what should I do?', evaluate their free time, their tasks, and their goals, then suggest exactly ONE actionable task.\n" +
                        "3. If they are currently inside a scheduled block (like a lecture or study session), remind them to stay focused on that unless they ask for a break.\n" +
                        "4. If they have limited free time, suggest a micro-task (like a single LeetCode problem or flashcards) that fits perfectly into the gap.\n" +
                        "5. Always answer the user's specific questions directly while keeping their broader goals and schedule in mind.\n" +
                        "6. Keep your responses concise and readable (use bullet points if necessary, but avoid long essays).\n\n" +
                        "User Context:\n" + structuredData + "\n\n" +
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

                    runOnUiThread(() -> {
                        messageList.add(new Message(reply, false));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                } else {
                    runOnUiThread(() -> {
                        messageList.add(new Message("Error: " + response.code(), false));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    messageList.add(new Message("Network or parsing error", false));
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
            }
        }).start();
    }

    private String getCurrentTime() {
        Calendar calendar = Calendar.getInstance();

        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        return String.format("%02d:%02d", hour, minute);
    }

    private JSONArray getTodaysSchedule() {
        JSONArray todaysSchedule = new JSONArray();
        try {
            String json = getSharedPreferences("app_data", MODE_PRIVATE).getString("schedule", "[]");
            JSONArray arr = new JSONArray(json);
            String currentDay = getCurrentDay();
            for (int i = 0; i < arr.length(); i++) {
                JSONObject obj = arr.getJSONObject(i);
                if (obj.has("day") && obj.getString("day").equals(currentDay)) {
                    todaysSchedule.put(obj);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return todaysSchedule;
    }

    private String getCurrentDay() {
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
}