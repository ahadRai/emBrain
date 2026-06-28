const API_BASE = process.env.NEXT_PUBLIC_API_URL ?? 'http://localhost/api/v1';

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  const res = await fetch(`${API_BASE}${path}`, {
    credentials: 'include',   // send httpOnly cookies (refresh_token)
    headers: { 'Content-Type': 'application/json', ...init?.headers },
    ...init,
  });

  if (!res.ok) {
    const body = await res.json().catch(() => ({}));
    throw { status: res.status, message: body.message ?? 'Request failed' };
  }

  if (res.status === 204) return undefined as T;
  return res.json();
}

// ── Auth ─────────────────────────────────────────────────────────────────────

export interface AuthResponse {
  accessToken: string;
  expiresIn: number;
  role: string;
}

export const authApi = {
  register: (email: string, password: string) =>
    request<{ message: string }>('/auth/register', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  login: (email: string, password: string) =>
    request<AuthResponse>('/auth/login', {
      method: 'POST',
      body: JSON.stringify({ email, password }),
    }),

  logout: (accessToken: string) =>
    request<{ message: string }>('/auth/logout', {
      method: 'POST',
      headers: { Authorization: `Bearer ${accessToken}` },
    }),

  refresh: () => request<AuthResponse>('/auth/refresh', { method: 'POST' }),
};

// ── User ─────────────────────────────────────────────────────────────────────

export interface Profile {
  id: string;
  name: string | null;
  bio: string | null;
  createdAt: string;
  updatedAt: string;
}

export interface Enrolment {
  id: string;
  userId: string;
  subject: string;
  enrolledAt: string;
}

export const userApi = {
  getProfile: (accessToken: string) =>
    request<Profile>('/users/profile', {
      headers: { Authorization: `Bearer ${accessToken}` },
    }),

  updateProfile: (accessToken: string, data: { name?: string; bio?: string }) =>
    request<Profile>('/users/profile', {
      method: 'PUT',
      headers: { Authorization: `Bearer ${accessToken}` },
      body: JSON.stringify(data),
    }),

  getEnrolments: (accessToken: string) =>
    request<Enrolment[]>('/users/enrolments', {
      headers: { Authorization: `Bearer ${accessToken}` },
    }),

  enrol: (accessToken: string, subject: string) =>
    request<Enrolment>('/users/enrolments', {
      method: 'POST',
      headers: { Authorization: `Bearer ${accessToken}` },
      body: JSON.stringify({ subject }),
    }),

  withdraw: (accessToken: string, subject: string) =>
    request<void>(`/users/enrolments/${encodeURIComponent(subject)}`, {
      method: 'DELETE',
      headers: { Authorization: `Bearer ${accessToken}` },
    }),
};
