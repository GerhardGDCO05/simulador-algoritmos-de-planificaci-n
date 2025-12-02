package com.example.Back_simulator.model;

import lombok.Data;

@Data
public class Process {
    private int id;
    private int arrivalTime;
    private int burstTime;
    private int priority; // para SJF con prioridad 
}