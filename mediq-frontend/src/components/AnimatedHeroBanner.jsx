import React from 'react';
import {
  Activity,
  Stethoscope,
  HeartPulse,
  Pill,
  UserCheck,
  ShieldAlert,
  Building2,
  BarChart3,
  Sparkles,
  Zap,
  CheckCircle2,
  Clock,
  Award
} from 'lucide-react';

const PAGE_CONFIGS = {
  login: {
    gradient: 'from-teal-900 via-slate-900 to-emerald-950',
    accentColor: 'teal',
    badgeText: 'MediQ Enterprise Platform 2026',
    title: 'Digital Prescription & Medical Camp Operating System',
    subtitle: 'Streamlining registration, vitals triage, doctor consultations, inventory, and pharmacy distribution.',
    icon: Activity,
  },
  doctor: {
    gradient: 'from-emerald-900 via-teal-900 to-slate-950',
    accentColor: 'emerald',
    badgeText: 'Clinical Workspace Active',
    title: 'Doctor Consultation Portal',
    subtitle: 'Review patient history, record complaints, prescribe digital medications, and update diagnosis.',
    icon: Stethoscope,
  },
  nurse: {
    gradient: 'from-cyan-900 via-teal-950 to-slate-900',
    accentColor: 'cyan',
    badgeText: 'Vitals & Triage Station',
    title: 'Nurse Screening Console',
    subtitle: 'Record blood pressure, pulse, oxygen saturation, temperature, and triage patient queue.',
    icon: HeartPulse,
  },
  pharmacy: {
    gradient: 'from-indigo-900 via-slate-900 to-purple-950',
    accentColor: 'indigo',
    badgeText: 'Pharmacy Dispensing Hub',
    title: 'Medication Fulfillment Center',
    subtitle: 'Verify digital prescriptions, check stock levels, dispense medicines, and issue dosage instructions.',
    icon: Pill,
  },
  volunteer: {
    gradient: 'from-amber-900 via-slate-900 to-orange-950',
    accentColor: 'amber',
    badgeText: 'Registration Desk',
    title: 'Patient Intake & Queue Tokens',
    subtitle: 'Register new patients, issue unique token numbers, and assign appropriate screening counters.',
    icon: UserCheck,
  },
  admin: {
    gradient: 'from-blue-900 via-slate-900 to-indigo-950',
    accentColor: 'blue',
    badgeText: 'System Administration',
    title: 'User Access & Camp Security Console',
    subtitle: 'Manage user credentials, assign role permissions, monitor active camps, and audit logs.',
    icon: ShieldAlert,
  },
  organizer: {
    gradient: 'from-teal-950 via-emerald-900 to-slate-900',
    accentColor: 'emerald',
    badgeText: 'Camp Command Center',
    title: 'Medical Camp Management Hub',
    subtitle: 'Create camps, allocate medical staff, set up doctor booths, and monitor live camp statistics.',
    icon: Building2,
  },
  reports: {
    gradient: 'from-violet-900 via-slate-900 to-purple-950',
    accentColor: 'violet',
    badgeText: 'Analytics & Insights',
    title: 'Reports & Medical Intelligence',
    subtitle: 'Deep-dive patient demographics, common diagnoses, drug usage, and camp performance metrics.',
    icon: BarChart3,
  },
};

