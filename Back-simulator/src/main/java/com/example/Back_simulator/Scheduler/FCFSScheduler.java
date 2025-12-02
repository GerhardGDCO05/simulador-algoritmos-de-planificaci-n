
package com.example.Back_simulator.Scheduler;

import com.example.Back_simulator.model.Process;
import com.example.Back_simulator.model.SchedulerResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class FCFSScheduler implements Scheduler {
    @Override
    public SchedulerResult schedule(List<Process> processes) {
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        List<Integer> executionOrder = new ArrayList<>();
        List<Integer> waitingTimes = new ArrayList<>(Collections.nCopies(processes.size(), 0));
        List<Integer> turnaroundTimes = new ArrayList<>(Collections.nCopies(processes.size(), 0));
        
        int currentTime = 0;
        
        for (Process p : processes) {
            if (currentTime < p.getArrivalTime()) {
                currentTime = p.getArrivalTime();
            }
            waitingTimes.set(p.getId(), currentTime - p.getArrivalTime());
            executionOrder.add(p.getId());
            currentTime += p.getBurstTime();
            turnaroundTimes.set(p.getId(), currentTime - p.getArrivalTime());
        }
        
        SchedulerResult result = new SchedulerResult();
        result.setAlgorithm("FCFS");
        result.setExecutionOrder(executionOrder);
        result.setWaitingTimes(waitingTimes);
        result.setTurnaroundTimes(turnaroundTimes);
        result.setAvgWaitingTime(waitingTimes.stream().mapToInt(Integer::intValue).average().orElse(0));
        result.setAvgTurnaroundTime(turnaroundTimes.stream().mapToInt(Integer::intValue).average().orElse(0));
        
        return result;
    }
}