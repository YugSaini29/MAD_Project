package com.example.momentum;

public class ScheduleBlock {
    String day;
    String startTime;
    String endTime;
    String type;

    public ScheduleBlock(String day, String startTime, String endTime, String type) {
        this.day = day;
        this.startTime = startTime;
        this.endTime = endTime;
        this.type = type;
    }
}
