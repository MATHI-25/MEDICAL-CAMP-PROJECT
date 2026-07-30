import React, { useState, useEffect } from 'react';
import campService from '../services/campService';
import userService from '../services/userService';
import patientService from '../services/patientService';
import StatusBadge from '../components/StatusBadge';
import Modal from '../components/Modal';
import {
  Building2,
  Plus,
  Users,
  Calendar,
  MapPin,
  CheckCircle2,
  Search,
  FileText,
  User,
  Activity,
  Stethoscope,
  Pill,
  ExternalLink,
  History,
  Clock,
} from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const OrganizerDashboard = () => {
  const [activeTab, setActiveTab] = useState('CAMPS'); // 'CAMPS' | 'PATIENT_HISTORY'
  const [camps, setCamps] = useState([]);
  const [loading, setLoading] = useState(true);

  // Staff options
  const [doctors, setDoctors] = useState([]);
  const [nurses, setNurses] = useState([]);
  const [volunteers, setVolunteers] = useState([]);

  // Modals
  const [isAddCampOpen, setIsAddCampOpen] = useState(false);
  const [isAssignStaffOpen, setIsAssignStaffOpen] = useState(false);
  const [selectedCamp, setSelectedCamp] = useState(null);

  // Forms
  const [newCamp, setNewCamp] = useState({
    title: 'Rural Healthcare Medical Camp',
    description: 'Free general health checkups, vitals recording, doctor consultation, digital prescription and medicine distribution.',
    startDate: '2026-08-01',
    endDate: '2026-08-03',
    startTime: '09:00 AM',
    endTime: '05:00 PM',
    location: 'Community Health Centre, Sector 12',
    venue: 'Community Health Centre, Sector 12',
    capacity: 250,
  });

  const [staffAssignment, setStaffAssignment] = useState({
    doctorIds: [],
    nurseIds: [],
    volunteerIds: [],
  });

  // Patient History State
  const [patientSearch, setPatientSearch] = useState('');
  const [patientsList, setPatientsList] = useState([]);
  const [selectedPatient, setSelectedPatient] = useState(null);
  const [patientTimeline, setPatientTimeline] = useState([]);
  const [historyLoading, setHistoryLoading] = useState(false);

  useEffect(() => {
    fetchCamps();
    fetchStaff();
  }, []);

  const fetchCamps = async () => {
    setLoading(true);
    try {
      const res = await campService.searchCamps(null, '', 0, 100);
      const list = res.data?.content || res.content || res.data || [];
      setCamps(Array.isArray(list) ? list : []);
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  const fetchStaff = async () => {
    try {
      const dRes = await userService.getUsersByRole('DOCTOR');
      const docs = dRes.data || dRes;
      setDoctors(Array.isArray(docs) ? docs : []);

      const nRes = await userService.getUsersByRole('NURSE');
      const nursesList = nRes.data || nRes;
      setNurses(Array.isArray(nursesList) ? nursesList : []);

      const vRes = await userService.getUsersByRole('REGISTRATION_VOLUNTEER');
      const vols = vRes.data || vRes;
      setVolunteers(Array.isArray(vols) ? vols : []);
    } catch (e) {
      console.error(e);
    }
  };

  const handleCreateCamp = async (e) => {
    e.preventDefault();
    try {
      const payload = {
        title: newCamp.title,
        description: newCamp.description,
        location: newCamp.location,
        venue: newCamp.venue || newCamp.location,
        startDate: newCamp.startDate,
        endDate: newCamp.endDate,
        startTime: newCamp.startTime,
        endTime: newCamp.endTime,
        operatingHours: `${newCamp.startTime} - ${newCamp.endTime}`,
        targetCapacity: parseInt(newCamp.capacity || newCamp.targetCapacity || 250),
        status: 'UPCOMING',
      };
      await campService.createCamp(payload);
      setIsAddCampOpen(false);
      await fetchCamps();
      alert('Medical Camp created successfully!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to create medical camp');
    }
  };

  const handleAssignStaff = async (e) => {
    e.preventDefault();
    if (!selectedCamp) return;
    try {
      await campService.assignStaff(selectedCamp.id, staffAssignment);
      setIsAssignStaffOpen(false);
      await fetchCamps();
      alert('Staff assigned to camp successfully!');
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to assign staff');
    }
  };

  const handleStatusChange = async (campId, status) => {
    try {
      await campService.updateCampStatus(campId, status);
      await fetchCamps();
    } catch (e) {
      alert(e?.message || e?.response?.data?.message || 'Failed to update status');
    }
  };

  // Patient History Methods
  const handleSearchPatients = async (e) => {
    if (e) e.preventDefault();
    setHistoryLoading(true);
    try {
      const res = await patientService.searchPatients(null, patientSearch || '', 0, 50);
      const pList = res.data?.content || res.content || res.data || [];
      setPatientsList(Array.isArray(pList) ? pList : []);
      if (pList.length > 0 && !selectedPatient) {
        handleSelectPatient(pList[0]);
      }
    } catch (err) {
      console.error(err);
    } finally {
      setHistoryLoading(false);
    }
  };

  const handleSelectPatient = async (patient) => {
    setSelectedPatient(patient);
    try {
      const res = await patientService.getPatientTimeline(patient.id);
      const tData = res.data || res;
      setPatientTimeline(Array.isArray(tData) ? tData : []);
    } catch (err) {
      console.error(err);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">Camp Operations Hub</h2>
          <p className="text-slate-400 text-xs">Create new relief camps and assign healthcare personnel</p>
        </div>
        <button
          onClick={() => setIsAddCampOpen(true)}
          className="px-4 py-2 bg-gradient-to-r from-emerald-600 to-teal-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md shadow-emerald-600/20 flex items-center space-x-2 transition-all"
        >
          <Plus className="w-4 h-4" />
          <span>Create New Medical Camp</span>
        </button>
      </div>

      {/* Hero Animated Banner */}
      <AnimatedHeroBanner
        type="organizer"
        stats={[
          { label: 'Total Camps', value: `${camps.length} Scheduled` },
          { label: 'Staff Roster', value: `${doctors.length + nurses.length + volunteers.length} Total` }
        ]}
      />

      {/* Tabs */}
      <div className="flex border-b border-slate-200 space-x-8">
        <button
          onClick={() => setActiveTab('CAMPS')}
          className={`pb-3 text-sm font-bold flex items-center space-x-2 transition-colors border-b-2 ${
            activeTab === 'CAMPS'
              ? 'border-teal-600 text-teal-700'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <Building2 className="w-4 h-4" />
          <span>Camp Details & Staff Assignments ({camps.length})</span>
        </button>

        <button
          onClick={() => {
            setActiveTab('PATIENT_HISTORY');
            handleSearchPatients();
          }}
          className={`pb-3 text-sm font-bold flex items-center space-x-2 transition-colors border-b-2 ${
            activeTab === 'PATIENT_HISTORY'
              ? 'border-teal-600 text-teal-700'
              : 'border-transparent text-slate-500 hover:text-slate-700'
          }`}
        >
          <History className="w-4 h-4" />
          <span>Patient Medical History & Records</span>
        </button>
      </div>

      {/* TAB 1: MEDICAL CAMPS LIST */}
      {activeTab === 'CAMPS' && (
        <div className="space-y-6">
          {loading ? (
            <div className="text-center py-12 text-slate-400 font-semibold text-sm">Loading medical camps...</div>
          ) : camps.length === 0 ? (
            <div className="text-center py-12 bg-white rounded-2xl border border-slate-100 p-8 space-y-3">
              <Building2 className="w-12 h-12 text-slate-300 mx-auto" />
              <h3 className="text-lg font-bold text-slate-700">No Medical Camps Scheduled</h3>
              <p className="text-slate-500 text-xs">Click "Create New Medical Camp" to publish your first camp.</p>
            </div>
          ) : (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
              {camps.map((camp) => (
                <div key={camp.id} className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-4">
                  <div className="flex items-start justify-between">
                    <div>
                      <span className="text-xs font-mono font-bold text-teal-700 bg-teal-50 px-2 py-0.5 rounded-md border border-teal-200">
                        {camp.campCode || `CAMP-${camp.id}`}
                      </span>
                      <h3 className="text-lg font-bold text-slate-800 mt-1.5">{camp.title}</h3>
                    </div>
                    <StatusBadge status={camp.status} />
                  </div>

                  <p className="text-slate-600 text-xs leading-relaxed">{camp.description}</p>

                  {/* Camp Meta Details */}
                  <div className="grid grid-cols-2 gap-3 text-xs font-semibold text-slate-600 bg-slate-50 p-3.5 rounded-xl border border-slate-100">
                    <div className="flex items-center space-x-2">
                      <MapPin className="w-4 h-4 text-teal-600 flex-shrink-0" />
                      <span className="truncate">{camp.location || camp.venue || 'Main Healthcare Centre'}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Calendar className="w-4 h-4 text-teal-600 flex-shrink-0" />
                      <span>{camp.startDate} to {camp.endDate}</span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Clock className="w-4 h-4 text-teal-600 flex-shrink-0" />
                      <span>Timing: <strong className="text-slate-800">{camp.operatingHours || (camp.startTime && camp.endTime ? `${camp.startTime} - ${camp.endTime}` : '09:00 AM - 05:00 PM')}</strong></span>
                    </div>
                    <div className="flex items-center space-x-2">
                      <Users className="w-4 h-4 text-teal-600 flex-shrink-0" />
                      <span>Capacity: <strong className="text-slate-800">{camp.targetCapacity || camp.capacity || 250} Patients</strong></span>
                    </div>
                  </div>

                  {/* Assigned Staff Breakdown */}
                  <div className="space-y-2 border-t border-slate-100 pt-3">
                    <div className="text-xs font-bold text-slate-500 uppercase tracking-wider">Assigned Team</div>
                    
                    <div className="text-xs text-slate-700 space-y-1">
                      <div>
                        <span className="font-bold text-teal-700">Doctors: </span>
                        {camp.assignedDoctors && camp.assignedDoctors.length > 0
                          ? camp.assignedDoctors.map((d) => d.fullName).join(', ')
                          : 'No doctors assigned'}
                      </div>
                      <div>
                        <span className="font-bold text-teal-700">Nurses: </span>
                        {camp.assignedNurses && camp.assignedNurses.length > 0
                          ? camp.assignedNurses.map((n) => n.fullName).join(', ')
                          : 'No nurses assigned'}
                      </div>
                      <div>
                        <span className="font-bold text-teal-700">Volunteers: </span>
                        {camp.assignedVolunteers && camp.assignedVolunteers.length > 0
                          ? camp.assignedVolunteers.map((v) => v.fullName).join(', ')
                          : 'No volunteers assigned'}
                      </div>
                    </div>
                  </div>

                  {/* Card Action Controls */}
                  <div className="flex items-center justify-between pt-3 border-t border-slate-100">
                    <button
                      onClick={() => {
                        setSelectedCamp(camp);
                        setStaffAssignment({
                          doctorIds: camp.assignedDoctors?.map((d) => d.id) || [],
                          nurseIds: camp.assignedNurses?.map((n) => n.id) || [],
                          volunteerIds: camp.assignedVolunteers?.map((v) => v.id) || [],
                        });
                        setIsAssignStaffOpen(true);
                      }}
                      className="px-3.5 py-1.5 bg-teal-50 text-teal-700 font-bold text-xs rounded-xl hover:bg-teal-100 transition-colors flex items-center space-x-1.5 border border-teal-200"
                    >
                      <Users className="w-3.5 h-3.5" />
                      <span>Manage & Assign Staff</span>
                    </button>

                    <div className="flex items-center space-x-1">
                      <button
                        onClick={() => handleStatusChange(camp.id, 'ONGOING')}
                        className="px-2.5 py-1 text-xs font-bold text-emerald-700 bg-emerald-50 hover:bg-emerald-100 rounded-lg border border-emerald-200"
                      >
                        Start
                      </button>
                      <button
                        onClick={() => handleStatusChange(camp.id, 'COMPLETED')}
                        className="px-2.5 py-1 text-xs font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg border border-slate-200"
                      >
                        Complete
                      </button>
                    </div>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>
      )}

      {/* TAB 2: PATIENT HISTORY & MEDICAL TIMELINE */}
      {activeTab === 'PATIENT_HISTORY' && (
        <div className="grid grid-cols-1 lg:grid-cols-3 gap-8">
          
          {/* Patient Directory */}
          <div className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-4">
            <h2 className="text-base font-bold text-slate-800 flex items-center space-x-2">
              <User className="w-4 h-4 text-teal-600" />
              <span>Registered Patient Directory</span>
            </h2>

            <form onSubmit={handleSearchPatients} className="relative">
              <input
                type="text"
                value={patientSearch}
                onChange={(e) => setPatientSearch(e.target.value)}
                placeholder="Search patient name or ID..."
                className="w-full pl-9 pr-4 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-semibold text-slate-800 focus:outline-none focus:ring-2 focus:ring-teal-500/20"
              />
              <Search className="w-4 h-4 text-slate-400 absolute left-3 top-1/2 -translate-y-1/2" />
            </form>

            <div className="space-y-2 max-h-[500px] overflow-y-auto pr-1">
              {patientsList.length === 0 ? (
                <div className="text-center py-8 text-xs text-slate-400">No patients found.</div>
              ) : (
                patientsList.map((pt) => (
                  <button
                    key={pt.id}
                    onClick={() => handleSelectPatient(pt)}
                    className={`w-full text-left p-3 rounded-xl border transition-all ${
                      selectedPatient?.id === pt.id
                        ? 'bg-teal-50 border-teal-500 ring-2 ring-teal-500/20'
                        : 'bg-slate-50 border-slate-100 hover:bg-slate-100'
                    }`}
                  >
                    <div className="flex justify-between items-start">
                      <span className="text-xs font-mono font-bold text-teal-700">{pt.patientCode || `PT-${pt.id}`}</span>
                      <span className="text-[10px] font-bold text-slate-500">{pt.gender}, {pt.age} yrs</span>
                    </div>
                    <div className="font-bold text-slate-800 text-sm mt-0.5">{pt.fullName}</div>
                    <div className="text-[11px] text-slate-500 mt-1">{pt.phone || 'No phone'}</div>
                  </button>
                ))
              )}
            </div>
          </div>

          {/* Timeline View */}
          <div className="lg:col-span-2 bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-6">
            {selectedPatient ? (
              <div className="space-y-6">
                
                {/* Patient Profile Card */}
                <div className="p-4 bg-teal-50 border border-teal-200 rounded-xl flex items-center justify-between">
                  <div>
                    <span className="text-xs font-mono font-bold text-teal-700">{selectedPatient.patientCode || `PT-${selectedPatient.id}`}</span>
                    <h2 className="text-xl font-black text-slate-800">{selectedPatient.fullName}</h2>
                    <div className="text-xs text-slate-600 mt-1 space-x-3 font-semibold">
                      <span>Age: {selectedPatient.age} yrs</span>
                      <span>Gender: {selectedPatient.gender}</span>
                      <span>Blood Group: {selectedPatient.bloodGroup || 'N/A'}</span>
                    </div>
                  </div>
                  <div className="text-right text-xs text-slate-500 font-medium">
                    <div>Phone: {selectedPatient.phone || 'N/A'}</div>
                    <div>Address: {selectedPatient.address || 'N/A'}</div>
                  </div>
                </div>

                {/* Timeline */}
                <div>
                  <h3 className="text-sm font-bold text-slate-800 mb-4 flex items-center space-x-2">
                    <Activity className="w-4 h-4 text-teal-600" />
                    <span>Complete Medical Visit History & Timeline ({patientTimeline.length} events)</span>
                  </h3>

                  {patientTimeline.length === 0 ? (
                    <div className="text-center py-8 text-xs text-slate-400">No past medical records found for this patient.</div>
                  ) : (
                    <div className="space-y-4">
                      {patientTimeline.map((item, idx) => (
                        <div key={idx} className="p-4 bg-slate-50 rounded-xl border border-slate-200 space-y-2">
                          <div className="flex justify-between items-center border-b border-slate-200 pb-2">
                            <span className="text-xs font-bold text-teal-700 uppercase tracking-wider">{item.eventType || 'Medical Visit'}</span>
                            <span className="text-[11px] text-slate-500 font-medium">{item.eventDate || item.createdAt}</span>
                          </div>
                          <div className="text-xs font-bold text-slate-800">{item.title || item.summary || 'Consultation Record'}</div>
                          <p className="text-xs text-slate-600">{item.details || item.description || 'General Health Examination'}</p>
                        </div>
                      ))}
                    </div>
                  )}
                </div>

              </div>
            ) : (
              <div className="text-center py-16 text-slate-400 font-semibold text-xs">
                Select a patient from the directory on the left to inspect their complete medical history.
              </div>
            )}
          </div>

        </div>
      )}

      {/* Create Camp Modal */}
      <Modal isOpen={isAddCampOpen} onClose={() => setIsAddCampOpen(false)} title="Create New Medical Camp">
        <form onSubmit={handleCreateCamp} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Camp Title</label>
            <input
              type="text"
              required
              value={newCamp.title}
              onChange={(e) => setNewCamp({ ...newCamp, title: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
            />
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Description</label>
            <textarea
              rows={2}
              value={newCamp.description}
              onChange={(e) => setNewCamp({ ...newCamp, description: e.target.value })}
              className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-medium text-slate-800"
            />
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Start Date</label>
              <input
                type="date"
                required
                value={newCamp.startDate}
                onChange={(e) => setNewCamp({ ...newCamp, startDate: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">End Date</label>
              <input
                type="date"
                required
                value={newCamp.endDate}
                onChange={(e) => setNewCamp({ ...newCamp, endDate: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Daily Start Time</label>
              <input
                type="text"
                placeholder="e.g. 09:00 AM"
                required
                value={newCamp.startTime}
                onChange={(e) => setNewCamp({ ...newCamp, startTime: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Daily End Time</label>
              <input
                type="text"
                placeholder="e.g. 05:00 PM"
                required
                value={newCamp.endTime}
                onChange={(e) => setNewCamp({ ...newCamp, endTime: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Venue Location</label>
              <input
                type="text"
                required
                value={newCamp.location}
                onChange={(e) => setNewCamp({ ...newCamp, location: e.target.value, venue: e.target.value })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
            <div>
              <label className="block text-xs font-bold text-slate-700 mb-1">Patient Capacity</label>
              <input
                type="number"
                required
                value={newCamp.capacity}
                onChange={(e) => setNewCamp({ ...newCamp, capacity: parseInt(e.target.value) })}
                className="w-full px-3 py-2 text-sm border border-slate-200 rounded-xl font-semibold text-slate-800"
              />
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-3 bg-teal-600 text-white font-bold rounded-xl shadow-md hover:bg-teal-700 transition-colors mt-2"
          >
            Save & Publish Medical Camp
          </button>
        </form>
      </Modal>

      {/* Assign Staff Modal */}
      <Modal isOpen={isAssignStaffOpen} onClose={() => setIsAssignStaffOpen(false)} title={`Assign Staff to ${selectedCamp?.title}`}>
        <form onSubmit={handleAssignStaff} className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Select Doctors</label>
            <div className="space-y-1 max-h-36 overflow-y-auto border border-slate-200 p-2.5 rounded-xl bg-slate-50">
              {doctors.map((doc) => (
                <label key={doc.id} className="flex items-center space-x-2 text-xs font-medium text-slate-700 py-0.5">
                  <input
                    type="checkbox"
                    checked={staffAssignment.doctorIds.includes(doc.id)}
                    onChange={(e) => {
                      const updated = e.target.checked
                        ? [...staffAssignment.doctorIds, doc.id]
                        : staffAssignment.doctorIds.filter((id) => id !== doc.id);
                      setStaffAssignment({ ...staffAssignment, doctorIds: updated });
                    }}
                    className="rounded text-teal-600"
                  />
                  <span>{doc.fullName} ({doc.specialization || 'Doctor'})</span>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Select Nurses</label>
            <div className="space-y-1 max-h-36 overflow-y-auto border border-slate-200 p-2.5 rounded-xl bg-slate-50">
              {nurses.map((nurse) => (
                <label key={nurse.id} className="flex items-center space-x-2 text-xs font-medium text-slate-700 py-0.5">
                  <input
                    type="checkbox"
                    checked={staffAssignment.nurseIds.includes(nurse.id)}
                    onChange={(e) => {
                      const updated = e.target.checked
                        ? [...staffAssignment.nurseIds, nurse.id]
                        : staffAssignment.nurseIds.filter((id) => id !== nurse.id);
                      setStaffAssignment({ ...staffAssignment, nurseIds: updated });
                    }}
                    className="rounded text-teal-600"
                  />
                  <span>{nurse.fullName} ({nurse.department || 'Nurse'})</span>
                </label>
              ))}
            </div>
          </div>

          <div>
            <label className="block text-xs font-bold text-slate-700 mb-1">Select Volunteers</label>
            <div className="space-y-1 max-h-36 overflow-y-auto border border-slate-200 p-2.5 rounded-xl bg-slate-50">
              {volunteers.map((vol) => (
                <label key={vol.id} className="flex items-center space-x-2 text-xs font-medium text-slate-700 py-0.5">
                  <input
                    type="checkbox"
                    checked={staffAssignment.volunteerIds.includes(vol.id)}
                    onChange={(e) => {
                      const updated = e.target.checked
                        ? [...staffAssignment.volunteerIds, vol.id]
                        : staffAssignment.volunteerIds.filter((id) => id !== vol.id);
                      setStaffAssignment({ ...staffAssignment, volunteerIds: updated });
                    }}
                    className="rounded text-teal-600"
                  />
                  <span>{vol.fullName} (Registration Volunteer)</span>
                </label>
              ))}
            </div>
          </div>

          <button
            type="submit"
            className="w-full py-3 bg-teal-600 text-white font-bold rounded-xl shadow-md hover:bg-teal-700 transition-colors mt-2"
          >
            Save Staff Assignments
          </button>
        </form>
      </Modal>

    </div>
  );
};

export default OrganizerDashboard;
