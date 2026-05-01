# 🚀 Momentum.ai – Smart Productivity Assistant

Momentum.ai is an Android application that helps students decide **what to do right now** based on their goals, schedule, and available time.

Instead of planning everything manually, the app uses **AI + user context** to suggest the most effective task at any moment.

---

## 🧠 Core Idea

Most productivity apps focus on planning.  
Momentum focuses on **decision-making in real time**.

> “Given my goals and current free time, what should I do right now?”

---

## ✨ Features

### 🎯 Goal Management
- Add goals with:
  - Title  
  - Priority (High / Medium / Low)  
  - Duration (months)  
- Stored in **Firebase Firestore**

---

### 📅 Time Table System
- Users create their daily schedule  
- App calculates:
  - Free time slots  
  - Remaining minutes  

- Stored locally using **SharedPreferences**

---

### 🤖 AI-Powered Suggestions
- Uses **Google Gemini API**
- Takes into account:
  - Current time  
  - Free time  
  - User goals  
  - Task list  

**Output:**
- Best task to do now  
- Duration  
- Short reasoning  

---

### 💬 Chat Mode
- Ask custom questions to AI  
- Context-aware responses  
- Separate from main “decision mode”

---

## 🏗️ Architecture
Android App
↓
User Context (Goals + Time + Free Minutes)
↓
AI Prompt Generation
↓
Gemini API (Direct Call)
↓
Suggestion Display


---

## ⚙️ Tech Stack

- Android (Java)
- Firebase Firestore (Goals storage)
- SharedPreferences (Local timetable storage)
- Gemini API (Google AI)
- RecyclerView (Chat UI)

---

## 🔐 Note on API Security

For this prototype:
- Gemini API is called directly from Android

In production:
- Android → Firebase Cloud Functions → Gemini API

This ensures API keys are secure.

---

## 🚀 How to Run
1. Clone the repository  
2. Open in Android Studio  
3. Add your API key:

```java
String apiKey = "YOUR_API_KEY";
