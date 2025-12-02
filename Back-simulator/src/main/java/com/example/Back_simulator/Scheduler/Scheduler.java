package com.example.Back_simulator.Scheduler;

import com.example.Back_simulator.model.Process;
import com.example.Back_simulator.model.SchedulerResult;
import java.util.List;

public interface Scheduler {
    SchedulerResult schedule(List<Process> processes);
}