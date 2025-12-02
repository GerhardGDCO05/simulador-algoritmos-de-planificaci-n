<!-- frontend/src/components/CPUScheduler.vue -->
<template>
  <div class="scheduler-container">
    <h2>🧠 Simulador de Algoritmos de Planificación de CPU</h2>
    
    <!-- Sección 1: Entrada de procesos -->
    <div class="section">
      <h3>📝 Procesos</h3>
      <div class="process-list">
        <div v-for="(process, index) in processes" :key="index" class="process-item">
          <div class="process-header">
            <span>Proceso {{ index + 1 }}</span>
            <button @click="removeProcess(index)" class="btn-remove">❌ Eliminar</button>
          </div>
          <div class="process-fields">
            <div class="field">
              <label>ID:</label>
              <input v-model.number="process.id" type="number" min="0" disabled>
            </div>
            <div class="field">
              <label>Tiempo de Llegada:</label>
              <input v-model.number="process.arrivalTime" type="number" min="0">
            </div>
            <div class="field">
              <label>Ráfaga CPU:</label>
              <input v-model.number="process.burstTime" type="number" min="1">
            </div>
            <div class="field">
              <label>Prioridad:</label>
              <input v-model.number="process.priority" type="number" min="1">
            </div>
          </div>
        </div>
      </div>
      
      <button @click="addProcess" class="btn-add">➕ Agregar Proceso</button>
      <button @click="addSampleData" class="btn-sample">📋 Datos de Ejemplo</button>
    </div>
    
    <!-- Sección 2: Configuración del algoritmo -->
    <div class="section">
      <h3>⚙️ Configuración del Algoritmo</h3>
      <div class="algorithm-config">
        <div class="field">
          <label>Algoritmo:</label>
          <select v-model="selectedAlgorithm" @change="onAlgorithmChange">
            <option value="fcfs">FCFS (First-Come, First-Served)</option>
            <option value="sjf">SJF (Shortest Job First)</option>
            <option value="rr">Round Robin</option>
          </select>
        </div>
        
        <div v-if="selectedAlgorithm === 'rr'" class="field">
          <label>Quantum:</label>
          <input v-model.number="quantum" type="number" min="1" max="10">
        </div>
        
        <button @click="runSimulation" :disabled="loading || processes.length === 0" class="btn-run">
          {{ loading ? '⏳ Ejecutando...' : '🚀 Ejecutar Simulación' }}
        </button>
      </div>
    </div>
    
    <!-- Sección 3: Resultados -->
    <div v-if="result" class="section results">
      <h3>📊 Resultados - {{ result.algorithm }}</h3>
      
      <!-- Tabla de procesos con resultados -->
      <table class="results-table">
        <thead>
          <tr>
            <th>ID</th>
            <th>Llegada</th>
            <th>Ráfaga</th>
            <th>Prioridad</th>
            <th>Tiempo Espera</th>
            <th>Tiempo Retorno</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(process, index) in processes" :key="index">
            <td>{{ process.id }}</td>
            <td>{{ process.arrivalTime }}</td>
            <td>{{ process.burstTime }}</td>
            <td>{{ process.priority }}</td>
            <td>{{ result.waitingTimes[index] }}</td>
            <td>{{ result.turnaroundTimes[index] }}</td>
          </tr>
        </tbody>
      </table>
      
      <!-- Métricas -->
      <div class="metrics">
        <div class="metric">
          <span class="metric-label">Orden de Ejecución:</span>
          <span class="metric-value">{{ result.executionOrder.join(' → ') }}</span>
        </div>
        <div class="metric">
          <span class="metric-label">Tiempo Espera Promedio:</span>
          <span class="metric-value">{{ result.avgWaitingTime.toFixed(2) }}</span>
        </div>
        <div class="metric">
          <span class="metric-label">Tiempo Retorno Promedio:</span>
          <span class="metric-value">{{ result.avgTurnaroundTime.toFixed(2) }}</span>
        </div>
      </div>
      
      <!-- Diagrama de Gantt simple -->
      <div v-if="result.executionOrder.length > 0" class="gantt-container">
        <h4>📈 Diagrama de Gantt</h4>
        <div class="gantt-chart">
          <div v-for="(processId, index) in result.executionOrder" 
               :key="index" 
               class="gantt-block"
               :style="{ width: '50px' }">
            <div class="gantt-process">P{{ processId }}</div>
            <div class="gantt-time">{{ index }}</div>
          </div>
        </div>
      </div>
    </div>
    
    <!-- Sección 4: Debug/Estado -->
    <div class="section debug">
      <h3>🔍 Estado del Sistema</h3>
      <div class="status">
        <p>✅ Backend: {{ backendStatus }}</p>
        <p>🔄 Procesos: {{ processes.length }}</p>
        <p>📡 Última respuesta: {{ lastResponseTime }}</p>
      </div>
      <button @click="testBackendConnection" class="btn-test">🔗 Probar Conexión Backend</button>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { schedulerAPI } from '../Services/api';

