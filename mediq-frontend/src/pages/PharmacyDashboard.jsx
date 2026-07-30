import React, { useState, useEffect } from 'react';
import pharmacyService from '../services/pharmacyService';
import prescriptionService from '../services/prescriptionService';
import campService from '../services/campService';
import notificationService from '../services/notificationService';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import { Pill, AlertTriangle, Plus, CheckCircle, Package, Download, Search, Check, AlertCircle, Smartphone, MessageSquare } from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const PharmacyDashboard = () => {
  const [inventory, setInventory] = useState([]);
  const [lowStockAlerts, setLowStockAlerts] = useState([]);
  const [prescriptions, setPrescriptions] = useState([]);
  const [camps, setCamps] = useState([]);
  const [selectedCampId, setSelectedCampId] = useState('');
  const [loading, setLoading] = useState(true);

  // Modals
  const [isAddMedicineOpen, setIsAddMedicineOpen] = useState(false);
  const [isDispenseModalOpen, setIsDispenseModalOpen] = useState(false);
  const [selectedPrescription, setSelectedPrescription] = useState(null);
  const [dispenseItems, setDispenseItems] = useState([]);
  const [dispenseRemarks, setDispenseRemarks] = useState('Dispensed at camp pharmacy counter');
  const [dispensingLoading, setDispensingLoading] = useState(false);

  // New Medicine Form
  const [newMedicine, setNewMedicine] = useState({
    medicineCode: 'MED-NEW-001',
    name: 'Metformin 500mg',
    category: 'Anti-Diabetic',
    batchNumber: 'BCH-2026-08',
    manufacturer: 'Sun Pharma',
    expiryDate: '2028-12-31',
    stockQuantity: 200,
    minAlertQuantity: 30,
    unitPrice: 1.5,
  });

  useEffect(() => {
    fetchCamps();
    fetchInventory();
  }, []);

  useEffect(() => {
    if (selectedCampId) {
      fetchPrescriptions();
    }
  }, [selectedCampId]);

  const fetchCamps = async () => {
    try {
      const res = await campService.searchCamps(null, '', 0, 10);
      const list = res.data?.content || res.content || res.data || [];
      setCamps(list);
      if (list.length > 0) setSelectedCampId(list[0].id);
    } catch (e) {
      console.error(e);
    }
  };

  const fetchInventory = async () => {
    setLoading(true);
    try {
      const res = await pharmacyService.getAllMedicines();
      const invList = res.data || res;
      setInventory(Array.isArray(invList) ? invList : []);
      const alertRes = await pharmacyService.getLowStockAlerts();
      const lowList = alertRes.data || alertRes;
      setLowStockAlerts(Array.isArray(lowList) ? lowList : []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchPrescriptions = async () => {
    try {
      const res = await prescriptionService.getPrescriptionsByStatus(selectedCampId, 'CREATED');
      const rxList = res.data || res;
      setPrescriptions(Array.isArray(rxList) ? rxList : []);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAddMedicine = async (e) => {
    e.preventDefault();
    try {
      await pharmacyService.addMedicine(newMedicine);
      setIsAddMedicineOpen(false);
      fetchInventory();
      alert('Medicine added to inventory successfully!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to add medicine');
    }
  };

  const handleDownloadPdf = async (prescription) => {
    try {
      const res = await prescriptionService.downloadPrescriptionPdf(prescription.id);
      const rawData = res.data || res;
      const blob = rawData instanceof Blob ? rawData : new Blob([rawData], { type: 'application/pdf' });
      const url = window.URL.createObjectURL(blob);
      const link = document.createElement('a');
      link.href = url;
      link.setAttribute('download', `Prescription-${prescription.prescriptionCode || prescription.id}.pdf`);
      document.body.appendChild(link);
      link.click();
      link.remove();
      setTimeout(() => window.URL.revokeObjectURL(url), 100);
    } catch (e) {
      alert(e?.message || 'Failed to download prescription PDF');
    }
  };

  const sanitizePhone = (rawPhone) => {
    if (!rawPhone) return '919876543210';
    let digits = rawPhone.replace(/[^0-9]/g, '');
    if (digits.length === 10) {
      digits = '91' + digits; // Default India country code 91
    }
    return digits;
  };

  const handleSendPrescriptionSms = async (prescription) => {
    try {
      const res = await notificationService.sendPrescriptionSms(prescription.id);
      const smsData = res.data || res;
      const messageBody = smsData.messageBody || 'Prescription details sent!';
      const rawPhone = smsData.recipientPhone || prescription.patient?.phone;
      const cleanPhone = sanitizePhone(rawPhone);

      // Launch native Cellular SMS app pre-filled for target phone number
      window.open(`sms:+${cleanPhone}?body=${encodeURIComponent(messageBody)}`, '_self');
    } catch (e) {
      alert(e?.message || 'Failed to dispatch prescription SMS');
    }
  };

  const handleSendPrescriptionWhatsApp = async (prescription) => {
    try {
      const res = await notificationService.sendPrescriptionSms(prescription.id);
      const smsData = res.data || res;
      const messageBody = smsData.messageBody || 'Prescription details sent!';
      const rawPhone = smsData.recipientPhone || prescription.patient?.phone;
      const cleanPhone = sanitizePhone(rawPhone);

      try {
        await notificationService.sendWhatsApp({
          phoneNumber: cleanPhone,
          customMessage: messageBody,
          notificationType: 'WHATSAPP_PRESCRIPTION',
        });
      } catch (err) {
        console.error('Backend WhatsApp gateway skipped', err);
      }

      window.open(`https://api.whatsapp.com/send?phone=${cleanPhone}&text=${encodeURIComponent(messageBody)}`, '_blank');
    } catch (e) {
      alert(e?.message || 'Failed to dispatch prescription WhatsApp message');
    }
  };

  const openDispenseModal = (prescription) => {
    setSelectedPrescription(prescription);
    const preparedItems = prescription.items?.map((item) => {
      const remainingQty = item.quantityPrescribed - (item.quantityDispensed || 0);
      const matchedInventory = inventory.find(
        (inv) => inv.name.toLowerCase().trim() === item.medicineName.toLowerCase().trim()
      );
      return {
        prescriptionItemId: item.id,
        medicineName: item.medicineName,
        dosage: item.dosage,
        frequency: item.frequency,
        quantityPrescribed: item.quantityPrescribed,
        quantityAlreadyDispensed: item.quantityDispensed || 0,
        quantityToDispense: Math.max(1, remainingQty),
        inventoryMatch: matchedInventory || null,
        availableStock: matchedInventory ? matchedInventory.stockQuantity : 0,
      };
    }) || [];
    setDispenseItems(preparedItems);
    setDispenseRemarks('Dispensed at camp pharmacy counter');
    setIsDispenseModalOpen(true);
  };

  const handleConfirmDispense = async (e) => {
    e.preventDefault();
    if (!selectedPrescription) return;

    setDispensingLoading(true);
    try {
      const itemsPayload = dispenseItems.map((item) => ({
        prescriptionItemId: item.prescriptionItemId,
        medicineId: item.inventoryMatch?.id || null,
        quantityToDispense: Number(item.quantityToDispense),
        remarks: dispenseRemarks,
      }));

      await pharmacyService.dispenseMedicines({
        prescriptionId: selectedPrescription.id,
        items: itemsPayload,
      });

      setIsDispenseModalOpen(false);
      setSelectedPrescription(null);
      fetchPrescriptions();
      fetchInventory();
      alert('Medicines dispensed successfully and inventory stock updated!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to dispense medicines');
    } finally {
      setDispensingLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">Active Pharmacy Station</h2>
          <p className="text-slate-400 text-xs">Switch medical camp location & manage inventory</p>
        </div>

        <div className="flex items-center space-x-3">
          <select
            value={selectedCampId}
            onChange={(e) => setSelectedCampId(e.target.value)}
            className="px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 shadow-xs focus:outline-none focus:ring-2 focus:ring-indigo-500/20"
          >
            {camps.map((c) => (
              <option key={c.id} value={c.id}>
                {c.title} ({c.campCode})
              </option>
            ))}
          </select>

          <button
            onClick={() => setIsAddMedicineOpen(true)}
            className="px-4 py-2 bg-gradient-to-r from-indigo-600 to-purple-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md shadow-indigo-600/20 flex items-center space-x-2 transition-all"
          >
            <Plus className="w-4 h-4" />
            <span>Add Medicine Stock</span>
          </button>
        </div>
      </div>

      {/* Hero Animated Banner */}
      <AnimatedHeroBanner
        type="pharmacy"
        stats={[
          { label: 'Pending Prescriptions', value: `${prescriptions.length} Orders` },
          { label: 'Low Stock Alerts', value: `${lowStockAlerts.length} Items` }
        ]}
      />

      {/* Low Stock Banner */}
      {lowStockAlerts.length > 0 && (
        <div className="bg-amber-50 border border-amber-200 p-4 rounded-2xl flex items-center justify-between">
          <div className="flex items-center space-x-3">
            <AlertTriangle className="w-6 h-6 text-amber-600" />
            <div>
              <div className="text-sm font-bold text-amber-900">Low Stock Alert ({lowStockAlerts.length} Medicines)</div>
              <div className="text-xs text-amber-700">The following medicines are below alert thresholds: {lowStockAlerts.map(m => m.name).join(', ')}</div>
            </div>
          </div>
        </div>
      )}

      {/* Pending Digital Prescriptions Grid */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-4">
        <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
          <Pill className="w-5 h-5 text-teal-600" />
          <span>Pending Prescriptions Queue ({prescriptions.length})</span>
        </h2>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
          {prescriptions.map((p) => (
            <div key={p.id} className="border border-slate-200 rounded-2xl p-4 space-y-3 bg-slate-50/50 hover:bg-white transition-all">
              <div className="flex justify-between items-start">
                <div>
                  <span className="font-mono font-bold text-xs text-teal-700 bg-teal-50 px-2 py-0.5 rounded-md border border-teal-100">
                    {p.prescriptionCode}
                  </span>
                  <h4 className="font-bold text-sm text-slate-800 mt-1">{p.patient?.fullName}</h4>
                  <div className="text-xs text-slate-500">Patient ID: {p.patient?.patientId}</div>
                </div>
                <StatusBadge status={p.status} />
              </div>

              {/* Medicines List */}
              <div className="space-y-1.5 bg-white p-3 rounded-xl border border-slate-100 text-xs">
                {p.items?.map((item) => (
                  <div key={item.id} className="flex justify-between items-center text-slate-700">
                    <div>
                      <span className="font-bold text-slate-800">{item.medicineName}</span>
                      <span className="text-slate-400 text-[11px] ml-2">({item.dosage})</span>
                    </div>
                    <span className="font-mono font-semibold bg-slate-100 px-2 py-0.5 rounded text-slate-700">
                      Qty: {item.quantityPrescribed}
                    </span>
                  </div>
                ))}
              </div>

              {/* Actions Footer */}
              <div className="flex flex-wrap items-center justify-between gap-2 pt-2">
                <div className="flex items-center space-x-2">
                  <button
                    onClick={() => handleDownloadPdf(p)}
                    className="px-3 py-1.5 text-xs font-bold text-teal-700 bg-teal-50 border border-teal-200 rounded-xl hover:bg-teal-100 transition-all flex items-center space-x-1.5"
                  >
                    <Download className="w-3.5 h-3.5 text-teal-600" />
                    <span>PDF</span>
                  </button>

                  <button
                    onClick={() => handleSendPrescriptionSms(p)}
                    className="px-3 py-1.5 text-xs font-bold text-amber-800 bg-amber-50 border border-amber-200 rounded-xl hover:bg-amber-100 transition-all flex items-center space-x-1.5"
                    title="Send plain-text SMS for keypad / feature phone users"
                  >
                    <Smartphone className="w-3.5 h-3.5 text-amber-600" />
                    <span>📱 Keypad SMS</span>
                  </button>

                  <button
                    onClick={() => handleSendPrescriptionWhatsApp(p)}
                    className="px-3 py-1.5 text-xs font-bold text-emerald-800 bg-emerald-50 border border-emerald-200 rounded-xl hover:bg-emerald-100 transition-all flex items-center space-x-1.5"
                    title="Send prescription via WhatsApp Direct API"
                  >
                    <MessageSquare className="w-3.5 h-3.5 text-emerald-600" />
                    <span>🟢 WhatsApp</span>
                  </button>
                </div>

                <button
                  onClick={() => openDispenseModal(p)}
                  className="px-4 py-1.5 bg-emerald-600 hover:bg-emerald-700 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-600/20 transition-all flex items-center space-x-1.5"
                >
                  <CheckCircle className="w-3.5 h-3.5" />
                  <span>Dispense Medicines</span>
                </button>
              </div>
            </div>
          ))}
          {prescriptions.length === 0 && (
            <div className="col-span-2 text-center py-8 text-slate-400 text-xs bg-slate-50/50 rounded-2xl border border-dashed border-slate-200">
              No pending prescriptions waiting in pharmacy queue
            </div>
          )}
        </div>
      </div>

      {/* Stock Inventory Directory Table */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center">
          <div>
            <h2 className="text-lg font-bold text-slate-800">Medicine Stock Inventory</h2>
            <p className="text-xs text-slate-400">Live inventory levels & stock status across pharmacy counter</p>
          </div>
          <span className="text-xs font-bold font-mono text-indigo-600 bg-indigo-50 px-3 py-1 rounded-xl">
            {inventory.length} Medicines Total
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-6">Code</th>
                <th className="py-3 px-6">Medicine Name</th>
                <th className="py-3 px-6">Category</th>
                <th className="py-3 px-6">Batch No</th>
                <th className="py-3 px-6">Expiry Date</th>
                <th className="py-3 px-6">Stock Qty</th>
                <th className="py-3 px-6">Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {inventory.map((m) => (
                <tr key={m.id} className="hover:bg-slate-50/50 transition-colors">
                  <td className="py-4 px-6 font-mono font-bold text-teal-700">{m.medicineCode}</td>
                  <td className="py-4 px-6 font-bold text-slate-800">{m.name}</td>
                  <td className="py-4 px-6 text-slate-600">{m.category}</td>
                  <td className="py-4 px-6 text-slate-600 font-mono text-xs">{m.batchNumber}</td>
                  <td className="py-4 px-6 text-slate-600 text-xs">{m.expiryDate}</td>
                  <td className="py-4 px-6 font-black text-slate-800">{m.stockQuantity}</td>
                  <td className="py-4 px-6">
                    <StatusBadge status={m.isLowStock || m.stockQuantity <= m.minAlertQuantity ? 'CRITICAL' : 'COMPLETED'} />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>

      {/* Interactive Dispense Modal */}
      <Modal
        isOpen={isDispenseModalOpen}
        onClose={() => setIsDispenseModalOpen(false)}
        title={`Dispense Prescription - ${selectedPrescription?.prescriptionCode || ''}`}
      >
        {selectedPrescription && (
          <form onSubmit={handleConfirmDispense} className="space-y-5">
            <div className="bg-slate-50 p-3.5 rounded-xl border border-slate-200 text-xs flex justify-between items-center">
              <div>
                <span className="font-bold text-slate-800 text-sm block">{selectedPrescription.patient?.fullName}</span>
                <span className="text-slate-500 font-mono">Patient ID: {selectedPrescription.patient?.patientId}</span>
              </div>
              <StatusBadge status={selectedPrescription.status} />
            </div>

            <div className="space-y-3">
              <h4 className="text-xs font-bold uppercase tracking-wider text-slate-500">Prescribed Medicine Items & Live Stock Check</h4>
              {dispenseItems.map((item, idx) => (
                <div key={idx} className="bg-white p-3 rounded-xl border border-slate-200 space-y-2">
                  <div className="flex justify-between items-start">
                    <div>
                      <span className="font-bold text-sm text-slate-800">{item.medicineName}</span>
                      <div className="text-xs text-slate-500">{item.dosage} | {item.frequency}</div>
                    </div>
                    {item.inventoryMatch ? (
                      <span
                        className={`text-xs font-bold px-2.5 py-1 rounded-lg flex items-center space-x-1 ${
                          item.availableStock >= item.quantityToDispense
                            ? 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                            : 'bg-rose-50 text-rose-700 border border-rose-200'
                        }`}
                      >
                        {item.availableStock >= item.quantityToDispense ? (
                          <Check className="w-3.5 h-3.5 text-emerald-600" />
                        ) : (
                          <AlertCircle className="w-3.5 h-3.5 text-rose-600" />
                        )}
                        <span>Stock: {item.availableStock} Available</span>
                      </span>
                    ) : (
                      <span className="text-xs font-bold px-2 py-1 bg-amber-50 text-amber-700 border border-amber-200 rounded-lg">
                        Not linked in Inventory
                      </span>
                    )}
                  </div>

                  <div className="grid grid-cols-2 gap-3 pt-1 text-xs">
                    <div>
                      <label className="block text-[11px] font-bold text-slate-500 mb-1">Prescribed Qty</label>
                      <input
                        type="number"
                        disabled
                        value={item.quantityPrescribed}
                        className="w-full px-2.5 py-1.5 bg-slate-100 border border-slate-200 rounded-lg font-bold text-slate-700"
                      />
                    </div>
                    <div>
                      <label className="block text-[11px] font-bold text-slate-700 mb-1">Qty to Dispense Now</label>
                      <input
                        type="number"
                        min="1"
                        max={item.availableStock > 0 ? item.availableStock : item.quantityPrescribed}
                        required
                        value={item.quantityToDispense}
                        onChange={(e) => {
                          const list = [...dispenseItems];
                          list[idx].quantityToDispense = parseInt(e.target.value) || 1;
                          setDispenseItems(list);
                        }}
                        className="w-full px-2.5 py-1.5 border border-slate-300 rounded-lg font-bold text-slate-800 focus:ring-2 focus:ring-emerald-500/20"
                      />
                    </div>
                  </div>
                </div>
              ))}
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Dispensing Remarks</label>
              <input
                type="text"
                value={dispenseRemarks}
                onChange={(e) => setDispenseRemarks(e.target.value)}
                className="w-full px-3 py-2 text-xs border border-slate-200 rounded-xl"
              />
            </div>

            <div className="flex justify-end space-x-3 pt-2">
              <button
                type="button"
                onClick={() => setIsDispenseModalOpen(false)}
                className="px-4 py-2 text-xs font-bold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-xl"
              >
                Cancel
              </button>
              <button
                type="submit"
                disabled={dispensingLoading}
                className="px-5 py-2 text-xs font-bold text-white bg-gradient-to-r from-emerald-600 to-teal-600 hover:opacity-95 rounded-xl shadow-md disabled:opacity-50 flex items-center space-x-1.5"
              >
                <CheckCircle className="w-4 h-4" />
                <span>{dispensingLoading ? 'Dispensing...' : 'Confirm & Deduct Inventory Stock'}</span>
              </button>
            </div>
          </form>
        )}
      </Modal>

      {/* Add Medicine Modal */}
      <Modal isOpen={isAddMedicineOpen} onClose={() => setIsAddMedicineOpen(false)} title="Add Medicine Stock to Inventory">
        <form onSubmit={handleAddMedicine} className="space-y-4">
          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Medicine Code</label>
              <input
                type="text"
                required
                value={newMedicine.medicineCode}
                onChange={(e) => setNewMedicine({ ...newMedicine, medicineCode: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Medicine Name</label>
              <input
                type="text"
                required
                value={newMedicine.name}
                onChange={(e) => setNewMedicine({ ...newMedicine, name: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Category</label>
              <input
                type="text"
                value={newMedicine.category}
                onChange={(e) => setNewMedicine({ ...newMedicine, category: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Batch Number</label>
              <input
                type="text"
                value={newMedicine.batchNumber}
                onChange={(e) => setNewMedicine({ ...newMedicine, batchNumber: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Stock Quantity</label>
              <input
                type="number"
                required
                value={newMedicine.stockQuantity}
                onChange={(e) => setNewMedicine({ ...newMedicine, stockQuantity: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Min Alert Qty</label>
              <input
                type="number"
                value={newMedicine.minAlertQuantity}
                onChange={(e) => setNewMedicine({ ...newMedicine, minAlertQuantity: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Expiry Date</label>
              <input
                type="date"
                required
                value={newMedicine.expiryDate}
                onChange={(e) => setNewMedicine({ ...newMedicine, expiryDate: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-3 bg-teal-600 text-white font-bold rounded-xl shadow-md hover:bg-teal-700 mt-2"
          >
            Save Medicine to Inventory
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default PharmacyDashboard;
