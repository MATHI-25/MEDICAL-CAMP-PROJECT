import React, { useState, useEffect } from 'react';
import queueService from '../services/queueService';
import nurseService from '../services/nurseService';
import userService from '../services/userService';
import campService from '../services/campService';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import { Activity, Stethoscope, Heart, Thermometer, UserCheck } from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const NurseDashboard = () => {
  const [queue, setQueue] = useState([]);
  const [doctors, setDoctors] = useState([]);
  const [camps, setCamps] = useState([]);
  const [selectedCampId, setSelectedCampId] = useState('');
  const [loading, setLoading] = useState(true);

  // Vitals Recording Modal
  const [isVitalsModalOpen, setIsVitalsModalOpen] = useState(false);
  const [selectedToken, setSelectedToken] = useState(null);

  const [vitalsForm, setVitalsForm] = useState({
    heightCm: 172.0,
    weightKg: 68.5,
    temperatureFahrenheit: 98.6,
    bpSystolic: 120,
    bpDiastolic: 80,
    pulseRateBpm: 72,
    spo2Percentage: 98,
    bloodSugarMgDl: 105,
    painScale: 0,
    nurseNotes: 'Patient appears alert and oriented x3. Normal vital signs.',
    assignedDoctorId: '',
  });

  useEffect(() => {
    fetchCamps();
    fetchDoctors();
  }, []);

  useEffect(() => {
    if (selectedCampId) {
      fetchNurseQueue();
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

  const fetchDoctors = async () => {
    try {
      const res = await userService.getUsersByRole('DOCTOR');
      const docs = res.data || res;
      const docList = Array.isArray(docs) ? docs : [];
      setDoctors(docList);
      if (docList.length > 0) {
        setVitalsForm((prev) => ({ ...prev, assignedDoctorId: docList[0].id }));
      }
    } catch (e) {
      console.error(e);
    }
  };

  const fetchNurseQueue = async () => {
    setLoading(true);
    try {
      const res = await queueService.getNurseQueue(selectedCampId);
      const queueList = res.data || res;
      setQueue(Array.isArray(queueList) ? queueList : []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const handleRecordVitals = async (e) => {
    e.preventDefault();
    if (!selectedToken) return;

    try {
      const payload = {
        patientId: selectedToken.patient.id,
        queueTokenId: selectedToken.id,
        campId: selectedCampId,
        assignedDoctorId: vitalsForm.assignedDoctorId ? parseInt(vitalsForm.assignedDoctorId) : (doctors[0]?.id || null),
        heightCm: parseFloat(vitalsForm.heightCm || 170),
        weightKg: parseFloat(vitalsForm.weightKg || 65),
        temperatureF: parseFloat(vitalsForm.temperatureFahrenheit || vitalsForm.temperatureF || 98.6),
        bloodPressure: vitalsForm.bloodPressure || `${vitalsForm.bpSystolic || 120}/${vitalsForm.bpDiastolic || 80}`,
        pulseRate: parseInt(vitalsForm.pulseRateBpm || vitalsForm.pulseRate || 72),
        respiratoryRate: parseInt(vitalsForm.respiratoryRate || 16),
        bloodSugarMgDl: parseFloat(vitalsForm.bloodSugarMgDl || 100),
        spo2Percent: parseInt(vitalsForm.spo2Percentage || vitalsForm.spo2Percent || 98),
        painScale: parseInt(vitalsForm.painScale || 0),
        symptoms: vitalsForm.symptoms || 'General checkup',
        nurseNotes: vitalsForm.nurseNotes || 'Vitals recorded by nurse',
      };

      await nurseService.recordVitals(payload);

      setIsVitalsModalOpen(false);
      fetchNurseQueue();
      alert('Vitals recorded & patient forwarded to Doctor Queue!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to record vitals');
    }
  };

  return (
    <div className="space-y-6">
      {/* Camp Selector Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">Active Screening Station</h2>
          <p className="text-slate-400 text-xs">Switch medical camp location for patient vitals triage</p>
        </div>
        <select
          value={selectedCampId}
          onChange={(e) => setSelectedCampId(e.target.value)}
          className="px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 shadow-xs focus:outline-none focus:ring-2 focus:ring-cyan-500/20"
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
        type="nurse"
        stats={[
          { label: 'Triage Queue', value: `${queue.length} Awaiting Vitals` },
          { label: 'Available Doctors', value: `${doctors.length} On Duty` }
        ]}
      />

      {/* Queue List */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
            <Activity className="w-5 h-5 text-teal-600" />
            <span>Waiting Patients Queue ({queue.length})</span>
          </h2>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-6">Token</th>
                <th className="py-3 px-6">Patient ID</th>
                <th className="py-3 px-6">Patient Name</th>
                <th className="py-3 px-6">Age / Gender</th>
                <th className="py-3 px-6">Queue Status</th>
                <th className="py-3 px-6 text-right">Action</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {queue.map((item) => (
                <tr key={item.id} className="hover:bg-slate-50/50">
                  <td className="py-4 px-6 font-mono font-black text-teal-700 text-base">
                    {item.tokenNumber}
                  </td>
                  <td className="py-4 px-6 font-mono text-slate-600">{item.patient?.patientId}</td>
                  <td className="py-4 px-6 font-bold text-slate-800">{item.patient?.fullName}</td>
                  <td className="py-4 px-6 text-slate-600">{item.patient?.age} Yrs / {item.patient?.gender}</td>
                  <td className="py-4 px-6">
                    <StatusBadge status={item.status} />
                  </td>
                  <td className="py-4 px-6 text-right">
                    <button
                      onClick={() => {
                        setSelectedToken(item);
                        setIsVitalsModalOpen(true);
                      }}
                      className="px-4 py-1.5 bg-teal-600 hover:bg-teal-700 text-white font-bold text-xs rounded-xl shadow-md shadow-teal-600/20 transition-all"
                    >
                      Record Vitals
                    </button>
                  </td>
                </tr>
              ))}
              {queue.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center py-8 text-slate-400 text-sm">
                    No patients currently waiting in nurse queue
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Vitals Recording Modal */}
      <Modal isOpen={isVitalsModalOpen} onClose={() => setIsVitalsModalOpen(false)} title={`Record Clinical Vitals for ${selectedToken?.patient?.fullName}`}>
        <form onSubmit={handleRecordVitals} className="space-y-4">
          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Height (cm)</label>
              <input
                type="number"
                step="0.1"
                required
                value={vitalsForm.heightCm}
                onChange={(e) => setVitalsForm({ ...vitalsForm, heightCm: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Weight (kg)</label>
              <input
                type="number"
                step="0.1"
                required
                value={vitalsForm.weightKg}
                onChange={(e) => setVitalsForm({ ...vitalsForm, weightKg: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Temperature (°F)</label>
              <input
                type="number"
                step="0.1"
                required
                value={vitalsForm.temperatureFahrenheit}
                onChange={(e) => setVitalsForm({ ...vitalsForm, temperatureFahrenheit: parseFloat(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">BP Systolic</label>
              <input
                type="number"
                required
                value={vitalsForm.bpSystolic}
                onChange={(e) => setVitalsForm({ ...vitalsForm, bpSystolic: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">BP Diastolic</label>
              <input
                type="number"
                required
                value={vitalsForm.bpDiastolic}
                onChange={(e) => setVitalsForm({ ...vitalsForm, bpDiastolic: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Pulse (BPM)</label>
              <input
                type="number"
                required
                value={vitalsForm.pulseRateBpm}
                onChange={(e) => setVitalsForm({ ...vitalsForm, pulseRateBpm: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">SpO2 (%)</label>
              <input
                type="number"
                required
                value={vitalsForm.spo2Percentage}
                onChange={(e) => setVitalsForm({ ...vitalsForm, spo2Percentage: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Blood Sugar (mg/dL)</label>
              <input
                type="number"
                value={vitalsForm.bloodSugarMgDl}
                onChange={(e) => setVitalsForm({ ...vitalsForm, bloodSugarMgDl: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Pain Scale (0-10)</label>
              <input
                type="number"
                min="0"
                max="10"
                value={vitalsForm.painScale}
                onChange={(e) => setVitalsForm({ ...vitalsForm, painScale: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Nurse Clinical Notes</label>
            <textarea
              rows={2}
              value={vitalsForm.nurseNotes}
              onChange={(e) => setVitalsForm({ ...vitalsForm, nurseNotes: e.target.value })}
              className="w-full px-3 py-2 text-sm border rounded-xl"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Assign Doctor for Consultation</label>
            <select
              value={vitalsForm.assignedDoctorId}
              onChange={(e) => setVitalsForm({ ...vitalsForm, assignedDoctorId: e.target.value })}
              className="w-full px-3 py-2 text-sm border rounded-xl font-medium text-slate-800"
            >
              {doctors.map((d) => (
                <option key={d.id} value={d.id}>
                  {d.fullName} ({d.specialization})
                </option>
              ))}
            </select>
          </div>

          <button
            type="submit"
            className="w-full py-3 bg-teal-600 text-white font-bold rounded-xl shadow-md hover:bg-teal-700 mt-2 flex items-center justify-center space-x-2"
          >
            <UserCheck className="w-5 h-5" />
            <span>Save Vitals & Send to Doctor Queue</span>
          </button>
        </form>
      </Modal>
    </div>
  );
};

export default NurseDashboard;
