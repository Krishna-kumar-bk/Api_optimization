import React, { useState, useEffect, useMemo, useCallback } from 'react';
import api from './api';
import { 
  Activity, Zap, CheckCircle, AlertCircle, RefreshCw, Gauge, 
  PlayCircle, ShieldAlert, Trophy, Globe, Laptop, Fingerprint, X, Search,
  UserPlus, LogIn
} from 'lucide-react';
import { 
  PieChart, Pie, Cell, Tooltip as ReTooltip, ResponsiveContainer, 
  BarChart, Bar, XAxis, YAxis, CartesianGrid
} from 'recharts';

function App() {
  const [token, setToken] = useState(sessionStorage.getItem('token'));
  const [user, setUser] = useState(null); 
  const [credentials, setCredentials] = useState({ email: '', password: '' });
  const [isRegistering, setIsRegistering] = useState(false); // 🔥 NEW: Toggle State
  
  const [stats, setStats] = useState({ 
    totalRequests: 0, avgLatency: 0, successRate: 0, totalErrors: 0, 
    fastRequests: 0, slowRequests: 0, status2xx: 0, status4xx: 0, status5xx: 0,
    topEndpoints: {}, timelineData: [], geoDistribution: {}, browserDistribution: {}
  });

  const [recentLogs, setRecentLogs] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [statusFilter, setStatusFilter] = useState("all");
  const [page, setPage] = useState(0);

  const [traceSearchId, setTraceSearchId] = useState("");
  const [foundTrace, setFoundTrace] = useState(null);

  const BROWSER_COLORS = ['#38bdf8', '#818cf8', '#fbbf24', '#f87171', '#34d399'];

  const handleLogin = async (e) => {
    e.preventDefault();
    try {
      localStorage.clear(); sessionStorage.clear();
      const res = await api.post('/auth/login', credentials);
      if (res.data.token) {
        sessionStorage.setItem('token', res.data.token);
        setToken(res.data.token);
      }
    } catch (err) { alert("Login Failed! Check your email and password."); }
  };

  // 🔥 NEW: Handle Registration
  const handleRegister = async (e) => {
    e.preventDefault();
    try {
      await api.post('/auth/register', credentials);
      alert("Registration Successful! You can now login.");
      setIsRegistering(false); // Switch back to login view
    } catch (err) { 
      alert("Registration Failed! This email might already be taken."); 
    }
  };

  const handleLogout = useCallback(() => {
    sessionStorage.clear(); localStorage.clear();
    setToken(null); setUser(null);
  }, []);

  const loadData = useCallback(async () => {
    const currentToken = sessionStorage.getItem('token');
    if (!currentToken || currentToken === 'null') return;

    try {
      const [profileRes, statsRes, logsRes] = await Promise.all([
        api.get('/auth/profile'),
        api.get('/api/analytics/dashboard'),
        api.get(`/api/monitoring/logs?page=${page}&size=20`)
      ]);
      
      setUser(profileRes.data); 
      setStats(statsRes.data);
      setRecentLogs(logsRes.data.content || []);
    } catch (err) { 
        if(err.response?.status === 401) handleLogout(); 
    }
  }, [page, handleLogout]);

  const runManualTest = async (endpoint) => {
    try { 
        await api.get(endpoint); 
        setTimeout(loadData, 500);
    } catch (err) { 
        setTimeout(loadData, 500); 
    }
  };

  const handleTraceSearch = async (e) => {
    e.preventDefault();
    if (!traceSearchId || !traceSearchId.startsWith("TR-")) {
        alert("Enter a valid Trace ID (e.g. TR-A1B2C3D4)");
        return;
    }
    try {
        const res = await api.get(`/api/monitoring/trace/${traceSearchId}`);
        setFoundTrace(res.data);
    } catch (err) {
        alert("Trace ID not found.");
    }
  };

  useEffect(() => {
    if (token) loadData();
  }, [token, loadData]);

  const browserData = useMemo(() => {
    return Object.entries(stats.browserDistribution || {}).map(([name, value]) => ({ 
      name, value: Number(value) 
    }));
  }, [stats.browserDistribution]);

  const filteredLogs = useMemo(() => {
    return recentLogs.filter(log => {
      const matchesSearch = log.endpoint.toLowerCase().includes(searchTerm.toLowerCase());
      const matchesStatus = statusFilter === "all" ? true : statusFilter === "errors" ? log.statusCode >= 400 : log.statusCode < 400;
      return matchesSearch && matchesStatus;
    });
  }, [recentLogs, searchTerm, statusFilter]);

  if (!token) {
    return (
      <div style={styles.container}>
        <div style={styles.loginBox}>
          <div style={{ textAlign: 'center', marginBottom: '20px' }}>
             <Activity size={40} color="#38bdf8" style={{ marginBottom: '10px' }} />
             <h2 style={{ color: 'white', margin: 0 }}>{isRegistering ? 'Join API Monitor' : 'API Monitor'}</h2>
          </div>
          
          <form onSubmit={isRegistering ? handleRegister : handleLogin} style={styles.form}>
            <div style={styles.inputWrapper}>
              <input 
                type="email" 
                placeholder="Email Address" 
                onChange={(e) => setCredentials({...credentials, email: e.target.value})} 
                style={styles.input} 
                required 
              />
            </div>
            <div style={styles.inputWrapper}>
              <input 
                type="password" 
                placeholder="Password" 
                onChange={(e) => setCredentials({...credentials, password: e.target.value})} 
                style={styles.input} 
                required 
              />
            </div>
            <button type="submit" style={styles.button}>
              {isRegistering ? <><UserPlus size={18}/> Register</> : <><LogIn size={18}/> Login</>}
            </button>
          </form>

          <p style={styles.toggleText}>
            {isRegistering ? "Already a developer?" : "New to the platform?"}
            <span 
              onClick={() => setIsRegistering(!isRegistering)} 
              style={styles.toggleLink}
            >
              {isRegistering ? ' Login here' : ' Create an account'}
            </span>
          </p>
        </div>
      </div>
    );
  }

  return (
    <div style={styles.dashboard}>
      {foundTrace && (
        <div style={styles.modalOverlay}>
            <div style={styles.modal}>
                <div style={{ display: 'flex', justifyContent: 'space-between', marginBottom: '20px' }}>
                    <h3 style={{ margin: 0, color: '#38bdf8' }}>Trace Result: {foundTrace.traceId}</h3>
                    <button onClick={() => setFoundTrace(null)} style={styles.closeBtn}><X size={20}/></button>
                </div>
                <div style={styles.traceGrid}>
                    <div style={styles.traceItem}><strong>Endpoint:</strong> {foundTrace.endpoint}</div>
                    <div style={styles.traceItem}><strong>Status:</strong> {foundTrace.statusCode}</div>
                    <div style={styles.traceItem}><strong>Latency:</strong> {foundTrace.responseTime}ms</div>
                    <div style={styles.traceItem}><strong>IP:</strong> {foundTrace.clientIp}</div>
                    <div style={styles.traceItem}><strong>Country:</strong> {foundTrace.country}</div>
                    <div style={styles.traceItem}><strong>Browser:</strong> {foundTrace.browser}</div>
                </div>
            </div>
        </div>
      )}

      <header style={styles.header}>
        <div style={{ display: 'flex', flexDirection: 'column' }}>
          <h1 style={{ display: 'flex', alignItems: 'center', gap: '12px', margin: 0 }}>
            <Activity size={32} color="#38bdf8" /> API Monitor
          </h1>
          {user && <span style={styles.userBadge}>Active: <strong>{user.email}</strong></span>}
        </div>
        <div style={{ display: 'flex', gap: '10px', alignItems: 'center' }}>
          <form onSubmit={handleTraceSearch} style={{ display: 'flex', gap: '5px' }}>
            <input 
                placeholder="Search Trace (TR-...)" 
                style={styles.traceInput} 
                value={traceSearchId}
                onChange={(e) => setTraceSearchId(e.target.value)}
            />
            <button type="submit" style={styles.traceBtn}><Fingerprint size={16}/></button>
          </form>
          <button onClick={loadData} style={styles.refreshBtn}><RefreshCw size={18} /></button>
          <button onClick={handleLogout} style={styles.logout}>Logout</button>
        </div>
      </header>

      <div style={styles.grid}>
        <StatCard icon={<Zap color="#fbbf24" />} label="Traffic" value={stats.totalRequests.toLocaleString()} />
        <StatCard icon={<Activity color="#38bdf8" />} label="Latency" value={`${stats.avgLatency.toFixed(1)}ms`} />
        <StatCard icon={<CheckCircle color="#10b981" />} label="Success" value={`${stats.successRate}%`} />
        <StatCard icon={<AlertCircle color="#ef4444" />} label="Errors" value={stats.totalErrors} />
      </div>

      <div style={styles.testCenterRow}>
        <div style={styles.testCenterCard}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px', marginBottom: '10px' }}>
                <Gauge size={20} color="#38bdf8" />
                <h3 style={{ margin: 0, fontSize: '0.9rem' }}>Developer Test Center</h3>
            </div>
            <div style={{ display: 'flex', gap: '12px' }}>
                <button onClick={() => runManualTest('/api/health')} style={styles.testBtnSuccess}>
                    <PlayCircle size={14} /> Simulate Success
                </button>
                <button onClick={() => runManualTest('/api/v1/trigger-error-test')} style={styles.testBtnError}>
                    <ShieldAlert size={14} /> Simulate Error
                </button>
            </div>
        </div>
      </div>

      <div style={styles.chartSection}>
        <div style={styles.chartCard}>
            <h3 style={styles.chartTitle}><Activity size={16} color="#10b981" /> Availability</h3>
            <ResponsiveContainer width="100%" height={180}>
                <BarChart data={stats.timelineData}>
                    <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                    <XAxis dataKey="time" stroke="#94a3b8" fontSize={10} />
                    <YAxis hide /><ReTooltip contentStyle={styles.tooltip} />
                    <Bar dataKey="success" fill="#10b981" radius={[4, 4, 0, 0]} />
                    <Bar dataKey="fail" fill="#ef4444" radius={[4, 4, 0, 0]} />
                </BarChart>
            </ResponsiveContainer>
        </div>

        <div style={styles.chartCard}>
            <h3 style={styles.chartTitle}><Globe size={16} color="#818cf8" /> Countries</h3>
            <div style={{ marginTop: '10px' }}>
              {Object.entries(stats.geoDistribution || {}).slice(0, 5).map(([c, v], idx) => (
                <div key={idx} style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.75rem', marginBottom: '5px' }}>
                  <span>{c}</span><span style={{ color: '#818cf8' }}>{((v/stats.totalRequests)*100 || 0).toFixed(1)}%</span>
                </div>
              ))}
            </div>
        </div>

        <div style={styles.chartCard}>
            <h3 style={styles.chartTitle}><Laptop size={16} color="#fbbf24" /> Browsers</h3>
            <ResponsiveContainer width="100%" height={150}>
                <PieChart>
                    <Pie data={browserData} innerRadius={40} outerRadius={55} dataKey="value">
                        {browserData.map((e, i) => <Cell key={i} fill={BROWSER_COLORS[i % 5]} />)}
                    </Pie>
                    <ReTooltip contentStyle={styles.tooltip} />
                </PieChart>
            </ResponsiveContainer>
        </div>

        <div style={styles.chartCard}>
            <h3 style={styles.chartTitle}><Trophy size={16} color="#fbbf24" /> Top Endpoints</h3>
            {Object.entries(stats.topEndpoints || {}).map(([e, c], i) => (
                <div key={i} style={{ marginBottom: '8px' }}>
                    <div style={{ display: 'flex', justifyContent: 'space-between', fontSize: '0.65rem' }}><span>{e}</span><span>{c}</span></div>
                    <div style={{ height: '3px', background: '#1e293b' }}><div style={{ width: `${(c/stats.totalRequests)*100}%`, height: '100%', background: '#38bdf8' }} /></div>
                </div>
            ))}
        </div>
      </div>

      <div style={styles.tableContainer}>
        <div style={{ padding: '10px', display: 'flex', gap: '10px' }}>
            <Search size={18} color="#64748b" />
            <input placeholder="Filter endpoints..." style={styles.searchInput} value={searchTerm} onChange={(e) => setSearchTerm(e.target.value)} />
        </div>
        <table style={styles.table}>
            <thead style={styles.tableHeadRow}>
                <tr><th style={styles.th}>Time</th><th style={styles.th}>Trace ID</th><th style={styles.th}>Endpoint</th><th style={styles.th}>Status</th><th style={styles.th}>Latency</th></tr>
            </thead>
            <tbody>
              {filteredLogs.map((log) => (
                <tr key={log.id} style={styles.tableRow}>
                  <td style={styles.td}>{new Date(log.timestamp).toLocaleTimeString()}</td>
                  <td style={styles.td}><code style={{ color: '#fbbf24' }}>{log.traceId}</code></td>
                  <td style={styles.td}><code style={{ color: '#38bdf8' }}>{log.endpoint}</code></td>
                  <td style={styles.td}><span style={{ color: log.statusCode >= 400 ? '#ef4444' : '#10b981' }}>{log.statusCode}</span></td>
                  <td style={styles.td}>{log.responseTime}ms</td>
                </tr>
              ))}
            </tbody>
        </table>
      </div>
    </div>
  );
}

