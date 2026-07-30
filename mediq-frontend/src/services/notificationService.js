import api from './api';

export const notificationService = {
  sendRegistrationSms: async (patientId, campId) =>
    api.post('/notifications/send-registration-sms', null, { params: { patientId, campId } }),

  sendPrescriptionSms: async (prescriptionId) =>
    api.post(`/notifications/send-prescription-sms/${prescriptionId}`),

  sendCustomSms: async (data) =>
    api.post('/notifications/send-custom-sms', data),

  sendWhatsApp: async (data) =>
    api.post('/notifications/send-whatsapp', data),
};

export default notificationService;
