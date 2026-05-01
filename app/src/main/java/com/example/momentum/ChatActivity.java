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

                String structuredData = "{\n" +
                        "\"current_time\": \"" + getCurrentTime() + "\",\n" +
                        "\"has_free_time\": " + hasFreeTime + ",\n" +
                        "\"free_time_minutes\": " + freeMinutes + ",\n" +
                        "\"goals\": " + goalsArray.toString() + ",\n" +
                        "\"tasks\": [\n" +
                        "  {\"title\": \"CNN Basics\", \"duration\": 60, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Backpropagation\", \"duration\": 120, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Leetcode Problem\", \"duration\": 45, \"goal\": \"DSA\", \"completed\": false}\n" +
                        "]\n" +
                        "}";

                String prompt = "You are Momentum.ai, a productivity assistant for students.\n" +
                        "Use the user's goals, available time, and message to respond helpfully.\n\n" +
                        "Rules:\n" +
                        "- Keep replies short.\n" +
                        "- If user asks what to do, suggest one best action.\n" +
                        "- If no free time is available, suggest rest or light revision.\n\n" +
                        "User Data:\n" + structuredData + "\n\n" +
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
}