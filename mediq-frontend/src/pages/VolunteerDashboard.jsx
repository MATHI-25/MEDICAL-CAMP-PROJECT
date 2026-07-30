import React, { useState, useEffect } from 'react';
import patientService from '../services/patientService';
import queueService from '../services/queueService';
import campService from '../services/campService';
import notificationService from '../services/notificationService';
import StatusBadge from '../components/StatusBadge';
import { UserPlus, Ticket, QrCode, Phone, HeartPulse, ShieldCheck, MessageSquare, Send, Smartphone } from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const VolunteerDashboard = () => {
  const [camps, setCamps] = useState([]);
  const [selectedCampId, setSelectedCampId] = useState('');
  const [registeredPatient, setRegisteredPatient] = useState(null);
  const [generatedToken, setGeneratedToken] = useState(null);
  const [smsSending, setSmsSending] = useState(false);

  // Form State
  const [patientForm, setPatientForm] = useState({
    fullName: 'Robert Miller',
    age: 45,
    gender: 'MALE',
    bloodGroup: 'O_POSITIVE',
    phone: '+1-555-0199',
    address: '42 Maple Street, Sector 5',
    emergencyContact: '+1-555-0198',
    allergies: 'Penicillin allergy',
    chronicDiseases: 'Hypertension',
  });

  useEffect(() => {
    fetchCamps();
  }, []);

  const fetchCamps = async () => {
    try {
      const res = await campService.getCampsByStatus('ONGOING');
      const ongoing = res.data || res;
      const ongoingList = Array.isArray(ongoing) ? ongoing : [];
      
      if (ongoingList.length > 0) {
        setCamps(ongoingList);
        setSelectedCampId(ongoingList[0].id);
      } else {
        const allRes = await campService.searchCamps(null, '', 0, 10);
        const all = allRes.data?.content || allRes.content || allRes.data || [];
        const allList = Array.isArray(all) ? all : [];
        setCamps(allList);
        if (allList.length > 0) setSelectedCampId(allList[0].id);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const handleRegisterPatient = async (e) => {
    e.preventDefault();
    if (!selectedCampId) {
      alert('Please select an active medical camp');
      return;
    }

    try {
      const pRes = await patientService.registerPatient({
        ...patientForm,
        registeredCampId: selectedCampId,
      });
      const newPatient = pRes?.data || pRes;
      setRegisteredPatient(newPatient);

      // Auto Generate Queue Token
      const tRes = await queueService.generateToken({
        patientId: newPatient.id,
        campId: selectedCampId,
        priorityReason: 'Regular Walk-in',
      });
      const newToken = tRes?.data || tRes;
      setGeneratedToken(newToken);

      // Auto Send Registration SMS
      try {
        await notificationService.sendRegistrationSms(newPatient.id, selectedCampId);
      } catch (err) {
        console.error('Auto SMS dispatch skipped', err);
      }
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to register patient');
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

  const handleSendRegistrationSms = async () => {
    if (!registeredPatient || !selectedCampId) return;
    setSmsSending(true);
    const cleanPhone = sanitizePhone(registeredPatient.phone);
    try {
      const res = await notificationService.sendRegistrationSms(registeredPatient.id, selectedCampId);
      const smsData = res.data || res;
      const messageBody = smsData.messageBody || 'Registration receipt sent!';

      // Launch native Cellular SMS app pre-filled for target phone number
      window.open(`sms:+${cleanPhone}?body=${encodeURIComponent(messageBody)}`, '_self');
    } catch (e) {
      alert(e?.message || 'Failed to dispatch SMS notification');
    } finally {
      setSmsSending(false);
    }
  };

  const handleSendWhatsApp = async () => {
    if (!registeredPatient || !generatedToken) return;
    const cleanPhone = sanitizePhone(registeredPatient.phone);
    const campObj = camps.find(c => c.id === selectedCampId);
    const msg = `[MediQ Health Camp Intake Receipt]\nHello ${registeredPatient.fullName}!\n` +
      `Camp: ${campObj?.title || 'Medical Camp'}\n` +
      `Token No: ${generatedToken.tokenNumber}\n` +
      `Patient ID: ${registeredPatient.patientId}\n` +
      `Timing: ${campObj?.operatingHours || '09:00 AM - 05:00 PM'}\n` +
      `Location: ${campObj?.location || 'Camp Venue'}`;

    try {
      await notificationService.sendWhatsApp({
        phoneNumber: cleanPhone,
        customMessage: msg,
        notificationType: 'WHATSAPP_REGISTRATION',
      });
      window.open(`https://api.whatsapp.com/send?phone=${cleanPhone}&text=${encodeURIComponent(msg)}`, '_blank');
    } catch (e) {
      window.open(`https://api.whatsapp.com/send?phone=${cleanPhone}&text=${encodeURIComponent(msg)}`, '_blank');
    }
  };

  return (
    <div className="space-y-6">
      {/* Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">Active Intake Station</h2>
          <p className="text-slate-400 text-xs">Select target medical camp for patient registration</p>
        </div>
        <select
          value={selectedCampId}
          onChange={(e) => setSelectedCampId(e.target.value)}
          className="px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 shadow-xs focus:outline-none focus:ring-2 focus:ring-amber-500/20"
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
        type="volunteer"
        stats={[
          { label: 'Active Camp', value: camps.find(c => c.id === selectedCampId)?.title || 'Selected' },
          { label: 'Latest Issued Token', value: generatedToken ? generatedToken.tokenNumber : 'None' }
        ]}
      />

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
        
        {/* Left: Patient Intake Form */}
        <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-6">
          <div className="flex items-center justify-between border-b border-slate-100 pb-4">
            <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
              <UserPlus className="w-5 h-5 text-teal-600" />
              <span>New Patient Intake Registration</span>
            </h2>

            {/* Camp Selector */}
            <div>
              <select
                value={selectedCampId}
                onChange={(e) => setSelectedCampId(e.target.value)}
                className="px-3 py-1.5 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700"
              >
                {camps.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.title} ({c.campCode})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <form onSubmit={handleRegisterPatient} className="space-y-4">
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Full Name</label>
                <input
                  type="text"
                  required
                  value={patientForm.fullName}
                  onChange={(e) => setPatientForm({ ...patientForm, fullName: e.target.value })}
                  placeholder="e.g. Robert Miller"
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Mobile Phone Number (SMS / WhatsApp)</label>
                <input
                  type="text"
                  required
                  value={patientForm.phone}
                  onChange={(e) => setPatientForm({ ...patientForm, phone: e.target.value })}
                  placeholder="+1-555-0199 or 9876543210"
                  className="w-full px-3 py-2 text-sm border rounded-xl font-semibold"
                />
              </div>
            </div>

            <div className="grid grid-cols-3 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Age</label>
                <input
                  type="number"
                  required
                  value={patientForm.age}
                  onChange={(e) => setPatientForm({ ...patientForm, age: parseInt(e.target.value) })}
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Gender</label>
                <select
                  value={patientForm.gender}
                  onChange={(e) => setPatientForm({ ...patientForm, gender: e.target.value })}
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                >
                  <option value="MALE">MALE</option>
                  <option value="FEMALE">FEMALE</option>
                  <option value="OTHER">OTHER</option>
                </select>
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Blood Group</label>
                <select
                  value={patientForm.bloodGroup}
                  onChange={(e) => setPatientForm({ ...patientForm, bloodGroup: e.target.value })}
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                >
                  <option value="A_POSITIVE">A+</option>
                  <option value="A_NEGATIVE">A-</option>
                  <option value="B_POSITIVE">B+</option>
                  <option value="B_NEGATIVE">B-</option>
                  <option value="O_POSITIVE">O+</option>
                  <option value="O_NEGATIVE">O-</option>
                  <option value="AB_POSITIVE">AB+</option>
                  <option value="AB_NEGATIVE">AB-</option>
                </select>
              </div>
            </div>

            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Residential Address</label>
              <input
                type="text"
                value={patientForm.address}
                onChange={(e) => setPatientForm({ ...patientForm, address: e.target.value })}
                className="w-full px-3 py-2 text-sm border rounded-xl"
              />
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Emergency Contact</label>
                <input
                  type="text"
                  value={patientForm.emergencyContact}
                  onChange={(e) => setPatientForm({ ...patientForm, emergencyContact: e.target.value })}
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                />
              </div>
              <div>
                <label className="block text-xs font-bold text-slate-700 mb-1">Allergies (If any)</label>
                <input
                  type="text"
                  value={patientForm.allergies}
                  onChange={(e) => setPatientForm({ ...patientForm, allergies: e.target.value })}
                  placeholder="e.g. Penicillin, Dust"
                  className="w-full px-3 py-2 text-sm border rounded-xl"
                />
              </div>
            </div>

            <button
              type="submit"
              className="w-full py-3 bg-gradient-to-r from-teal-600 to-emerald-600 text-white font-bold rounded-xl shadow-lg shadow-teal-600/30 hover:opacity-95 transition-all flex items-center justify-center space-x-2"
            >
              <Ticket className="w-5 h-5" />
              <span>Register Patient & Auto-Send SMS Token Receipt</span>
            </button>
          </form>
        </div>

        {/* Right: Issued Patient ID & Token Card */}
        <div className="space-y-6">
          {registeredPatient && generatedToken ? (
            <div className="bg-gradient-to-tr from-slate-900 to-slate-800 text-white rounded-3xl p-6 shadow-2xl space-y-4 border border-slate-700">
              <div className="flex items-center justify-between border-b border-slate-700/60 pb-3">
                <div className="flex items-center space-x-2">
                  <ShieldCheck className="w-5 h-5 text-teal-400" />
                  <span className="text-xs font-bold uppercase tracking-wider text-slate-400">MediQ Intake Pass</span>
                </div>
                <span className="text-xs font-mono font-bold bg-teal-500/20 text-teal-300 px-2 py-0.5 rounded-full border border-teal-500/30">
                  VERIFIED
                </span>
              </div>

              {/* Big Queue Token Display */}
              <div className="text-center bg-slate-800/80 p-4 rounded-2xl border border-slate-700">
                <div className="text-xs font-semibold text-slate-400 uppercase">Queue Token Number</div>
                <div className="text-4xl font-black tracking-wider text-teal-400 my-1 font-mono">
                  {generatedToken.tokenNumber}
                </div>
                <div className="text-xs text-slate-400 font-medium">Est. Wait: ~{generatedToken.estimatedWaitMinutes} minutes</div>
              </div>

              {/* Patient Demographics */}
              <div className="space-y-2 text-xs">
                <div className="flex justify-between border-b border-slate-800 pb-1">
                  <span className="text-slate-400">Patient ID</span>
                  <span className="font-bold font-mono text-teal-300">{registeredPatient.patientId}</span>
                </div>
                <div className="flex justify-between border-b border-slate-800 pb-1">
                  <span className="text-slate-400">Full Name</span>
                  <span className="font-bold">{registeredPatient.fullName}</span>
                </div>
                <div className="flex justify-between border-b border-slate-800 pb-1">
                  <span className="text-slate-400">Age / Gender</span>
                  <span className="font-bold">{registeredPatient.age} Yrs / {registeredPatient.gender}</span>
                </div>
                <div className="flex justify-between border-b border-slate-800 pb-1">
                  <span className="text-slate-400">Mobile Phone</span>
                  <span className="font-bold text-teal-300 font-mono">{registeredPatient.phone}</span>
                </div>
              </div>

              {/* Instant Notification Buttons */}
              <div className="space-y-2 pt-2 border-t border-slate-700">
                <button
                  onClick={handleSendRegistrationSms}
                  disabled={smsSending}
                  className="w-full py-2 bg-gradient-to-r from-amber-500 to-orange-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md flex items-center justify-center space-x-2 transition-all"
                >
                  <Smartphone className="w-4 h-4" />
                  <span>{smsSending ? 'Sending SMS...' : '📱 Send Plain SMS (Keypad Phone)'}</span>
                </button>

                <button
                  onClick={handleSendWhatsApp}
                  className="w-full py-2 bg-gradient-to-r from-emerald-600 to-teal-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md flex items-center justify-center space-x-2 transition-all"
                >
                  <MessageSquare className="w-4 h-4" />
                  <span>🟢 Send WhatsApp Receipt</span>
                </button>

                <button
                  onClick={() => window.print()}
                  className="w-full py-2 bg-slate-800 hover:bg-slate-700 text-slate-300 font-bold text-xs rounded-xl border border-slate-700 transition-colors"
                >
                  Print Paper Token Pass
                </button>
              </div>
            </div>
          ) : (
            <div className="bg-slate-100/70 border-2 border-dashed border-slate-200 rounded-3xl p-8 text-center text-slate-400 space-y-2">
              <Ticket className="w-10 h-10 mx-auto text-slate-300 stroke-1" />
              <div className="text-sm font-bold text-slate-600">No Patient Registered Yet</div>
              <p className="text-xs">Fill out patient details on the left to generate digital pass & auto-dispatch mobile receipt</p>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default VolunteerDashboard;
