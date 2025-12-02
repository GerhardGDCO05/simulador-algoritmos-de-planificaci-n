// backend/src/main/java/com/simulator/scheduler/SJFScheduler.java
package com.example.Back_simulator.Scheduler;

import com.example.Back_simulator.model.Process;
import com.example.Back_simulator.model.SchedulerResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class SJFScheduler implements Scheduler {
    
    @Override
    public SchedulerResult schedule(List<Process> processes) {
        // Ordenar por tiempo de llegada primero
        processes.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        List<Integer> executionOrder = new ArrayList<>();
        List<Integer> waitingTimes = new ArrayList<>(Collections.nCopies(processes.size(), 0));
        List<Integer> turnaroundTimes = new ArrayList<>(Collections.nCopies(processes.size(), 0));
        
        int currentTime = 0;
        List<Process> remainingProcesses = new ArrayList<>(processes);
        List<Process> readyQueue = new ArrayList<>();
        
        while (!remainingProcesses.isEmpty() || !readyQueue.isEmpty()) {
            // Agregar procesos que han llegado a la cola de listos
            Iterator<Process> iterator = remainingProcesses.iterator();
            while (iterator.hasNext()) {
                Process p = iterator.next();
                if (p.getArrivalTime() <= currentTime) {
                    readyQueue.add(p);
                    iterator.remove();
                }
            }
            
            // Si no hay procesos en cola de listos, avanzar tiempo
            if (readyQueue.isEmpty()) {
                if (!remainingProcesses.isEmpty()) {
                    currentTime = remainingProcesses.get(0).getArrivalTime();
                    continue;
                }
                break;
            }
            
            // Seleccionar proceso con menor ráfaga (SJF)
            readyQueue.sort(Comparator.comparingInt(Process::getBurstTime));
            Process currentProcess = readyQueue.remove(0);
            
            executionOrder.add(currentProcess.getId());
            waitingTimes.set(
                currentProcess.getId(), 
                currentTime - currentProcess.getArrivalTime()
            );
            
            currentTime += currentProcess.getBurstTime();
            turnaroundTimes.set(
                currentProcess.getId(), 
                currentTime - currentProcess.getArrivalTime()
            );
        }
        
        return createResult("SJF", executionOrder, waitingTimes, turnaroundTimes);
    }
    
    private SchedulerResult createResult(String algorithm, 
                                        List<Integer> executionOrder,
                                        List<Integer> waitingTimes,
                                        List<Integer> turnaroundTimes) {
        SchedulerResult result = new SchedulerResult();
        result.setAlgorithm(algorithm);
        result.setExecutionOrder(executionOrder);
        result.setWaitingTimes(waitingTimes);
        result.setTurnaroundTimes(turnaroundTimes);
        result.setAvgWaitingTime(
            waitingTimes.stream().mapToInt(Integer::intValue).average().orElse(0)
        );
        result.setAvgTurnaroundTime(
            turnaroundTimes.stream().mapToInt(Integer::intValue).average().orElse(0)
        );
        return result;
    }
}