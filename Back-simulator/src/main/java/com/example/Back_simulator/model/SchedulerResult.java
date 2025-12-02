package com.example.Back_simulator.model;

import lombok.Data;
import java.util.List;

@Data
public class SchedulerResult {
    private String algorithm;
    private List<Integer> executionOrder;
    private List<Integer> waitingTimes;
    private List<Integer> turnaroundTimes;
    private double avgWaitingTime;
    private double avgTurnaroundTime;
}