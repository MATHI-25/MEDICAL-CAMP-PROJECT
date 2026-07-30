import api from './api';

export const pharmacyService = {
  addMedicine: async (data) => api.post('/pharmacy/inventory', data),
  updateStock: async (id, addedQuantity) => api.patch(`/pharmacy/inventory/${id}/restock`, { addedQuantity }),
  getMedicineById: async (id) => api.get(`/pharmacy/inventory/${id}`),
  getAllMedicines: async () => api.get('/pharmacy/inventory'),
  searchMedicines: async (keyword) => api.get('/pharmacy/inventory/search', { params: { keyword } }),
  getLowStockAlerts: async () => api.get('/pharmacy/inventory/low-stock'),
  dispenseMedicines: async (data) => api.post('/pharmacy/dispense', data),
  getDispenseRecordsForPrescription: async (prescriptionId) => api.get(`/pharmacy/dispense/prescription/${prescriptionId}`),
  getPharmacistDispenseHistory: async () => api.get('/pharmacy/dispense/history'),
};

export default pharmacyService;
