package com.example.Back_simulator.Controller;

import com.example.Back_simulator.model.Process;
import com.example.Back_simulator.model.SchedulerResult;
import com.example.Back_simulator.Scheduler.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/scheduler")
@CrossOrigin(origins = "http://localhost:5173")
public class SchedulerController {
    
    @Autowired private FCFSScheduler fcfsScheduler;
    @Autowired private SJFScheduler sjfScheduler;
    @Autowired private RoundRobinScheduler rrScheduler;
    
    @PostMapping("/fcfs")
    public SchedulerResult fcfs(@RequestBody List<Process> processes) {
        return fcfsScheduler.schedule(processes);
    }
    
    @PostMapping("/sjf")
    public SchedulerResult sjf(@RequestBody List<Process> processes) {
        return sjfScheduler.schedule(processes);
    }
    
    @PostMapping("/rr")
    public SchedulerResult rr(@RequestBody List<Process> processes, @RequestParam(defaultValue = "2") int quantum) {
        return rrScheduler.schedule(processes, quantum);
    }
    
    // Endpoint adicional para obtener el quantum por defecto
    @GetMapping("/rr/default-quantum")
    public int getDefaultQuantum() {
        return rrScheduler.getDefaultQuantum();
    }
}