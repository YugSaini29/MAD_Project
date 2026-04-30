package com.example.momentum;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.json.*;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    EditText input;
    Button sendBtn;

    List<Message> messageList;
    ChatAdapter adapter;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);


        recyclerView = findViewById(R.id.chatRecycler);
        input = findViewById(R.id.messageInput);
        sendBtn = findViewById(R.id.sendBtn);

        messageList = new ArrayList<>();
        adapter = new ChatAdapter(messageList);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);


        int freeMinutes = getIntent().getIntExtra("free_minutes", -1);

        sendBtn.setOnClickListener(v -> {
            String text = input.getText().toString().trim();

            if (!text.isEmpty()) {
                // taking in user message
                messageList.add(new Message(text, true));
                adapter.notifyItemInserted(messageList.size() - 1);
                recyclerView.scrollToPosition(messageList.size() - 1);

                input.setText("");
                String FreeMinsPrompt = " context : Free time available: " + freeMinutes + " minutes. Suggest accordingly.";

                // fake reply until api inclusion
                sendToAI(text + FreeMinsPrompt);
            }
        });

        findViewById(R.id.goalsActivity).setOnClickListener(v->{
            Intent gotogoalsactivity = new Intent(this, GoalsActivity.class);
            startActivity(gotogoalsactivity);
        });

        findViewById(R.id.GotoTimeTable).setOnClickListener(v -> {
            Intent intent = new Intent(this, TimetableActivity.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }
    private void sendToAI(String userMessage) {
        new Thread(() -> {
            try {

                OkHttpClient client = new OkHttpClient();
                String apiKey = "AIzaSyD-sXk0ZPb0JHfr6luxTMZ_mNrltHK5nSQ";

                JSONObject json = new JSONObject();
                JSONArray contents = new JSONArray();
                JSONObject contentObj = new JSONObject();
                JSONArray parts = new JSONArray();


                String structuredData = "{\n" +
                        "\"current_time\": \"15:30\",\n" +


                        "\"goals\": [\n" +
                        "  {\"title\": \"Deep Learning\", \"priority\": \"high\", \"progress\": 20},\n" +
                        "  {\"title\": \"DSA\", \"priority\": \"medium\", \"progress\": 50}\n" +
                        "],\n" +

                        "\"tasks\": [\n" +
                        "  {\"title\": \"CNN Basics\", \"duration\": 60, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Backpropagation\", \"duration\": 120, \"goal\": \"Deep Learning\", \"completed\": false},\n" +
                        "  {\"title\": \"Solve Leetcode\", \"duration\": 45, \"goal\": \"DSA\", \"completed\": false}\n" +
                        "]\n" +
                        "}";

                String prompt = "You are an intelligent productivity assistant.\n" +
                        "Your job is to choose the BEST task for the user RIGHT NOW.\n\n" +

                        "Rules:\n" +
                        "- Prioritize high priority goals\n" +
                        "- Only pick tasks that fit within available time\n" +
                        "- Prefer tasks that improve progress meaningfully\n" +
                        "- Suggest ONLY ONE task\n" +
                        "- Return format: Task + duration + short reason\n\n" +

                        "User Data:\n" + structuredData +"This message following is from user, try to first see what user's intent is." +userMessage;


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
                    // 2. If it fails, log the exact error so you can debug it
                    System.err.println("Gemini API Error! Code: " + response.code());
                    System.err.println("Error Body: " + responseBody);

                    runOnUiThread(() -> {
                        messageList.add(new Message("Error: " + response.code(), false));
                        adapter.notifyItemInserted(messageList.size() - 1);
                        recyclerView.scrollToPosition(messageList.size() - 1);
                    });
                }

            } catch (Exception e) {
                // 3. Catch actual crashes (like no internet or parsing errors)
                e.printStackTrace();
                runOnUiThread(() -> {
                    messageList.add(new Message("Network or Parsing Exception", false));
                    adapter.notifyItemInserted(messageList.size() - 1);
                    recyclerView.scrollToPosition(messageList.size() - 1);
                });
            }
        }).start();
    }
}