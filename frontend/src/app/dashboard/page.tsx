'use client';

import { useEffect, useState } from 'react';
import { useRouter } from 'next/navigation';
import { authApi, userApi, type Profile, type Enrolment } from '@/lib/api';

const SUBJECTS = [
  'IGCSE Physics',
  'IGCSE Mathematics',
  'IGCSE Chemistry',
  'IGCSE Biology',
  'IAL Mathematics',
  'IAL Physics',
  'IAL Chemistry',
];

export default function DashboardPage() {
  const router  = useRouter();
  const [token,       setToken]       = useState<string | null>(null);
  const [profile,     setProfile]     = useState<Profile | null>(null);
  const [enrolments,  setEnrolments]  = useState<Enrolment[]>([]);
  const [newSubject,  setNewSubject]  = useState(SUBJECTS[0]);
  const [loading,     setLoading]     = useState(true);
  const [error,       setError]       = useState('');

  useEffect(() => {
    const t = sessionStorage.getItem('access_token');
    if (!t) { router.replace('/login'); return; }
    setToken(t);

    Promise.all([userApi.getProfile(t), userApi.getEnrolments(t)])
      .then(([p, e]) => { setProfile(p); setEnrolments(e); })
      .catch(() => router.replace('/login'))
      .finally(() => setLoading(false));
  }, [router]);

  async function handleEnrol() {
    if (!token) return;
    try {
      const e = await userApi.enrol(token, newSubject);
      setEnrolments((prev) => [...prev, e]);
    } catch (err: any) {
      setError(err.message ?? 'Could not enrol');
    }
  }

  async function handleWithdraw(subject: string) {
    if (!token) return;
    try {
      await userApi.withdraw(token, subject);
      setEnrolments((prev) => prev.filter((e) => e.subject !== subject));
    } catch (err: any) {
      setError(err.message ?? 'Could not withdraw');
    }
  }

  async function handleLogout() {
    if (token) await authApi.logout(token).catch(() => {});
    document.cookie = 'access_token=; path=/; max-age=0';
    sessionStorage.removeItem('access_token');
    router.replace('/login');
  }

  if (loading) {
    return (
      <main className="min-h-screen flex items-center justify-center bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900">
        <div className="text-indigo-400 animate-pulse text-lg font-medium">Loading…</div>
      </main>
    );
  }

  return (
    <main className="min-h-screen bg-gradient-to-br from-slate-900 via-indigo-950 to-slate-900 px-4 py-10">
      <div className="max-w-2xl mx-auto space-y-6">

        {/* Header */}
        <div className="flex items-center justify-between">
          <div>
            <h1 className="text-3xl font-bold text-white tracking-tight">
              {profile?.name ? `Hi, ${profile.name}` : 'Dashboard'}
            </h1>
            <p className="text-slate-400 text-sm mt-1">emBrain AI Study Assistant</p>
          </div>
          <button
            onClick={handleLogout}
            className="text-sm text-slate-400 hover:text-white border border-white/10 rounded-lg px-4 py-2 transition-colors"
          >
            Sign out
          </button>
        </div>

        {error && (
          <p className="text-red-400 text-sm bg-red-500/10 border border-red-500/20 rounded-lg px-4 py-2">
            {error}
          </p>
        )}

        {/* Profile card */}
        <section className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-6">
          <h2 className="text-lg font-semibold text-white mb-4">Your Profile</h2>
          <div className="space-y-2 text-sm text-slate-300">
            <div><span className="text-slate-500">Email:</span> {profile?.id}</div>
            <div><span className="text-slate-500">Name:</span> {profile?.name ?? <em className="text-slate-500">Not set</em>}</div>
            <div><span className="text-slate-500">Bio:</span> {profile?.bio ?? <em className="text-slate-500">Not set</em>}</div>
          </div>
        </section>

        {/* Enrolled subjects */}
        <section className="bg-white/5 backdrop-blur-md border border-white/10 rounded-2xl p-6">
          <h2 className="text-lg font-semibold text-white mb-4">Enrolled Subjects</h2>

          {enrolments.length === 0 ? (
            <p className="text-slate-500 text-sm">No subjects enrolled yet.</p>
          ) : (
            <ul className="space-y-2">
              {enrolments.map((e) => (
                <li key={e.id} className="flex items-center justify-between bg-white/5 rounded-lg px-4 py-2.5">
                  <span className="text-sm text-white">{e.subject}</span>
                  <button
                    onClick={() => handleWithdraw(e.subject)}
                    className="text-xs text-red-400 hover:text-red-300 transition-colors"
                  >
                    Withdraw
                  </button>
                </li>
              ))}
            </ul>
          )}

          <div className="mt-4 flex gap-2">
            <select
              value={newSubject}
              onChange={(e) => setNewSubject(e.target.value)}
              className="flex-1 bg-white/10 border border-white/20 text-white rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-indigo-500"
            >
              {SUBJECTS.map((s) => <option key={s} value={s} className="bg-slate-900">{s}</option>)}
            </select>
            <button
              onClick={handleEnrol}
              className="bg-indigo-600 hover:bg-indigo-500 text-white text-sm font-medium rounded-lg px-4 py-2 transition-colors"
            >
              Enrol
            </button>
          </div>
        </section>

      </div>
    </main>
  );
}