function StatCard({ icon, label, value }) {
  return (
    <div style={styles.card}>
      <div style={{ display: 'flex', alignItems: 'center', gap: '8px', color: '#94a3b8', fontSize: '0.7rem', fontWeight: 'bold' }}>{icon} {label}</div>
      <div style={{ fontSize: '1.4rem', fontWeight: 'bold' }}>{value}</div>
    </div>
  );
}

const styles = {
  dashboard: { backgroundColor: '#020617', minHeight: '100vh', color: 'white', padding: '20px', fontFamily: 'Inter, sans-serif' },
  header: { display: 'flex', justifyContent: 'space-between', alignItems: 'flex-start', marginBottom: '20px' },
  userBadge: { fontSize: '0.75rem', color: '#38bdf8', marginTop: '4px' },
  grid: { display: 'grid', gridTemplateColumns: 'repeat(4, 1fr)', gap: '15px' },
  card: { backgroundColor: '#0f172a', padding: '15px', borderRadius: '12px', border: '1px solid #1e293b' },
  testCenterRow: { marginTop: '20px' },
  testCenterCard: { backgroundColor: '#0f172a', padding: '15px', borderRadius: '12px', border: '1px solid #38bdf844' },
  testBtnSuccess: { backgroundColor: '#10b98122', color: '#10b981', border: '1px solid #10b98144', padding: '8px 12px', borderRadius: '6px', cursor: 'pointer' },
  testBtnError: { backgroundColor: '#ef444422', color: '#ef4444', border: '1px solid #ef444444', padding: '8px 12px', borderRadius: '6px', cursor: 'pointer' },
  chartSection: { display: 'grid', gridTemplateColumns: 'repeat(2, 1fr)', gap: '15px', marginTop: '15px' },
  chartCard: { backgroundColor: '#0f172a', padding: '15px', borderRadius: '12px', border: '1px solid #1e293b' },
  chartTitle: { fontSize: '0.75rem', color: '#94a3b8', marginBottom: '10px', display: 'flex', alignItems: 'center', gap: '8px' },
  tooltip: { backgroundColor: '#0f172a', border: '1px solid #1e293b', borderRadius: '8px' },
  tableContainer: { marginTop: '20px', backgroundColor: '#0f172a', borderRadius: '12px', border: '1px solid #1e293b' },
  table: { width: '100%', borderCollapse: 'collapse' },
  tableHeadRow: { backgroundColor: '#020617', color: '#64748b', fontSize: '0.7rem' },
  th: { padding: '12px', textAlign: 'left' },
  tableRow: { borderBottom: '1px solid #1e293b' },
  td: { padding: '12px', fontSize: '0.75rem' },
  traceInput: { backgroundColor: '#020617', border: '1px solid #1e293b', color: 'white', padding: '6px 12px', borderRadius: '6px', fontSize: '0.75rem' },
  traceBtn: { backgroundColor: '#38bdf8', border: 'none', borderRadius: '6px', padding: '6px 10px', cursor: 'pointer' },
  modalOverlay: { position: 'fixed', top: 0, left: 0, right: 0, bottom: 0, backgroundColor: 'rgba(0,0,0,0.8)', display: 'flex', justifyContent: 'center', alignItems: 'center', zIndex: 1000 },
  modal: { backgroundColor: '#0f172a', padding: '30px', borderRadius: '16px', border: '1px solid #38bdf8', width: '500px' },
  traceGrid: { display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '15px' },
  traceItem: { fontSize: '0.85rem' },
  closeBtn: { background: 'none', border: 'none', color: '#64748b', cursor: 'pointer' },
  searchInput: { backgroundColor: 'transparent', border: 'none', color: 'white', outline: 'none', flex: 1 },
  container: { backgroundColor: '#020617', height: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center' },
  loginBox: { backgroundColor: '#0f172a', padding: '40px', borderRadius: '16px', width: '350px' },
  form: { display: 'flex', flexDirection: 'column', gap: '15px' },
  input: { padding: '10px', borderRadius: '8px', border: '1px solid #1e293b', backgroundColor: '#020617', color: 'white', width: '100%' },
  button: { padding: '10px', borderRadius: '8px', backgroundColor: '#38bdf8', color: '#020617', fontWeight: 'bold', cursor: 'pointer', border: 'none', display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' },
  refreshBtn: { backgroundColor: '#0f172a', color: '#38bdf8', border: '1px solid #1e293b', padding: '8px', borderRadius: '8px' },
  logout: { backgroundColor: '#ef4444', color: 'white', border: 'none', padding: '8px 16px', borderRadius: '8px', fontWeight: 'bold' },
  toggleText: { color: '#94a3b8', fontSize: '0.85rem', textAlign: 'center', marginTop: '15px' },
  toggleLink: { color: '#38bdf8', cursor: 'pointer', fontWeight: 'bold' }
};

export default App;