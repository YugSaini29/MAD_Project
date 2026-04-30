package com.example.momentum;

import java.util.ArrayList;
import java.util.List;

public class AppData {

    private static AppData instance;

    public List<Goal> goalList = new ArrayList<>();

    private AppData() {}

    public static AppData getInstance() {
        if (instance == null) {
            instance = new AppData();
        }
        return instance;
    }
}
