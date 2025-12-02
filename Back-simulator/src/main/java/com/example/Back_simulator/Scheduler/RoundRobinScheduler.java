// backend/src/main/java/com/simulator/scheduler/RoundRobinScheduler.java
package com.example.Back_simulator.Scheduler;

import com.example.Back_simulator.model.Process;
import com.example.Back_simulator.model.SchedulerResult;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class RoundRobinScheduler implements Scheduler {
    
    // Quantum por defecto
    private static final int DEFAULT_QUANTUM = 2;
    
    @Override
    public SchedulerResult schedule(List<Process> processes) {
        return schedule(processes, DEFAULT_QUANTUM);
    }
    
    // Método sobrecargado que acepta quantum personalizado
    public SchedulerResult schedule(List<Process> processes, int quantum) {
        if (quantum <= 0) {
            throw new IllegalArgumentException("Quantum must be greater than 0");
        }
        
        // Crear copia de procesos para no modificar la lista original
        List<Process> processList = new ArrayList<>(processes);
        
        // Ordenar por tiempo de llegada
        processList.sort(Comparator.comparingInt(Process::getArrivalTime));
        
        // Inicializar estructuras de datos
        int n = processList.size();
        List<Integer> executionOrder = new ArrayList<>();
        List<Integer> waitingTimes = new ArrayList<>(Collections.nCopies(n, 0));
        List<Integer> turnaroundTimes = new ArrayList<>(Collections.nCopies(n, 0));
        List<Integer> remainingTime = new ArrayList<>();
        List<Integer> arrivalTime = new ArrayList<>();
        List<Boolean> firstExecution = new ArrayList<>(Collections.nCopies(n, true));
        
        // Inicializar tiempos restantes y tiempos de llegada
        for (Process p : processList) {
            remainingTime.add(p.getBurstTime());
            arrivalTime.add(p.getArrivalTime());
        }
        
        // Cola para Round Robin (almacena índices de procesos)
        Queue<Integer> readyQueue = new LinkedList<>();
        
        int currentTime = 0;
        int completed = 0;
        int index = 0; // Índice para agregar procesos que llegan
        
        // Agregar primeros procesos que llegan en tiempo 0
        while (index < n && processList.get(index).getArrivalTime() <= currentTime) {
            readyQueue.add(index);
            index++;
        }
        
        // Ejecutar Round Robin
        while (completed < n) {
            if (readyQueue.isEmpty()) {
                // Si no hay procesos listos, avanzar al siguiente tiempo de llegada
                if (index < n) {
                    currentTime = processList.get(index).getArrivalTime();
                    readyQueue.add(index);
                    index++;
                }
                continue;
            }
            
            // Sacar proceso de la cola
            int currentIndex = readyQueue.poll();
            Process currentProcess = processList.get(currentIndex);
            
            // Registrar primera ejecución para cálculo de waiting time
            if (firstExecution.get(currentIndex)) {
                waitingTimes.set(currentIndex, currentTime - arrivalTime.get(currentIndex));
                firstExecution.set(currentIndex, false);
            }
            
            // Ejecutar proceso por quantum o hasta que termine
            int executionTime = Math.min(quantum, remainingTime.get(currentIndex));
            executionOrder.add(currentProcess.getId());
            
            // Actualizar tiempo actual
            currentTime += executionTime;
            
            // Actualizar tiempo restante
            remainingTime.set(currentIndex, remainingTime.get(currentIndex) - executionTime);
            
            // Agregar nuevos procesos que han llegado durante esta ejecución
            while (index < n && processList.get(index).getArrivalTime() <= currentTime) {
                readyQueue.add(index);
                index++;
            }
            
            // Si el proceso no ha terminado, volver a colocarlo en la cola
            if (remainingTime.get(currentIndex) > 0) {
                readyQueue.add(currentIndex);
            } else {
                // Proceso terminado
                completed++;
                turnaroundTimes.set(currentIndex, currentTime - arrivalTime.get(currentIndex));
            }
        }
        
        // Calcular tiempos de espera correctamente (acumulativo)
        // En Round Robin, el waiting time ya se calculó durante la ejecución
        // Pero ajustemos para procesos que esperaron múltiples veces
        calculateFinalWaitingTimes(processList, waitingTimes, turnaroundTimes);
        
        // Calcular promedios
        double avgWait = waitingTimes.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        double avgTurnaround = turnaroundTimes.stream()
            .mapToInt(Integer::intValue)
            .average()
            .orElse(0.0);
        
        // Crear resultado
        SchedulerResult result = new SchedulerResult();
        result.setAlgorithm("Round Robin (Quantum=" + quantum + ")");
        result.setExecutionOrder(executionOrder);
        result.setWaitingTimes(waitingTimes);
        result.setTurnaroundTimes(turnaroundTimes);
        result.setAvgWaitingTime(avgWait);
        result.setAvgTurnaroundTime(avgTurnaround);
        
        return result;
    }
    
    private void calculateFinalWaitingTimes(List<Process> processes,List<Integer> waitingTimes,List<Integer> turnaroundTimes) {
        // Waiting Time = Turnaround Time - Burst Time
        for (int i = 0; i < processes.size(); i++) {
            int burstTime = processes.get(i).getBurstTime();
            int turnaround = turnaroundTimes.get(i);
            waitingTimes.set(i, turnaround - burstTime);
        }
    }
    
    // Método para obtener el quantum por defecto (útil para la UI)
    public int getDefaultQuantum() {
        return DEFAULT_QUANTUM;
    }
}