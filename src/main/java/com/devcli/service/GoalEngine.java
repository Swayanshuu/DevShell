package com.devcli.service;

import com.devcli.model.Goal;
import com.devcli.storage.LocalStorageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class GoalEngine {

    private final LocalStorageService storageService;

    @Autowired
    public GoalEngine(LocalStorageService storageService) {
        this.storageService = storageService;
    }

    public List<Goal> getGoals() {
        List<Goal> goals = storageService.getGoals();
        if (goals.isEmpty()) {
            // Seed default goals if empty
            goals.add(new Goal(UUID.randomUUID().toString(), "Solve 100 LeetCode problems", "DSA consistency goal", 100, 82, Goal.Status.IN_PROGRESS, LocalDateTime.now().minusDays(30), null));
            goals.add(new Goal(UUID.randomUUID().toString(), "Ship DevShell v2", "Release major developer operating system update", 100, 64, Goal.Status.IN_PROGRESS, LocalDateTime.now().minusDays(14), null));
            storageService.saveGoals(goals);
        }
        return goals;
    }

    public Goal addGoal(String title) {
        List<Goal> goals = getGoals();
        Goal newGoal = new Goal(UUID.randomUUID().toString(), title, "Personal development goal", 100, 10, Goal.Status.IN_PROGRESS, LocalDateTime.now(), null);
        goals.add(newGoal);
        storageService.saveGoals(goals);
        return newGoal;
    }
}