// Animated Vector Artwork per page type
const AnimatedIllustration = ({ type }) => {
  switch (type) {
    case 'doctor':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          {/* Glowing background circles */}
          <div className="absolute w-44 h-44 bg-emerald-500/20 rounded-full blur-2xl animate-pulse-glow" />
          <div className="absolute w-32 h-32 border-2 border-emerald-400/30 rounded-full animate-spin-slow" style={{ animationDuration: '18s' }} />

          {/* SVG Animated Heartbeat Line & Stethoscope */}
          <svg className="w-full h-full max-w-[280px] text-emerald-400 z-10 overflow-visible" viewBox="0 0 300 160">
            <path
              d="M 10,80 L 60,80 L 80,40 L 100,120 L 125,10 L 155,140 L 180,70 L 200,90 L 220,80 L 290,80"
              fill="none"
              stroke="currentColor"
              strokeWidth="4"
              strokeLinecap="round"
              strokeLinejoin="round"
              className="animate-heartbeat filter drop-shadow-[0_0_12px_rgba(52,211,153,0.8)]"
            />
          </svg>

          {/* Floating Card Badges */}
          <div className="absolute top-2 left-4 bg-slate-900/80 backdrop-blur-md border border-emerald-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-slow">
            <Stethoscope className="w-4 h-4 text-emerald-400" />
            <span className="text-xs font-semibold text-emerald-100">Rx Generator Active</span>
          </div>

          <div className="absolute bottom-3 right-4 bg-slate-900/80 backdrop-blur-md border border-teal-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <CheckCircle2 className="w-4 h-4 text-teal-400" />
            <span className="text-xs font-semibold text-teal-100">Direct Consultation</span>
          </div>
        </div>
      );

    case 'nurse':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-cyan-500/20 rounded-full blur-2xl animate-pulse-glow" />
          
          {/* Animated Vitals Radar Wave */}
          <div className="relative w-36 h-36 border border-cyan-400/40 rounded-full flex items-center justify-center">
            <div className="absolute w-24 h-24 border border-teal-400/60 rounded-full animate-ping opacity-30" />
            <HeartPulse className="w-16 h-16 text-cyan-300 animate-pulse drop-shadow-[0_0_15px_rgba(103,232,249,0.8)]" />
          </div>

          <div className="absolute top-3 right-2 bg-slate-900/80 backdrop-blur-md border border-cyan-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-slow">
            <Activity className="w-4 h-4 text-cyan-400" />
            <span className="text-xs font-semibold text-cyan-100">BP & Pulse Monitor</span>
          </div>

          <div className="absolute bottom-2 left-2 bg-slate-900/80 backdrop-blur-md border border-teal-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Zap className="w-4 h-4 text-amber-400" />
            <span className="text-xs font-semibold text-amber-100">Triage Priority</span>
          </div>
        </div>
      );

    case 'pharmacy':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-indigo-500/20 rounded-full blur-2xl animate-pulse-glow" />

          {/* Floating Pill Graphic with Orbit Ring */}
          <div className="relative w-36 h-36 flex items-center justify-center">
            <div className="absolute inset-0 border-2 border-dashed border-indigo-400/40 rounded-full animate-spin-slow" />
            <Pill className="w-16 h-16 text-indigo-300 transform -rotate-45 animate-float-slow drop-shadow-[0_0_15px_rgba(165,180,252,0.8)]" />
          </div>

          <div className="absolute top-2 left-2 bg-slate-900/80 backdrop-blur-md border border-indigo-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Pill className="w-4 h-4 text-purple-400" />
            <span className="text-xs font-semibold text-purple-100">Stock Inventory Synced</span>
          </div>

          <div className="absolute bottom-2 right-2 bg-slate-900/80 backdrop-blur-md border border-purple-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-slow">
            <CheckCircle2 className="w-4 h-4 text-indigo-400" />
            <span className="text-xs font-semibold text-indigo-100">Dosage Verified</span>
          </div>
        </div>
      );

    case 'volunteer':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-amber-500/20 rounded-full blur-2xl animate-pulse-glow" />

          <div className="relative w-32 h-32 bg-slate-900/90 border-2 border-amber-400/40 rounded-3xl p-4 shadow-2xl flex flex-col items-center justify-center animate-float-slow">
            <UserCheck className="w-10 h-10 text-amber-400 mb-1" />
            <span className="text-xs font-bold text-amber-200">Token Scanner</span>
            <span className="text-lg font-black text-amber-300 animate-pulse">Q-104</span>
          </div>

          <div className="absolute top-2 right-3 bg-slate-900/80 backdrop-blur-md border border-orange-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Clock className="w-4 h-4 text-orange-400" />
            <span className="text-xs font-semibold text-orange-100">Fast Token Generation</span>
          </div>
        </div>
      );

    case 'admin':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-blue-500/20 rounded-full blur-2xl animate-pulse-glow" />

          {/* Shield Guard Vector */}
          <div className="relative w-32 h-32 flex items-center justify-center border-2 border-blue-400/30 rounded-2xl animate-float-slow">
            <ShieldAlert className="w-16 h-16 text-blue-300 drop-shadow-[0_0_15px_rgba(147,197,253,0.8)]" />
          </div>

          <div className="absolute top-3 left-4 bg-slate-900/80 backdrop-blur-md border border-blue-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Award className="w-4 h-4 text-blue-400" />
            <span className="text-xs font-semibold text-blue-100">RBAC Security Active</span>
          </div>
        </div>
      );

    case 'organizer':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-emerald-500/20 rounded-full blur-2xl animate-pulse-glow" />

          <div className="relative w-32 h-32 border-2 border-emerald-400/40 rounded-3xl p-4 flex flex-col items-center justify-center animate-float-slow bg-slate-900/70 backdrop-blur">
            <Building2 className="w-10 h-10 text-emerald-400 mb-1" />
            <span className="text-xs font-bold text-emerald-200">Camp Setup</span>
          </div>

          <div className="absolute bottom-3 right-4 bg-slate-900/80 backdrop-blur-md border border-teal-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Sparkles className="w-4 h-4 text-emerald-400" />
            <span className="text-xs font-semibold text-emerald-100">Live Logistics</span>
          </div>
        </div>
      );

    case 'reports':
      return (
        <div className="relative w-full h-48 md:h-56 flex items-center justify-center">
          <div className="absolute w-44 h-44 bg-violet-500/20 rounded-full blur-2xl animate-pulse-glow" />

          {/* Animated Bar Chart Graphic */}
          <div className="w-36 h-28 bg-slate-900/80 backdrop-blur border border-violet-400/30 rounded-2xl p-3 flex items-end justify-between space-x-2 animate-float-slow">
            <div className="w-5 bg-violet-500 rounded-t h-1/2 animate-pulse" style={{ animationDelay: '0.1s' }} />
            <div className="w-5 bg-purple-500 rounded-t h-3/4 animate-pulse" style={{ animationDelay: '0.3s' }} />
            <div className="w-5 bg-indigo-500 rounded-t h-full animate-pulse" style={{ animationDelay: '0.5s' }} />
            <div className="w-5 bg-teal-400 rounded-t h-2/3 animate-pulse" style={{ animationDelay: '0.7s' }} />
          </div>

          <div className="absolute top-2 left-2 bg-slate-900/80 backdrop-blur-md border border-violet-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <BarChart3 className="w-4 h-4 text-violet-400" />
            <span className="text-xs font-semibold text-violet-100">Real-time Insights</span>
          </div>
        </div>
      );

    case 'login':
    default:
      return (
        <div className="relative w-full h-56 md:h-64 flex items-center justify-center">
          <div className="absolute w-56 h-56 bg-teal-500/20 rounded-full blur-3xl animate-pulse-glow" />
          <div className="absolute w-40 h-40 border-2 border-emerald-400/30 rounded-full animate-spin-slow" style={{ animationDuration: '24s' }} />

          <div className="relative z-10 flex flex-col items-center">
            <div className="w-20 h-20 bg-gradient-to-tr from-teal-500 to-emerald-400 rounded-3xl flex items-center justify-center shadow-2xl shadow-teal-500/40 animate-float-slow mb-3">
              <Activity className="w-12 h-12 text-white stroke-[2.5]" />
            </div>
            <span className="text-sm font-black tracking-widest text-emerald-300 uppercase">MediQ Platform</span>
          </div>

          <div className="absolute top-4 left-4 bg-slate-900/80 backdrop-blur-md border border-teal-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-reverse">
            <Sparkles className="w-4 h-4 text-teal-400" />
            <span className="text-xs font-semibold text-teal-100">Smart Medical Workflow</span>
          </div>

          <div className="absolute bottom-4 right-4 bg-slate-900/80 backdrop-blur-md border border-emerald-500/30 px-3 py-1.5 rounded-xl shadow-lg flex items-center space-x-2 animate-float-slow">
            <CheckCircle2 className="w-4 h-4 text-emerald-400" />
            <span className="text-xs font-semibold text-emerald-100">Role Secured</span>
          </div>
        </div>
      );
  }
};

