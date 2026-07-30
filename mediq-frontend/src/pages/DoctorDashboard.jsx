import React, { useState, useEffect } from 'react';
import queueService from '../services/queueService';
import nurseService from '../services/nurseService';
import doctorService from '../services/doctorService';
import prescriptionService from '../services/prescriptionService';
import referralService from '../services/referralService';
import patientService from '../services/patientService';
import campService from '../services/campService';
import pharmacyService from '../services/pharmacyService';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import { useAuth } from '../context/AuthContext';
import {
  Stethoscope,
  FileText,
  Pill,
  Share2,
  History,
  CheckCircle,
  Download,
  Search,
  Plus,
  Trash2,
  AlertTriangle,
  Check,
  PackageCheck
} from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const DoctorDashboard = () => {
  const { user } = useAuth();
  const [queue, setQueue] = useState([]);
  const [camps, setCamps] = useState([]);
  const [selectedCampId, setSelectedCampId] = useState('');
  const [activeToken, setActiveToken] = useState(null);

  // Live Pharmacy Inventory State
  const [pharmacyStock, setPharmacyStock] = useState([]);
  const [stockSearchKeyword, setStockSearchKeyword] = useState('');

  // Patient Vitals & History
  const [patientVitals, setPatientVitals] = useState(null);
  const [medicalTimeline, setMedicalTimeline] = useState([]);

  // Forms & Decision
  const [submittingLoading, setSubmittingLoading] = useState(false);
  const [requiresReferral, setRequiresReferral] = useState(false);

  const [consultationForm, setConsultationForm] = useState({
    diseaseName: 'Upper Respiratory Tract Infection',
    diagnosisNotes: 'Patient presents with mild fever, sore throat, and nasal congestion.',
    labTestRecommendations: 'Complete Blood Count (CBC) if symptoms persist.',
    doctorNotes: 'Advised rest, warm hydration, and prescribed antibiotic course.',
  });

  // Digital Prescription Items
  const [medicines, setMedicines] = useState([
    { medicineName: 'Amoxicillin 500mg', dosage: '500mg', frequency: '1-0-1 After Meals', duration: '5 Days', instructions: 'Take with full glass of water', quantityPrescribed: 10 },
    { medicineName: 'Paracetamol 500mg', dosage: '500mg', frequency: '1-1-1 As Needed', duration: '3 Days', instructions: 'For fever or mild pain', quantityPrescribed: 9 },
  ]);

  // Referral Details
  const [referralForm, setReferralForm] = useState({
    hospitalName: 'City General Government Hospital',
    hospitalAddress: '100 Healthcare Boulevard, Central District',
    department: 'Pulmonology / Internal Medicine',
    specialistType: 'Pulmonologist',
    reason: 'Advanced diagnostic workup and chest X-ray evaluation required.',
    urgency: 'NORMAL',
  });

  useEffect(() => {
    fetchCamps();
    fetchPharmacyStock();
  }, []);

  useEffect(() => {
    const docId = user?.userId || user?.id;
    if (selectedCampId && docId) {
      fetchDoctorQueue();
    }
  }, [selectedCampId, user?.userId, user?.id]);

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

  const fetchPharmacyStock = async () => {
    try {
      const res = await pharmacyService.getAllMedicines();
      const list = res.data || res;
      setPharmacyStock(Array.isArray(list) ? list : []);
    } catch (e) {
      console.error('Failed to fetch pharmacy inventory stock', e);
    }
  };

  const fetchDoctorQueue = async () => {
    try {
      const docId = user?.userId || user?.id;
      const res = await queueService.getDoctorQueue(selectedCampId, docId);
      const list = res.data || res;
      const qList = Array.isArray(list) ? list : [];
      setQueue(qList);
      if (qList.length > 0 && !activeToken) {
        selectPatientForConsultation(qList[0]);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const selectPatientForConsultation = async (token) => {
    setActiveToken(token);
    setPatientVitals(null);
    setRequiresReferral(false);
    setConsultationForm({
      diseaseName: 'Upper Respiratory Tract Infection',
      diagnosisNotes: 'Patient presents with mild fever, sore throat, and nasal congestion.',
      labTestRecommendations: 'Complete Blood Count (CBC) if symptoms persist.',
      doctorNotes: 'Advised rest, warm hydration, and prescribed antibiotic course.',
    });
    setReferralForm({
      hospitalName: 'City General Government Hospital',
      hospitalAddress: '100 Healthcare Boulevard, Central District',
      department: 'Pulmonology / Internal Medicine',
      specialistType: 'Pulmonologist',
      reason: 'Advanced diagnostic workup and chest X-ray evaluation required.',
      urgency: 'NORMAL',
    });
    setMedicines([
      { medicineName: 'Amoxicillin 500mg', dosage: '500mg', frequency: '1-0-1 After Meals', duration: '5 Days', instructions: 'Take with full glass of water', quantityPrescribed: 10 },
      { medicineName: 'Paracetamol 500mg', dosage: '500mg', frequency: '1-1-1 As Needed', duration: '3 Days', instructions: 'For fever or mild pain', quantityPrescribed: 9 },
    ]);

    try {
      // 1. Fetch Recorded Vitals by queue token ID
      try {
        const vRes = await nurseService.getVitalsByTokenId(token.id);
        const vData = vRes.data || vRes;
        if (vData && (vData.id || vData.bloodPressure || vData.bpSystolic)) {
          setPatientVitals(vData);
        }
      } catch (err) {
        // 2. Fallback to latest vitals in patient history if token vitals not found
        try {
          const hRes = await nurseService.getPatientVitalsHistory(token.patient.id);
          const hList = hRes.data || hRes;
          if (Array.isArray(hList) && hList.length > 0) {
            setPatientVitals(hList[0]);
          }
        } catch (err2) {
          console.error(err2);
        }
      }

      // Fetch Medical History Timeline
      const tRes = await patientService.getPatientTimeline(token.patient.id);
      const tData = tRes.data || tRes;
      setMedicalTimeline(Array.isArray(tData) ? tData : []);
    } catch (e) {
      console.error(e);
    }
  };

  const handleAddMedicineFromStock = (stockItem) => {
    setMedicines((prev) => [
      ...prev,
      {
        medicineName: stockItem.name,
        dosage: '500mg',
        frequency: '1-0-1 After Meals',
        duration: '5 Days',
        instructions: 'Take as directed',
        quantityPrescribed: Math.min(10, stockItem.stockQuantity || 1),
      },
    ]);
  };

  const handleAddBlankMedicineRow = () => {
    setMedicines((prev) => [
      ...prev,
      {
        medicineName: '',
        dosage: '500mg',
        frequency: '1-0-1 After Meals',
        duration: '5 Days',
        instructions: 'Take as directed',
        quantityPrescribed: 10,
      },
    ]);
  };

  const handleRemoveMedicineRow = (index) => {
    setMedicines((prev) => prev.filter((_, i) => i !== index));
  };

  const handleCompleteConsultation = async (e) => {
    e.preventDefault();
    if (!activeToken || submittingLoading) return;
    setSubmittingLoading(true);

    try {
      // 1. Save Doctor Consultation
      const cRes = await doctorService.saveConsultation({
        ...consultationForm,
        patientId: activeToken.patient.id,
        campId: selectedCampId,
        queueTokenId: activeToken.id,
        vitalsId: patientVitals?.id,
        requiresReferral,
      });

      const consultation = cRes.data || cRes;

      // 2. Decision Branching
      if (requiresReferral) {
        // Create or Update Hospital Referral
        await referralService.createReferral({
          ...referralForm,
          patientId: activeToken.patient.id,
          campId: selectedCampId,
          consultationId: consultation.id,
          doctorNotes: consultationForm.doctorNotes,
        });
        alert('Hospital Referral issued! Patient status set to REFERRED_TO_HOSPITAL.');
      } else {
        // Create Digital Prescription
        await prescriptionService.createPrescription({
          consultationId: consultation.id,
          patientId: activeToken.patient.id,
          campId: selectedCampId,
          generalInstructions: consultationForm.doctorNotes,
          items: medicines,
        });
        alert('Digital Prescription created! Sent to Pharmacy counter.');
      }

      setActiveToken(null);
      setPatientVitals(null);
      setRequiresReferral(false);
      setMedicines([]);
      await fetchDoctorQueue();
      await fetchPharmacyStock();
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to complete consultation');
    } finally {
      setSubmittingLoading(false);
    }
  };

  const filteredStock = pharmacyStock.filter(
    (m) =>
      m.name?.toLowerCase().includes(stockSearchKeyword.toLowerCase()) ||
      m.category?.toLowerCase().includes(stockSearchKeyword.toLowerCase()) ||
      m.medicineCode?.toLowerCase().includes(stockSearchKeyword.toLowerCase())
  );

  return (
    <div className="space-y-6">
      {/* Camp Selector Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">Active Camp Workspace</h2>
          <p className="text-slate-400 text-xs">Switch camp location to view assigned queue</p>
        </div>
        <select
          value={selectedCampId}
          onChange={(e) => setSelectedCampId(e.target.value)}
          className="px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 shadow-xs focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
        >
          {camps.map((c) => (
            <option key={c.id} value={c.id}>
              {c.title} ({c.campCode})
            </option>
          ))}
        </select>
      </div>

      {/* Hero Animated Banner */}
      <AnimatedHeroBanner
        type="doctor"
        stats={[
          { label: 'Queued Patients', value: `${queue.length} Pending` },
          { label: 'Current Token', value: activeToken ? activeToken.tokenNumber : 'None Active' }
        ]}
      />

      <div className="grid grid-cols-1 lg:grid-cols-4 gap-8">
        {/* Left Col: Queue List */}
        <div className="lg:col-span-1 bg-white rounded-2xl border border-slate-100 shadow-xs p-4 space-y-3">
          <h3 className="text-xs font-bold uppercase tracking-wider text-slate-400">Assigned Queue ({queue.length})</h3>
          <div className="space-y-2">
            {queue.map((item) => (
              <button
                key={item.id}
                onClick={() => selectPatientForConsultation(item)}
                className={`w-full p-3 rounded-xl border text-left transition-all ${
                  activeToken?.id === item.id
                    ? 'bg-teal-50 border-teal-500 text-teal-950 ring-2 ring-teal-500/20'
                    : 'bg-slate-50 border-slate-200 hover:bg-slate-100 text-slate-800'
                }`}
              >
                <div className="flex items-center justify-between">
                  <span className="font-mono font-black text-sm text-teal-700">{item.tokenNumber}</span>
                  <StatusBadge status={item.status} />
                </div>
                <div className="font-bold text-xs mt-1">{item.patient?.fullName}</div>
                <div className="text-[11px] text-slate-500">{item.patient?.age} Yrs / {item.patient?.gender}</div>
              </button>
            ))}
          </div>
        </div>

        {/* Right Col: Consultation Workspace */}
        <div className="lg:col-span-3 space-y-6">
          {activeToken ? (
            <div className="space-y-6">
              {/* Active Patient Vitals Bar */}
              <div className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-4">
                <div className="flex justify-between items-center border-b border-slate-100 pb-3">
                  <div>
                    <span className="text-xs font-mono font-bold text-teal-600 bg-teal-50 px-2 py-0.5 rounded-md">
                      {activeToken.patient?.patientId}
                    </span>
                    <h2 className="text-lg font-bold text-slate-800 mt-1">{activeToken.patient?.fullName}</h2>
                  </div>
                  <div className="text-right text-xs text-slate-500">
                    <div>Blood Group: <span className="font-bold text-slate-800">{activeToken.patient?.bloodGroup}</span></div>
                    <div>Allergies: <span className="font-bold text-rose-600">{activeToken.patient?.allergies || 'None'}</span></div>
                  </div>
                </div>

                {/* Vitals Cards */}
                {patientVitals ? (
                  <div className="space-y-3">
                    <div className="grid grid-cols-2 sm:grid-cols-7 gap-3 text-center">
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">Blood Pressure</div>
                        <div className="text-sm font-black text-slate-800">
                          {patientVitals.bloodPressure || (patientVitals.bpSystolic ? `${patientVitals.bpSystolic}/${patientVitals.bpDiastolic}` : '120/80')}
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">Pulse Rate</div>
                        <div className="text-sm font-black text-slate-800">
                          {patientVitals.pulseRate || patientVitals.pulseRateBpm || 74} BPM
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">Temperature</div>
                        <div className="text-sm font-black text-slate-800">
                          {patientVitals.temperatureF || patientVitals.temperatureFahrenheit || 98.6}°F
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">SpO2</div>
                        <div className="text-sm font-black text-slate-800">
                          {patientVitals.spo2Percent || patientVitals.spo2Percentage || 98}%
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">Blood Sugar</div>
                        <div className="text-sm font-black text-indigo-700">
                          {patientVitals.bloodSugarMgDl ? `${patientVitals.bloodSugarMgDl} mg/dL` : '105 mg/dL'}
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">BMI</div>
                        <div className="text-sm font-black text-teal-700">
                          {patientVitals.bmi || '23.5'}
                        </div>
                      </div>
                      <div className="bg-slate-50 p-2.5 rounded-xl border border-slate-100">
                        <div className="text-[10px] uppercase font-bold text-slate-400">Pain Scale</div>
                        <div className="text-sm font-black text-amber-600">
                          {patientVitals.painScale != null ? patientVitals.painScale : 0}/10
                        </div>
                      </div>
                    </div>

                    {patientVitals.nurseNotes && (
                      <div className="bg-cyan-50/70 p-3 rounded-xl border border-cyan-100 text-xs text-cyan-900 flex items-start space-x-2">
                        <span className="font-bold text-cyan-800 uppercase text-[10px] bg-cyan-100 px-2 py-0.5 rounded-md">Nurse Triage Notes:</span>
                        <span className="font-medium">{patientVitals.nurseNotes}</span>
                      </div>
                    )}
                  </div>
                ) : (
                  <div className="p-3 bg-amber-50 rounded-xl border border-amber-200 text-xs text-amber-800 flex items-center justify-between">
                    <span>⚠️ Nurse Vitals not yet recorded for this patient token.</span>
                    <span className="text-[11px] font-bold text-amber-700">Pending Screening</span>
                  </div>
                )}
              </div>

              {/* Consultation Diagnosis Form */}
              <form onSubmit={handleCompleteConsultation} className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-6">
                <h3 className="text-base font-bold text-slate-800 border-b border-slate-100 pb-3 flex items-center space-x-2">
                  <Stethoscope className="w-5 h-5 text-teal-600" />
                  <span>Medical Diagnosis & Findings</span>
                </h3>

                <div className="grid grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Disease / Diagnosis Name</label>
                    <input
                      type="text"
                      required
                      value={consultationForm.diseaseName}
                      onChange={(e) => setConsultationForm({ ...consultationForm, diseaseName: e.target.value })}
                      className="w-full px-3 py-2 text-sm border rounded-xl"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-700 mb-1">Lab / Diagnostic Recommendations</label>
                    <input
                      type="text"
                      value={consultationForm.labTestRecommendations}
                      onChange={(e) => setConsultationForm({ ...consultationForm, labTestRecommendations: e.target.value })}
                      className="w-full px-3 py-2 text-sm border rounded-xl"
                    />
                  </div>
                </div>

                <div>
                  <label className="block text-xs font-bold text-slate-700 mb-1">Diagnosis Notes</label>
                  <textarea
                    rows={2}
                    required
                    value={consultationForm.diagnosisNotes}
                    onChange={(e) => setConsultationForm({ ...consultationForm, diagnosisNotes: e.target.value })}
                    className="w-full px-3 py-2 text-sm border rounded-xl"
                  />
                </div>

                {/* Decision Selector */}
                <div className="p-4 bg-slate-50 rounded-2xl border border-slate-200 flex items-center justify-between">
                  <div>
                    <div className="text-sm font-bold text-slate-800">Referral Decision Branching</div>
                    <div className="text-xs text-slate-500">Choose treatment inside camp vs external hospital referral</div>
                  </div>
                  <div className="flex space-x-3">
                    <button
                      type="button"
                      onClick={() => setRequiresReferral(false)}
                      className={`px-4 py-2 text-xs font-bold rounded-xl border transition-all ${
                        !requiresReferral
                          ? 'bg-teal-600 text-white border-teal-600 shadow-sm'
                          : 'bg-white text-slate-700 border-slate-200'
                      }`}
                    >
                      Treat inside Camp (Digital Prescription)
                    </button>
                    <button
                      type="button"
                      onClick={() => setRequiresReferral(true)}
                      className={`px-4 py-2 text-xs font-bold rounded-xl border transition-all ${
                        requiresReferral
                          ? 'bg-purple-600 text-white border-purple-600 shadow-sm'
                          : 'bg-white text-slate-700 border-slate-200'
                      }`}
                    >
                      Hospital Referral Required
                    </button>
                  </div>
                </div>

                {/* Conditional Branch UI */}
                {!requiresReferral ? (
                  <div className="space-y-6 pt-2">
                    {/* Live Pharmacy Stock Directory Card for Doctors */}
                    <div className="bg-slate-50 border border-slate-200 p-4 rounded-2xl space-y-3">
                      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-2">
                        <div>
                          <h4 className="text-xs font-bold uppercase tracking-wider text-slate-700 flex items-center space-x-1.5">
                            <PackageCheck className="w-4 h-4 text-emerald-600" />
                            <span>Live Camp Pharmacy Inventory Availability</span>
                          </h4>
                          <p className="text-[11px] text-slate-500">Check current medicine stock before prescribing</p>
                        </div>
                        <div className="relative w-full sm:w-64">
                          <Search className="w-3.5 h-3.5 text-slate-400 absolute left-3 top-2.5" />
                          <input
                            type="text"
                            placeholder="Search pharmacy stock..."
                            value={stockSearchKeyword}
                            onChange={(e) => setStockSearchKeyword(e.target.value)}
                            className="w-full pl-8 pr-3 py-1.5 bg-white border border-slate-200 rounded-xl text-xs font-medium focus:outline-none focus:ring-2 focus:ring-emerald-500/20"
                          />
                        </div>
                      </div>

                      <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-2.5 max-h-48 overflow-y-auto custom-scrollbar p-1">
                        {filteredStock.map((stockItem) => {
                          const isLow = stockItem.stockQuantity <= (stockItem.minAlertQuantity || 20);
                          const isOut = stockItem.stockQuantity <= 0;
                          return (
                            <div
                              key={stockItem.id}
                              className="bg-white p-2.5 rounded-xl border border-slate-200 flex items-center justify-between shadow-xs hover:border-emerald-300 transition-all text-xs"
                            >
                              <div className="pr-2">
                                <div className="font-bold text-slate-800 line-clamp-1">{stockItem.name}</div>
                                <div className="text-[10px] text-slate-400">{stockItem.category || 'General'}</div>
                                <span
                                  className={`inline-block mt-1 text-[10px] font-bold px-2 py-0.5 rounded-md ${
                                    isOut
                                      ? 'bg-rose-50 text-rose-700 border border-rose-200'
                                      : isLow
                                      ? 'bg-amber-50 text-amber-700 border border-amber-200'
                                      : 'bg-emerald-50 text-emerald-700 border border-emerald-200'
                                  }`}
                                >
                                  {isOut ? 'Out of Stock' : `${stockItem.stockQuantity} in stock`}
                                </span>
                              </div>
                              <button
                                type="button"
                                disabled={isOut}
                                onClick={() => handleAddMedicineFromStock(stockItem)}
                                className="px-2.5 py-1 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-40 text-white font-bold text-[11px] rounded-lg shadow-xs flex items-center space-x-1 shrink-0"
                              >
                                <Plus className="w-3 h-3" />
                                <span>Add</span>
                              </button>
                            </div>
                          );
                        })}
                        {filteredStock.length === 0 && (
                          <div className="col-span-3 text-center py-4 text-xs text-slate-400">
                            No medicines match search keyword in pharmacy inventory.
                          </div>
                        )}
                      </div>
                    </div>

                    {/* Prescription Items Builder */}
                    <div className="space-y-4">
                      <div className="flex items-center justify-between">
                        <h4 className="text-sm font-bold text-slate-800 flex items-center space-x-2">
                          <Pill className="w-4 h-4 text-teal-600" />
                          <span>Prescribe Medicines List ({medicines.length} Items)</span>
                        </h4>
                        <button
                          type="button"
                          onClick={handleAddBlankMedicineRow}
                          className="px-3 py-1.5 bg-teal-50 border border-teal-200 text-teal-700 hover:bg-teal-100 font-bold text-xs rounded-xl flex items-center space-x-1 transition-all"
                        >
                          <Plus className="w-3.5 h-3.5" />
                          <span>Add Blank Row</span>
                        </button>
                      </div>

                      <div className="space-y-2">
                        {medicines.map((med, idx) => {
                          const matchedStock = pharmacyStock.find(
                            (s) => s.name?.toLowerCase().trim() === med.medicineName?.toLowerCase().trim()
                          );
                          const exceedsStock = matchedStock && med.quantityPrescribed > matchedStock.stockQuantity;

                          return (
                            <div key={idx} className="bg-slate-50 p-3 rounded-xl border border-slate-200 space-y-2">
                              <div className="grid grid-cols-12 gap-2 text-xs items-center">
                                <div className="col-span-4">
                                  <label className="block text-[10px] font-bold text-slate-400 mb-1">Medicine Name</label>
                                  <input
                                    type="text"
                                    required
                                    placeholder="Medicine Name (e.g. Paracetamol 500mg)"
                                    value={med.medicineName}
                                    onChange={(e) => {
                                      const list = [...medicines];
                                      list[idx].medicineName = e.target.value;
                                      setMedicines(list);
                                    }}
                                    className="w-full px-2.5 py-1.5 border rounded-lg bg-white font-semibold text-slate-800"
                                  />
                                </div>
                                <div className="col-span-2">
                                  <label className="block text-[10px] font-bold text-slate-400 mb-1">Dosage</label>
                                  <input
                                    type="text"
                                    placeholder="500mg"
                                    value={med.dosage}
                                    onChange={(e) => {
                                      const list = [...medicines];
                                      list[idx].dosage = e.target.value;
                                      setMedicines(list);
                                    }}
                                    className="w-full px-2 py-1.5 border rounded-lg bg-white"
                                  />
                                </div>
                                <div className="col-span-2">
                                  <label className="block text-[10px] font-bold text-slate-400 mb-1">Frequency</label>
                                  <input
                                    type="text"
                                    placeholder="1-0-1"
                                    value={med.frequency}
                                    onChange={(e) => {
                                      const list = [...medicines];
                                      list[idx].frequency = e.target.value;
                                      setMedicines(list);
                                    }}
                                    className="w-full px-2 py-1.5 border rounded-lg bg-white"
                                  />
                                </div>
                                <div className="col-span-2">
                                  <label className="block text-[10px] font-bold text-slate-400 mb-1">Duration</label>
                                  <input
                                    type="text"
                                    placeholder="5 Days"
                                    value={med.duration}
                                    onChange={(e) => {
                                      const list = [...medicines];
                                      list[idx].duration = e.target.value;
                                      setMedicines(list);
                                    }}
                                    className="w-full px-2 py-1.5 border rounded-lg bg-white"
                                  />
                                </div>
                                <div className="col-span-1">
                                  <label className="block text-[10px] font-bold text-slate-400 mb-1 text-center">Qty</label>
                                  <input
                                    type="number"
                                    min="1"
                                    required
                                    value={med.quantityPrescribed}
                                    onChange={(e) => {
                                      const list = [...medicines];
                                      list[idx].quantityPrescribed = parseInt(e.target.value) || 1;
                                      setMedicines(list);
                                    }}
                                    className="w-full px-2 py-1.5 border rounded-lg bg-white font-bold text-center text-slate-800"
                                  />
                                </div>
                                <div className="col-span-1 flex justify-center pt-4">
                                  <button
                                    type="button"
                                    onClick={() => handleRemoveMedicineRow(idx)}
                                    className="p-1 text-slate-400 hover:text-rose-600 transition-colors"
                                  >
                                    <Trash2 className="w-4 h-4" />
                                  </button>
                                </div>
                              </div>

                              {/* Pharmacy Live Stock Indicator Bar */}
                              <div className="flex items-center space-x-2 text-[11px]">
                                {matchedStock ? (
                                  <span
                                    className={`font-bold flex items-center space-x-1 ${
                                      exceedsStock ? 'text-rose-600 font-bold' : 'text-emerald-700'
                                    }`}
                                  >
                                    {exceedsStock ? (
                                      <AlertTriangle className="w-3.5 h-3.5 text-rose-600 inline" />
                                    ) : (
                                      <Check className="w-3.5 h-3.5 text-emerald-600 inline" />
                                    )}
                                    <span>
                                      Pharmacy Live Stock: {matchedStock.stockQuantity} available
                                      {exceedsStock && ` (Warning: Prescribing ${med.quantityPrescribed} exceeds available stock!)`}
                                    </span>
                                  </span>
                                ) : (
                                  <span className="text-slate-400 italic">
                                    Medicine name not matched with current pharmacy inventory list.
                                  </span>
                                )}
                              </div>
                            </div>
                          );
                        })}
                      </div>
                    </div>
                  </div>
                ) : (
                  /* Hospital Referral Builder */
                  <div className="space-y-4 pt-2">
                    <h4 className="text-sm font-bold text-purple-900 flex items-center space-x-2">
                      <Share2 className="w-4 h-4 text-purple-600" />
                      <span>Destination Hospital Referral Details</span>
                    </h4>

                    {/* Referring Doctor Information Banner */}
                    <div className="p-3.5 bg-purple-50 border border-purple-200 rounded-xl text-xs text-purple-900 flex items-center justify-between shadow-xs">
                      <div>
                        <span className="text-[10px] uppercase font-bold text-purple-600 tracking-wider block">Referring Medical Officer</span>
                        <span className="font-bold text-sm text-purple-950">{user?.fullName || 'Dr. Sarah Jenkins'}</span>
                        <span className="text-purple-700 ml-2 font-mono">({user?.memberId || 'MC-DOC-001'})</span>
                      </div>
                      <div className="text-right">
                        <span className="bg-purple-200 text-purple-800 text-[10px] font-bold px-2 py-1 rounded-lg">
                          {user?.specialization || 'Attending Physician'}
                        </span>
                      </div>
                    </div>

                    <div className="grid grid-cols-2 gap-4">
                      <div>
                        <label className="block text-xs font-bold text-slate-700 mb-1">Destination Hospital Name</label>
                        <input
                          type="text"
                          required
                          value={referralForm.hospitalName}
                          onChange={(e) => setReferralForm({ ...referralForm, hospitalName: e.target.value })}
                          className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
                        />
                      </div>
                      <div>
                        <label className="block text-xs font-bold text-slate-700 mb-1">Department / Specialty</label>
                        <input
                          type="text"
                          required
                          value={referralForm.department}
                          onChange={(e) => setReferralForm({ ...referralForm, department: e.target.value })}
                          className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
                        />
                      </div>
                    </div>

                    <div>
                      <label className="block text-xs font-bold text-slate-700 mb-1">Clinical Referral Reason & Diagnosis Summary</label>
                      <textarea
                        rows={2}
                        required
                        value={referralForm.reason}
                        onChange={(e) => setReferralForm({ ...referralForm, reason: e.target.value })}
                        className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-medium text-slate-800"
                      />
                    </div>
                  </div>
                )}

                <button
                  type="submit"
                  disabled={submittingLoading}
                  className="w-full py-3 bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold rounded-xl shadow-lg shadow-teal-600/30 hover:opacity-95 disabled:opacity-50 transition-all flex items-center justify-center space-x-2"
                >
                  <CheckCircle className="w-5 h-5" />
                  <span>{submittingLoading ? 'Finalizing Consultation...' : 'Finalize Consultation & Submit Record'}</span>
                </button>
              </form>
            </div>
          ) : (
            <div className="bg-white rounded-2xl border border-slate-100 p-12 text-center text-slate-400">
              Select a patient from the queue on the left to begin doctor consultation.
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default DoctorDashboard;
