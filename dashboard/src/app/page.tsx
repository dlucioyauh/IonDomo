"use client";

import { useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";
import { 
  Smartphone, Clock, Shield, Activity, Lock, BookOpen, 
  Settings, Zap, TrendingUp, Ban, Timer, Sparkles, Search
} from "lucide-react";
import { getAppMeta, isSystemApp, type AppMeta } from "@/lib/appCatalog";

interface AppUsage {
  app_name: string;
  duration_seconds: number;
  is_blocked: boolean;
  daily_limit_seconds: number;
  requires_bible_unlock: boolean;
  is_system: boolean;
  icon: string;
  label: string;
  risk_level: string;
  risk_reasons: string;
}

const formatDuration = (seconds: number) => {
  if (seconds === 0) return "0s";
  const h = Math.floor(seconds / 3600);
  const m = Math.floor((seconds % 3600) / 60);
  const s = seconds % 60;
  const parts = [];
  if (h > 0) parts.push(`${h}h`);
  if (m > 0) parts.push(`${m}m`);
  if (s > 0 && h === 0) parts.push(`${s}s`);
  return parts.join(" ");
};

// Componente de ícone com fallback elegante
function AppIcon({ pkg, size = 48, realIcon }: { pkg: string; size?: number; realIcon?: string }) {
  const meta = getAppMeta(pkg);
  const [imgError, setImgError] = useState(false);
  if (realIcon && !imgError) {
    return (
      <img src={`data:image/png;base64,${realIcon}`} alt={meta.name}
        onError={() => setImgError(true)}
        className="rounded-xl object-cover shadow-lg"
        style={{ width: size, height: size }} />
    );
  }
  
  if (meta.iconUrl && !imgError) {
    return (
      <img
        src={meta.iconUrl}
        alt={meta.name}
        onError={() => setImgError(true)}
        className="rounded-xl object-cover shadow-lg"
        style={{ width: size, height: size }}
      />
    );
  }
  
  // Fallback: inicial colorida com gradiente
  return (
    <div
      className="rounded-xl flex items-center justify-center text-white font-bold shadow-lg"
      style={{
        width: size,
        height: size,
        backgroundColor: meta.color,
        fontSize: size * 0.45,
        boxShadow: `0 8px 24px ${meta.color}40`,
      }}
    >
      {meta.name.charAt(0)}
    </div>
  );
}

// Nome real: prioriza label do dispositivo (Android), depois catálogo, depois heurística
function resolveAppName(app: AppUsage): string {
  if (app.label && app.label.trim().length > 0) return app.label.trim();
  return getAppMeta(app.app_name).name;
}

export default function Dashboard() {
  const [data, setData] = useState<AppUsage[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [totalTime, setTotalTime] = useState(0);
  const [selectedApp, setSelectedApp] = useState<AppUsage | null>(null);
  const [search, setSearch] = useState("");

  const loadData = () => {
    fetch("http://192.168.3.109:8080/api/v1/dashboard")
      .then((res) => {
        if (!res.ok) throw new Error(`Backend retornou HTTP ${res.status}`);
        return res.json();
      })
      .then((result) => {
        if (result.status === "success") {
          const filtered = (result.data || []).filter((a: AppUsage) => !a.is_system && !isSystemApp(a.app_name));
          setData(filtered);
          setTotalTime(filtered.reduce((acc: number, curr: AppUsage) => acc + curr.duration_seconds, 0));
          setError(null);
        } else {
          setError(result.error || "Resposta inválida do backend");
        }
        setLoading(false);
      })
      .catch((err) => {
        console.error("Erro ao buscar dados:", err);
        setError(err.message || "Backend indisponível");
        setLoading(false);
      });
  };

  useEffect(() => { loadData(); }, []);

  const toggleBlock = async (app: AppUsage) => {
    await fetch("http://192.168.3.109:8080/api/v1/control/block", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        device_id: "416fd3c8a63a8b26",
        app_name: app.app_name,
        is_blocked: !app.is_blocked,
      }),
    });
    loadData();
  };

  const setLimit = async (app: AppUsage, hours: number) => {
    await fetch("http://192.168.3.109:8080/api/v1/control/limit", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        device_id: "416fd3c8a63a8b26",
        app_name: app.app_name,
        daily_limit_seconds: hours * 3600,
      }),
    });
    loadData();
  };

  const filteredData = data.filter((a) => {
    const meta = getAppMeta(a.app_name);
    return meta.name.toLowerCase().includes(search.toLowerCase());
  });

  if (loading) {
    return (
      <div className="min-h-screen bg-slate-950 flex items-center justify-center">
        <div className="relative">
          <div className="w-20 h-20 border-4 border-violet-500 border-t-transparent rounded-full animate-spin"></div>
          <Shield className="w-8 h-8 text-violet-400 absolute top-1/2 left-1/2 -translate-x-1/2 -translate-y-1/2" />
        </div>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gradient-to-br from-slate-950 via-violet-950/20 to-slate-950 text-slate-100 p-6 md:p-10">
      <div className="max-w-7xl mx-auto space-y-8">
        
        {/* Header 3D */}
        <div className="relative overflow-hidden rounded-3xl bg-gradient-to-r from-violet-600/20 via-blue-600/20 to-cyan-600/20 border border-violet-500/30 p-8 shadow-2xl backdrop-blur-xl">
          <div className="absolute -top-24 -right-24 w-64 h-64 bg-violet-500/20 rounded-full blur-3xl"></div>
          <div className="absolute -bottom-24 -left-24 w-64 h-64 bg-cyan-500/20 rounded-full blur-3xl"></div>
          
          <div className="relative flex flex-col md:flex-row md:items-center justify-between gap-4">
            <div className="flex items-center gap-5">
              <div className="relative">
                <div className="absolute inset-0 bg-violet-500 blur-xl opacity-50"></div>
                <div className="relative p-4 bg-gradient-to-br from-violet-500 to-blue-600 rounded-2xl shadow-2xl">
                  <Shield className="w-10 h-10 text-white" />
                </div>
              </div>
              <div>
                <h1 className="text-4xl font-black bg-gradient-to-r from-violet-300 via-blue-300 to-cyan-300 bg-clip-text text-transparent tracking-tight">
                  IonDomo
                </h1>
                <p className="text-slate-400 text-sm mt-1 font-medium">Central de Controle Parental Inteligente</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <div className="px-4 py-2 bg-emerald-500/10 border border-emerald-500/30 rounded-full flex items-center gap-2 shadow-lg">
                <div className="w-2 h-2 bg-emerald-400 rounded-full animate-pulse"></div>
                <span className="text-emerald-400 text-sm font-semibold">Online</span>
              </div>
              <div className="px-4 py-2 bg-violet-500/10 border border-violet-500/30 rounded-full flex items-center gap-2 shadow-lg">
                <Sparkles className="w-4 h-4 text-violet-400" />
                <span className="text-violet-300 text-sm font-semibold">LGPD Compliant</span>
              </div>
            </div>
          </div>
        </div>

        {error && (
          <div className="bg-red-500/10 border border-red-500/30 text-red-300 px-5 py-4 rounded-2xl flex items-center gap-3">
            <Ban className="w-5 h-5" />
            <span className="text-sm font-medium">Falha ao carregar dados: {error}. Verifique se o backend está rodando (porta 8080).</span>
          </div>
        )}

        {/* Stats 3D Cards */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          <StatCard 
            icon={<Smartphone className="w-6 h-6" />} 
            label="Dispositivo" 
            value="Poco M5" 
            sub="ID: 416fd3c8...a8b26"
            gradient="from-blue-600 to-cyan-600"
          />
          <StatCard 
            icon={<Activity className="w-6 h-6" />} 
            label="Apps Relevantes" 
            value={String(data.length)} 
            sub="Filtrados (sem apps de sistema)"
            gradient="from-violet-600 to-purple-600"
          />
          <StatCard 
            icon={<Clock className="w-6 h-6" />} 
            label="Tempo Total de Tela" 
            value={formatDuration(totalTime)} 
            sub="Acumulado nas últimas 24h"
            gradient="from-cyan-600 to-emerald-600"
          />
        </div>

        {/* Barra de Busca */}
        <div className="relative">
          <Search className="absolute left-4 top-1/2 -translate-y-1/2 w-5 h-5 text-slate-500" />
          <input
            type="text"
            placeholder="Buscar aplicativo..."
            value={search}
            onChange={(e) => setSearch(e.target.value)}
            className="w-full bg-slate-900/60 border border-slate-800 rounded-2xl py-4 pl-12 pr-4 text-white placeholder-slate-500 focus:outline-none focus:border-violet-500 focus:ring-2 focus:ring-violet-500/20 transition-all"
          />
        </div>

        {/* Gráfico Principal */}
        <div className="bg-slate-900/60 backdrop-blur-xl p-8 rounded-3xl border border-slate-800 shadow-2xl">
          <div className="flex items-center gap-3 mb-6">
            <TrendingUp className="w-6 h-6 text-violet-400" />
            <h2 className="text-2xl font-bold text-white">Análise de Uso por Aplicativo</h2>
          </div>
          {filteredData.length === 0 ? (
            <div className="h-64 flex items-center justify-center text-slate-500">
              Nenhum app encontrado com esse nome.
            </div>
          ) : (
            <ResponsiveContainer width="100%" height={320}>
              <BarChart data={filteredData} margin={{ top: 20, right: 30, left: 0, bottom: 20 }}>
                <CartesianGrid strokeDasharray="3 3" stroke="#1e293b" vertical={false} />
                <XAxis 
                  dataKey="app_name" 
                  tickFormatter={(v) => {
                    const found = filteredData.find(d => d.app_name === v);
                    return found ? resolveAppName(found) : getAppMeta(v).name;
                  }}
                  angle={-30} 
                  textAnchor="end" 
                  height={70} 
                  tick={{ fill: "#94a3b8", fontSize: 12 }} 
                  axisLine={{ stroke: "#334155" }}
                />
                <YAxis 
                  tickFormatter={(v: number) => formatDuration(v)} 
                  tick={{ fill: "#94a3b8", fontSize: 12 }} 
                  axisLine={false}
                  tickLine={false}
                />
                <Tooltip 
                  cursor={{ fill: "rgba(139, 92, 246, 0.1)" }}
                  contentStyle={{ 
                    backgroundColor: "#0f172a", 
                    borderColor: "#334155", 
                    borderRadius: "16px",
                    color: "#f8fafc",
                    boxShadow: "0 20px 40px rgba(0,0,0,0.5)"
                  }}
                  formatter={(v: number) => [formatDuration(v), "Tempo de Uso"]}
                  labelFormatter={(l) => {
                    const found = filteredData.find(d => d.app_name === String(l));
                    return found ? resolveAppName(found) : getAppMeta(String(l)).name;
                  }}
                />
                <Bar dataKey="duration_seconds" radius={[8, 8, 0, 0]} barSize={45}>
                  {filteredData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={getAppMeta(entry.app_name).color} />
                  ))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>

        {/* Grid de Apps com Ícones Reais */}
        <div>
          <div className="flex items-center gap-3 mb-6">
            <Settings className="w-6 h-6 text-cyan-400" />
            <h2 className="text-2xl font-bold text-white">Controles Parentais</h2>
            <span className="text-slate-500 text-sm ml-2">Clique em um app para configurar</span>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-5">
            {filteredData.map((app, index) => (
              <AppCard 
                key={index}
                app={app}
                onClick={() => setSelectedApp(app)}
              />
            ))}
          </div>
        </div>

        {selectedApp && (
          <ConfigModal 
            app={selectedApp} 
            onClose={() => setSelectedApp(null)}
            onToggleBlock={toggleBlock}
            onSetLimit={setLimit}
          />
        )}
      </div>
    </div>
  );
}

function StatCard({ icon, label, value, sub, gradient }: any) {
  return (
    <div className="group relative overflow-hidden rounded-3xl bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-6 shadow-2xl hover:shadow-violet-500/20 transition-all duration-500 hover:-translate-y-1">
      <div className={`absolute inset-0 bg-gradient-to-br ${gradient} opacity-0 group-hover:opacity-10 transition-opacity duration-500`}></div>
      <div className="relative flex items-start justify-between">
        <div className={`p-3 rounded-2xl bg-gradient-to-br ${gradient} shadow-lg`}>
          <div className="text-white">{icon}</div>
        </div>
      </div>
      <div className="relative mt-4">
        <p className="text-sm font-medium text-slate-400">{label}</p>
        <p className="text-3xl font-black text-white mt-1 tracking-tight">{value}</p>
        <p className="text-xs text-slate-500 mt-1">{sub}</p>
      </div>
    </div>
  );
}

function AppCard({ app, onClick }: { app: AppUsage; onClick: () => void }) {
  const meta = getAppMeta(app.app_name);
  return (
    <div 
      onClick={onClick}
      className="group cursor-pointer relative overflow-hidden rounded-2xl bg-slate-900/60 backdrop-blur-xl border border-slate-800 p-5 shadow-xl hover:shadow-2xl hover:-translate-y-1 hover:border-violet-500/50 transition-all duration-300"
    >
      {/* BADGE ÚNICO DE RISCO */}
      {app.risk_level !== "safe" && (
        <div 
          title={app.risk_reasons}
          className="absolute top-3 right-3 z-20 flex items-center gap-1 px-2 py-1 rounded-lg text-xs font-bold border shadow-md"
          style={{
            backgroundColor: app.risk_level === "high" ? "rgba(239,68,68,0.15)" : "rgba(245,158,11,0.15)",
            borderColor: app.risk_level === "high" ? "rgba(239,68,68,0.4)" : "rgba(245,158,11,0.4)",
            color: app.risk_level === "high" ? "#F87171" : "#FBBF24",
          }}
        >
          <Shield className="w-3 h-3" />
          {app.risk_level === "high" ? "Risco" : "Atenção"}
        </div>
      )}
      <div className="relative flex items-center justify-between mb-3">
        {/* Badge Atenção/Risco fica no topo direito */}
        

        <div className="flex items-center gap-3">
          <AppIcon pkg={app.app_name} size={48} realIcon={app.icon} />
          
          <div>
            <p className="text-white font-bold">{resolveAppName(app)}</p>
            <p className="text-xs text-slate-500 font-mono truncate max-w-[150px]">{app.app_name}</p>
          </div>
        </div>
        {app.is_blocked && (
          <div className="p-2 bg-red-500/20 border border-red-500/40 rounded-lg">
            <Lock className="w-4 h-4 text-red-400" />
          </div>
        )}
      </div>
      
      <div className="flex items-center justify-between mt-4 pt-4 border-t border-slate-800">
        <div>
          <p className="text-2xl font-black text-white">{formatDuration(app.duration_seconds)}</p>
          <p className="text-xs text-slate-500">tempo hoje</p>
        </div>
        <div className="text-right">
          {app.daily_limit_seconds > 0 ? (
            <div className="flex items-center gap-1 text-cyan-400">
              <Timer className="w-4 h-4" />
              <span className="text-xs font-semibold">{formatDuration(app.daily_limit_seconds)}</span>
            </div>
          ) : (
            <span className="text-xs text-slate-600">sem limite</span>
          )}
          {app.requires_bible_unlock && (
            <div className="flex items-center gap-1 text-violet-400 mt-1">
              <BookOpen className="w-3 h-3" />
              <span className="text-xs font-semibold">Bíblia Unlock</span>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}

function ConfigModal({ app, onClose, onToggleBlock, onSetLimit }: any) {
  const meta = getAppMeta(app.app_name);
  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-black/80 backdrop-blur-md" onClick={onClose}>
      <div 
        className="relative w-full max-w-md bg-gradient-to-br from-slate-900 to-violet-950/30 border border-violet-500/30 rounded-3xl p-8 shadow-2xl"
        onClick={(e) => e.stopPropagation()}
      >
        <div className="flex items-center justify-between mb-6">
          <div className="flex items-center gap-3">
            <AppIcon pkg={app.app_name} size={56} realIcon={app.icon} />
            <div>
              <h3 className="text-xl font-bold text-white">{resolveAppName(app)}</h3>
              <p className="text-xs text-slate-400 font-mono">{app.app_name}</p>
            </div>
          </div>
          <button onClick={onClose} className="text-slate-400 hover:text-white transition-colors">
            <Ban className="w-6 h-6" />
          </button>
        </div>

        <div className="space-y-4">
          <div className="p-4 bg-slate-800/50 rounded-2xl border border-slate-700">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <Lock className="w-5 h-5 text-red-400" />
                <span className="text-white font-semibold">Bloquear App</span>
              </div>
              <button
                onClick={() => onToggleBlock(app)}
                className={`relative w-14 h-8 rounded-full transition-all duration-300 ${
                  app.is_blocked ? "bg-red-500 shadow-lg shadow-red-500/50" : "bg-slate-600"
                }`}
              >
                <div className={`absolute top-1 w-6 h-6 bg-white rounded-full shadow-lg transition-transform duration-300 ${
                  app.is_blocked ? "translate-x-7" : "translate-x-1"
                }`}></div>
              </button>
            </div>
          </div>

          <div className="p-4 bg-slate-800/50 rounded-2xl border border-slate-700">
            <div className="flex items-center gap-3 mb-3">
              <Timer className="w-5 h-5 text-cyan-400" />
              <span className="text-white font-semibold">Limite Diário</span>
            </div>
            <div className="grid grid-cols-4 gap-2">
              {[0, 1, 2, 4].map((h) => (
                <button
                  key={h}
                  onClick={() => onSetLimit(app, h)}
                  className={`py-3 rounded-xl font-bold text-sm transition-all duration-300 ${
                    app.daily_limit_seconds === h * 3600
                      ? "bg-gradient-to-br from-cyan-500 to-blue-600 text-white shadow-lg shadow-cyan-500/50 scale-105"
                      : "bg-slate-700 text-slate-300 hover:bg-slate-600"
                  }`}
                >
                  {h === 0 ? "∞" : `${h}h`}
                </button>
              ))}
            </div>
          </div>

          <div className="p-4 bg-violet-900/30 rounded-2xl border border-violet-500/30">
            <div className="flex items-center justify-between">
              <div className="flex items-center gap-3">
                <BookOpen className="w-5 h-5 text-violet-400" />
                <div>
                  <span className="text-white font-semibold">Bíblia Unlock</span>
                  <p className="text-xs text-slate-400">Filho precisa ler capítulo + responder perguntas</p>
                </div>
              </div>
              <span className="px-3 py-1 bg-violet-500/20 text-violet-300 text-xs font-bold rounded-full border border-violet-500/40">
                FASE 2
              </span>
            </div>
          </div>
        </div>

        <div className="mt-6 pt-4 border-t border-slate-800 text-center">
          <p className="text-xs text-slate-500">
            <Zap className="w-3 h-3 inline mr-1 text-yellow-400" />
            Alterações aplicadas em tempo real ao dispositivo
          </p>
        </div>
      </div>
    </div>
  );
}