export const AnimatedHeroBanner = ({
  type = 'doctor',
  title,
  subtitle,
  badgeText,
  stats = []
}) => {
  const config = PAGE_CONFIGS[type] || PAGE_CONFIGS.doctor;
  const MainIcon = config.icon;

  const displayTitle = title || config.title;
  const displaySubtitle = subtitle || config.subtitle;
  const displayBadge = badgeText || config.badgeText;

  return (
    <div className={`relative overflow-hidden rounded-3xl bg-gradient-to-br ${config.gradient} text-white shadow-2xl border border-slate-700/50 mb-8 p-6 md:p-8`}>
      {/* Background Animated Grid Overlay */}
      <div className="absolute inset-0 bg-[linear-gradient(to_right,#ffffff0a_1px,transparent_1px),linear-gradient(to_bottom,#ffffff0a_1px,transparent_1px)] bg-[size:24px_24px] pointer-events-none" />

      {/* Light Glow Circles */}
      <div className="absolute -top-24 -left-24 w-64 h-64 bg-teal-500/10 rounded-full blur-3xl" />
      <div className="absolute -bottom-24 -right-24 w-64 h-64 bg-emerald-500/10 rounded-full blur-3xl" />

      <div className="relative z-10 grid grid-cols-1 lg:grid-cols-12 gap-6 items-center">
        {/* Left Column: Text & Stats */}
        <div className="lg:col-span-7 space-y-4">
          <div className="inline-flex items-center space-x-2 px-3 py-1.5 rounded-full bg-white/10 backdrop-blur-md border border-white/15 text-xs font-bold tracking-wide text-emerald-200">
            <Sparkles className="w-3.5 h-3.5 text-emerald-300" />
            <span>{displayBadge}</span>
          </div>

          <div className="flex items-start space-x-3">
            <div className="p-3 bg-white/10 backdrop-blur-md rounded-2xl border border-white/15 shadow-inner mt-1 hidden sm:block">
              <MainIcon className="w-7 h-7 text-emerald-300" />
            </div>
            <div>
              <h1 className="text-2xl md:text-3xl font-black tracking-tight text-white leading-tight">
                {displayTitle}
              </h1>
              <p className="text-slate-300 text-sm mt-1.5 leading-relaxed max-w-2xl">
                {displaySubtitle}
              </p>
            </div>
          </div>

          {/* Quick Stats Pill List */}
          {stats && stats.length > 0 && (
            <div className="pt-2 flex flex-wrap gap-3">
              {stats.map((stat, idx) => {
                const StatIcon = stat.icon || Activity;
                return (
                  <div
                    key={idx}
                    className="flex items-center space-x-2 px-3 py-1.5 rounded-xl bg-slate-900/60 backdrop-blur-md border border-white/10 text-xs shadow-md"
                  >
                    <StatIcon className="w-4 h-4 text-emerald-400" />
                    <span className="text-slate-400 font-medium">{stat.label}:</span>
                    <span className="font-bold text-white">{stat.value}</span>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right Column: Animated Vector Graphic */}
        <div className="lg:col-span-5 flex justify-center">
          <AnimatedIllustration type={type} />
        </div>
      </div>
    </div>
  );
};

export default AnimatedHeroBanner;
