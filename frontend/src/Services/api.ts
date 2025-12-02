import axios from 'axios';

// Crear instancia de axios con configuración base
const api = axios.create({
  baseURL: 'http://localhost:8080/api', // URL de tu backend
  timeout: 10000, // 10 segundos
  headers: {
    'Content-Type': 'application/json',
    'Accept': 'application/json'
  }
});

// Interceptor para logging (debug)
api.interceptors.request.use(
  config => {
    console.log('🔵 Enviando petición:', {
      method: config.method?.toUpperCase(),
      url: config.url,
      data: config.data
    });
    return config;
  },
  error => {
    console.error('🔴 Error en petición:', error);
    return Promise.reject(error);
  }
);

// Interceptor para respuestas
api.interceptors.response.use(
  response => {
    console.log('🟢 Respuesta recibida:', {
      status: response.status,
      data: response.data
    });
    return response;
  },
  error => {
    console.error('🔴 Error en respuesta:', {
      status: error.response?.status,
      message: error.message,
      data: error.response?.data
    });
    return Promise.reject(error);
  }
);

// Métodos específicos para el simulador
export const schedulerAPI = {
  // FCFS
  fcfs: (processes:any) => {
    console.log('📤 Enviando procesos FCFS:', processes);
    return api.post('/scheduler/fcfs', processes);
  },
  
  // SJF
  sjf: (processes:any) => {
    console.log('📤 Enviando procesos SJF:', processes);
    return api.post('/scheduler/sjf', processes);
  },
  
  // Round Robin
  rr: (processes:any, quantum = 2) => {
    console.log('📤 Enviando procesos RR, quantum:', quantum);
    return api.post(`/scheduler/rr?quantum=${quantum}`, processes);
  },
  
  // Health check
  health: () => api.get('/scheduler/health')
};

export default api;