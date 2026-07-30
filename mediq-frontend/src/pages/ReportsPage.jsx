import React, { useState, useEffect } from 'react';
import reportService from '../services/reportService';
import campService from '../services/campService';
import pharmacyService from '../services/pharmacyService';
import { useAuth } from '../context/AuthContext';
import StatusBadge from '../components/StatusBadge';
import {
  BarChart3,
  FileSpreadsheet,
  Users,
  Stethoscope,
  Pill,
  Download,
  Package,
  AlertTriangle,
  Building2,
  Clock,
  CheckCircle2
} from 'lucide-react';
import AnimatedHeroBanner from '../components/AnimatedHeroBanner';

export const ReportsPage = () => {
  const { user } = useAuth();
  const isPharmacist = user?.role === 'PHARMACY';
  const isOrganizerOrAdmin = user?.role === 'ORGANIZER' || user?.role === 'SYSTEM_ADMIN';

  const [camps, setCamps] = useState([]);
  const [selectedCampId, setSelectedCampId] = useState('');
  const [analytics, setAnalytics] = useState(null);
  const [doctorReports, setDoctorReports] = useState([]);
  const [medicineReports, setMedicineReports] = useState([]);
  const [dispenseHistory, setDispenseHistory] = useState([]);
  const [referralReport, setReferralReport] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetchCamps();
  }, []);

  useEffect(() => {
    if (selectedCampId || isPharmacist) {
      fetchReportData();
    }
  }, [selectedCampId]);

  const fetchCamps = async () => {
    try {
      const res = await campService.searchCamps(null, '', 0, 50);
      const list = res.data?.content || res.content || res.data || [];
      const arr = Array.isArray(list) ? list : [];
      setCamps(arr);

      if (arr.length > 0) {
        // Default to ongoing camp if available, otherwise first camp
        const ongoing = arr.find((c) => c.status === 'ONGOING') || arr[0];
        setSelectedCampId(ongoing.id);
      }
    } catch (e) {
      console.error(e);
    }
  };

  const fetchReportData = async () => {
    setLoading(true);
    try {
      if (selectedCampId) {
        const aRes = await reportService.getCampAnalytics(selectedCampId);
        setAnalytics(aRes.data || aRes || null);
      }

      // Fetch Doctor Workload only for Non-Pharmacists
      if (!isPharmacist && selectedCampId) {
        try {
          const dRes = await reportService.getDoctorWorkloadReport(selectedCampId);
          const docs = dRes.data || dRes;
          setDoctorReports(Array.isArray(docs) ? docs : []);
        } catch (err) {
          console.error('Doctor workload report skipped', err);
          setDoctorReports([]);
        }
      }

      // Fetch Medicine Stock Consumption Report
      try {
        const mRes = await reportService.getMedicineConsumptionReport();
        const meds = mRes.data || mRes;
        setMedicineReports(Array.isArray(meds) ? meds : []);
      } catch (err) {
        console.error('Medicine report skipped', err);
      }

      // Fetch Pharmacist Dispense History
      try {
        const dispRes = await pharmacyService.getPharmacistDispenseHistory();
        const dispData = dispRes.data || dispRes;
        setDispenseHistory(Array.isArray(dispData) ? dispData : []);
      } catch (err) {
        console.error('Dispense history skipped', err);
      }

      // Fetch Referral Report
      if (selectedCampId) {
        try {
          const rRes = await reportService.getReferralReport(selectedCampId);
          setReferralReport(rRes.data || rRes || null);
        } catch (err) {
          console.error('Referral report skipped', err);
        }
      }
    } catch (e) {
      console.error(e);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="space-y-6">
      {/* Top Action Bar */}
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 bg-white p-4 rounded-2xl border border-slate-100 shadow-xs">
        <div>
          <h2 className="text-sm font-bold text-slate-700">
            {isPharmacist ? 'Pharmacy & Medicine Dispense Reports' : 'Master Analytics Workspace'}
          </h2>
          <p className="text-slate-400 text-xs">
            {isPharmacist
              ? 'Track medicine inventory consumption & pharmacy dispense history'
              : 'Filter by camp & export CSV audit reports'}
          </p>
        </div>

        <div className="flex flex-wrap items-center gap-3">
          {camps.length > 0 && (
            <select
              value={selectedCampId}
              onChange={(e) => setSelectedCampId(e.target.value)}
              className="px-3.5 py-2 bg-slate-50 border border-slate-200 rounded-xl text-xs font-bold text-slate-700 shadow-xs focus:outline-none focus:ring-2 focus:ring-violet-500/20"
            >
              {camps.map((c) => (
                <option key={c.id} value={c.id}>
                  {c.title} ({c.campCode})
                </option>
              ))}
            </select>
          )}

          {!isPharmacist && (
            <button
              onClick={async () => {
                try {
                  const res = await reportService.downloadPatientsCsv(selectedCampId);
                  const rawData = res.data || res;
                  const blob = rawData instanceof Blob ? rawData : new Blob([rawData], { type: 'text/csv' });
                  const url = window.URL.createObjectURL(blob);
                  const link = document.createElement('a');
                  link.href = url;
                  link.setAttribute('download', `Patients-Report-${selectedCampId || 'all'}.csv`);
                  document.body.appendChild(link);
                  link.click();
                  link.remove();
                  setTimeout(() => window.URL.revokeObjectURL(url), 100);
                } catch (e) {
                  alert(e?.message || 'Failed to export Patients CSV');
                }
              }}
              className="px-4 py-2 bg-gradient-to-r from-violet-600 to-purple-600 hover:opacity-95 text-white font-bold text-xs rounded-xl shadow-md shadow-violet-600/20 flex items-center space-x-2 transition-all"
            >
              <Download className="w-4 h-4" />
              <span>Export Patients CSV</span>
            </button>
          )}

          <button
            onClick={async () => {
              try {
                const res = await reportService.downloadMedicinesCsv();
                const rawData = res.data || res;
                const blob = rawData instanceof Blob ? rawData : new Blob([rawData], { type: 'text/csv' });
                const url = window.URL.createObjectURL(blob);
                const link = document.createElement('a');
                link.href = url;
                link.setAttribute('download', 'Medicine-Inventory-Report.csv');
                document.body.appendChild(link);
                link.click();
                link.remove();
                setTimeout(() => window.URL.revokeObjectURL(url), 100);
              } catch (e) {
                alert(e?.message || 'Failed to export Medicines CSV');
              }
            }}
            className="px-4 py-2 bg-slate-800 hover:bg-slate-900 text-white font-bold text-xs rounded-xl shadow-md flex items-center space-x-2 transition-all"
          >
            <FileSpreadsheet className="w-4 h-4" />
            <span>Export Medicines CSV</span>
          </button>
        </div>
      </div>

      {/* Hero Animated Banner */}
      <AnimatedHeroBanner
        type={isPharmacist ? 'pharmacy' : 'reports'}
        stats={[
          {
            label: 'Total Registered Patients',
            value: `${analytics?.totalPatientsRegistered || 0} Patients`,
          },
          {
            label: 'Queue Completion Rate',
            value: `${analytics?.completionRatePercentage || 0}%`,
          },
        ]}
      />

      {/* Camp Analytics Stat Cards */}
      {analytics && (
        <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs space-y-1">
            <div className="text-xs font-bold text-slate-400 uppercase">Registered Patients</div>
            <div className="text-3xl font-black text-slate-800">{analytics.totalPatientsRegistered}</div>
            <div className="text-xs font-medium text-teal-600">Total Patient Intake</div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs space-y-1">
            <div className="text-xs font-bold text-slate-400 uppercase">Doctor Consultations</div>
            <div className="text-3xl font-black text-slate-800">{analytics.totalConsultationsCompleted}</div>
            <div className="text-xs font-medium text-teal-600">Diagnoses Completed</div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs space-y-1">
            <div className="text-xs font-bold text-slate-400 uppercase">Prescriptions Issued</div>
            <div className="text-3xl font-black text-slate-800">{analytics.totalPrescriptionsIssued}</div>
            <div className="text-xs font-medium text-teal-600">Digital RX Issued</div>
          </div>

          <div className="bg-white p-6 rounded-2xl border border-slate-100 shadow-xs space-y-1">
            <div className="text-xs font-bold text-slate-400 uppercase">Queue Completion Rate</div>
            <div className="text-3xl font-black text-emerald-600">{analytics.completionRatePercentage}%</div>
            <div className="text-xs font-medium text-slate-500">Efficiency Score</div>
          </div>
        </div>
      )}

      {/* DOCTOR WORKLOAD REPORT: Visible to Organizer, Admin, Doctor (Hidden for Pharmacist) */}
      {!isPharmacist && (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
          <div className="p-6 border-b border-slate-100 flex justify-between items-center">
            <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
              <Stethoscope className="w-5 h-5 text-teal-600" />
              <span>Doctor Clinical Workload Breakdown</span>
            </h2>
            <span className="text-xs font-bold font-mono text-teal-700 bg-teal-50 px-3 py-1 rounded-xl">
              {doctorReports.length} Active Medical Officers
            </span>
          </div>

          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse">
              <thead>
                <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                  <th className="py-3 px-6">Member ID</th>
                  <th className="py-3 px-6">Doctor Name</th>
                  <th className="py-3 px-6">Specialization</th>
                  <th className="py-3 px-6">Consultations</th>
                  <th className="py-3 px-6">Prescriptions</th>
                  <th className="py-3 px-6">Referrals</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100 text-sm">
                {doctorReports.map((d) => (
                  <tr key={d.doctorId} className="hover:bg-slate-50/50 transition-colors">
                    <td className="py-4 px-6 font-mono font-bold text-teal-700">{d.doctorMemberId}</td>
                    <td className="py-4 px-6 font-bold text-slate-800">{d.doctorName}</td>
                    <td className="py-4 px-6 text-slate-600">{d.specialization}</td>
                    <td className="py-4 px-6 font-bold text-slate-800">{d.totalConsultations}</td>
                    <td className="py-4 px-6 text-slate-700 font-semibold">{d.totalPrescriptions}</td>
                    <td className="py-4 px-6 text-purple-700 font-bold">{d.totalReferrals}</td>
                  </tr>
                ))}
                {doctorReports.length === 0 && (
                  <tr>
                    <td colSpan={6} className="text-center py-6 text-slate-400 text-xs">
                      No doctor workload data recorded for this camp.
                    </td>
                  </tr>
                )}
              </tbody>
            </table>
          </div>
        </div>
      )}

      {/* PHARMACY MEDICINE STOCK CONSUMPTION REPORT */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
            <Pill className="w-5 h-5 text-indigo-600" />
            <span>Pharmacy & Medicine Stock Consumption Report</span>
          </h2>
          <span className="text-xs font-bold font-mono text-indigo-700 bg-indigo-50 px-3 py-1 rounded-xl">
            {medicineReports.length} Stock Categories
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-6">Code</th>
                <th className="py-3 px-6">Medicine Name</th>
                <th className="py-3 px-6">Category</th>
                <th className="py-3 px-6">Current Stock</th>
                <th className="py-3 px-6">Min Alert Qty</th>
                <th className="py-3 px-6">Stock Status</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {medicineReports.map((m) => (
                <tr key={m.medicineId || m.medicineCode} className="hover:bg-slate-50/50 transition-colors">
                  <td className="py-4 px-6 font-mono font-bold text-indigo-700">{m.medicineCode}</td>
                  <td className="py-4 px-6 font-bold text-slate-800">{m.name}</td>
                  <td className="py-4 px-6 text-slate-600">{m.category}</td>
                  <td className="py-4 px-6 font-black text-slate-800">{m.currentStock}</td>
                  <td className="py-4 px-6 text-slate-500 font-mono">{m.minAlertQuantity}</td>
                  <td className="py-4 px-6">
                    <StatusBadge
                      status={m.isLowStock || m.currentStock <= m.minAlertQuantity ? 'CRITICAL' : 'COMPLETED'}
                    />
                  </td>
                </tr>
              ))}
              {medicineReports.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center py-6 text-slate-400 text-xs">
                    No medicine inventory consumption data recorded.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* MEDICINE DISPENSE HISTORY REPORT */}
      <div className="bg-white rounded-2xl border border-slate-100 shadow-xs overflow-hidden">
        <div className="p-6 border-b border-slate-100 flex justify-between items-center">
          <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2">
            <Package className="w-5 h-5 text-emerald-600" />
            <span>Medicine Dispense Audit History</span>
          </h2>
          <span className="text-xs font-bold font-mono text-emerald-700 bg-emerald-50 px-3 py-1 rounded-xl">
            {dispenseHistory.length} Recorded Dispenses
          </span>
        </div>

        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-slate-50 border-b border-slate-100 text-xs font-bold text-slate-400 uppercase tracking-wider">
                <th className="py-3 px-6">Prescription Code</th>
                <th className="py-3 px-6">Medicine Dispensed</th>
                <th className="py-3 px-6">Qty Dispensed</th>
                <th className="py-3 px-6">Pharmacist</th>
                <th className="py-3 px-6">Dispense Date</th>
                <th className="py-3 px-6">Remarks</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100 text-sm">
              {dispenseHistory.map((rec) => (
                <tr key={rec.id} className="hover:bg-slate-50/50 transition-colors">
                  <td className="py-4 px-6 font-mono font-bold text-emerald-700">{rec.prescriptionCode}</td>
                  <td className="py-4 px-6 font-bold text-slate-800">{rec.medicineName}</td>
                  <td className="py-4 px-6 font-black text-slate-900">{rec.quantityDispensed} Units</td>
                  <td className="py-4 px-6 text-slate-700">{rec.pharmacistName}</td>
                  <td className="py-4 px-6 text-slate-500 text-xs font-mono">
                    {new Date(rec.dispenseDate).toLocaleString()}
                  </td>
                  <td className="py-4 px-6 text-slate-600 text-xs">{rec.remarks || 'Standard dispense'}</td>
                </tr>
              ))}
              {dispenseHistory.length === 0 && (
                <tr>
                  <td colSpan={6} className="text-center py-6 text-slate-400 text-xs">
                    No medicine dispense history recorded yet.
                  </td>
                </tr>
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* OVERALL REFERRAL ANALYTICS REPORT (For Admin / Organizer) */}
      {referralReport && !isPharmacist && (
        <div className="bg-white rounded-2xl border border-slate-100 shadow-xs p-6 space-y-4">
          <h2 className="text-lg font-bold text-slate-800 flex items-center space-x-2 border-b border-slate-100 pb-3">
            <Building2 className="w-5 h-5 text-purple-600" />
            <span>Hospital Referral Analytics Summary</span>
          </h2>

          <div className="grid grid-cols-2 md:grid-cols-6 gap-4 text-center">
            <div className="bg-purple-50 p-4 rounded-xl border border-purple-100">
              <div className="text-xs text-purple-700 font-bold uppercase">Total Referrals</div>
              <div className="text-2xl font-black text-purple-900 mt-1">{referralReport.totalReferrals}</div>
            </div>
            <div className="bg-amber-50 p-4 rounded-xl border border-amber-100">
              <div className="text-xs text-amber-700 font-bold uppercase">Created</div>
              <div className="text-2xl font-black text-amber-900 mt-1">{referralReport.createdCount}</div>
            </div>
            <div className="bg-blue-50 p-4 rounded-xl border border-blue-100">
              <div className="text-xs text-blue-700 font-bold uppercase">Dispatched</div>
              <div className="text-2xl font-black text-blue-900 mt-1">{referralReport.sentCount}</div>
            </div>
            <div className="bg-cyan-50 p-4 rounded-xl border border-cyan-100">
              <div className="text-xs text-cyan-700 font-bold uppercase">Visited</div>
              <div className="text-2xl font-black text-cyan-900 mt-1">{referralReport.visitedCount}</div>
            </div>
            <div className="bg-emerald-50 p-4 rounded-xl border border-emerald-100">
              <div className="text-xs text-emerald-700 font-bold uppercase">Completed</div>
              <div className="text-2xl font-black text-emerald-900 mt-1">{referralReport.completedCount}</div>
            </div>
            <div className="bg-rose-50 p-4 rounded-xl border border-rose-100">
              <div className="text-xs text-rose-700 font-bold uppercase">Critical Cases</div>
              <div className="text-2xl font-black text-rose-900 mt-1">{referralReport.criticalCount}</div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default ReportsPage;
