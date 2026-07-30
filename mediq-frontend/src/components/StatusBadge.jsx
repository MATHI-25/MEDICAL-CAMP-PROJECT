import React from 'react';

export const StatusBadge = ({ status, type = 'queue' }) => {
  if (!status) return null;

  let colorClasses = 'bg-slate-100 text-slate-700 border-slate-200';

  const s = String(status).toUpperCase();

  if (s === 'WAITING' || s === 'UPCOMING' || s === 'CREATED') {
    colorClasses = 'bg-amber-50 text-amber-700 border-amber-200';
  } else if (s === 'IN_VITALS' || s === 'WAITING_FOR_DOCTOR' || s === 'IN_CONSULTATION' || s === 'ONGOING' || s === 'UNDER_TREATMENT' || s === 'SENT') {
    colorClasses = 'bg-teal-50 text-teal-700 border-teal-200 font-medium';
  } else if (s === 'COMPLETED' || s === 'DISPENSED' || s === 'VISITED') {
    colorClasses = 'bg-emerald-50 text-emerald-700 border-emerald-200 font-semibold';
  } else if (s === 'CANCELLED' || s === 'CRITICAL' || s === 'DEACTIVATED') {
    colorClasses = 'bg-rose-50 text-rose-700 border-rose-200 font-semibold';
  } else if (s === 'SENT_TO_PHARMACY' || s === 'PARTIALLY_DISPENSED') {
    colorClasses = 'bg-sky-50 text-sky-700 border-sky-200 font-medium';
  } else if (s === 'REFERRED_TO_HOSPITAL' || s === 'URGENT') {
    colorClasses = 'bg-purple-50 text-purple-700 border-purple-200 font-semibold';
  }

  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs border ${colorClasses}`}>
      <span className="w-1.5 h-1.5 mr-1.5 rounded-full bg-current opacity-75"></span>
      {s.replace(/_/g, ' ')}
    </span>
  );
};

export default StatusBadge;