// Datos reactivos
const processes = ref([
  { id: 0, arrivalTime: 0, burstTime: 5, priority: 1 },
  { id: 1, arrivalTime: 1, burstTime: 3, priority: 2 },
  { id: 2, arrivalTime: 2, burstTime: 8, priority: 3 }
]);

const selectedAlgorithm = ref('fcfs');
const quantum = ref(2);
const result = ref(null);
const loading = ref(false);
const backendStatus = ref('Desconocido');
const lastResponseTime = ref('N/A');

// Métodos
const addProcess = () => {
  const newId = processes.value.length;
  processes.value.push({
    id: newId,
    arrivalTime: 0,
    burstTime: 1,
    priority: 1
  });
};

const removeProcess = (index) => {
  if (processes.value.length > 1) {
    processes.value.splice(index, 1);
    // Reasignar IDs
    processes.value.forEach((p, i) => p.id = i);
  }
};

const addSampleData = () => {
  processes.value = [
    { id: 0, arrivalTime: 0, burstTime: 5, priority: 1 },
    { id: 1, arrivalTime: 1, burstTime: 3, priority: 2 },
    { id: 2, arrivalTime: 2, burstTime: 8, priority: 3 },
    { id: 3, arrivalTime: 3, burstTime: 6, priority: 1 },
    { id: 4, arrivalTime: 4, burstTime: 4, priority: 2 }
  ];
};

const onAlgorithmChange = () => {
  result.value = null; // Limpiar resultados anteriores
};

const runSimulation = async () => {
  loading.value = true;
  result.value = null;
  lastResponseTime.value = 'Enviando...';
  
  try {
    console.log('Iniciando simulación con algoritmo:', selectedAlgorithm.value);
    console.log('Procesos a enviar:', JSON.stringify(processes.value));
    
    let response;
    const startTime = Date.now();
    
    switch (selectedAlgorithm.value) {
      case 'fcfs':
        response = await schedulerAPI.fcfs(processes.value);
        break;
      case 'sjf':
        response = await schedulerAPI.sjf(processes.value);
        break;
      case 'rr':
        response = await schedulerAPI.rr(processes.value, quantum.value);
        break;
    }
    
    const endTime = Date.now();
    lastResponseTime.value = `${endTime - startTime}ms`;
    
    result.value = response.data;
    console.log('Resultado recibido:', result.value);
    
    backendStatus.value = '✅ Conectado';
    
  } catch (error) {
    console.error('Error en simulación:', error);
    backendStatus.value = '❌ Error de conexión';
    lastResponseTime.value = 'Error';
    
    alert(`Error: ${error.response?.data?.message || error.message}`);
  } finally {
    loading.value = false;
  }
};

const testBackendConnection = async () => {
  try {
    const response = await schedulerAPI.health();
    backendStatus.value = `✅ ${response.data}`;
    console.log('Health check exitoso:', response.data);
  } catch (error) {
    backendStatus.value = '❌ Backend no disponible';
    console.error('Health check falló:', error);
  }
};

// Al cargar el componente
onMounted(() => {
  testBackendConnection();
});
</script>

