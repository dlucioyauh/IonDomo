"use client";

import { useEffect, useState } from "react";
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer, Cell } from "recharts";
import { Smartphone, Clock, Shield } from "lucide-react";

interface AppUsage {
  app_name: string;
  duration_seconds: number;
}

export default function Dashboard() {
  const [data, setData] = useState<AppUsage[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    fetch("http://192.168.3.109:8080/api/v1/dashboard")
      .then((res) => res.json())
      .then((result) => {
        if (result.status === "success") setData(result.data);
        setLoading(false);
      })
      .catch((err) => { console.error("Erro:", err); setLoading(false); });
  }, []);

  const formatDuration = (seconds: number) => {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return h > 0 ? `${h}h ${m}m` : `${m} min`;
  };

  const COLORS = ["#0088FE", "#00C49F", "#FFBB28", "#FF8042", "#8884D8"];

  return (
    <div className="min-h-screen bg-gray-50 p-8">
      <div className="max-w-4xl mx-auto">
        <div className="flex items-center gap-3 mb-8">
          <Shield className="w-8 h-8 text-blue-600" />
          <div>
            <h1 className="text-3xl font-bold text-gray-900">IonDomo Dashboard</h1>
            <p className="text-gray-500">Monitoramento de bem-estar digital em tempo real</p>
          </div>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-3 gap-6 mb-8">
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-2">
              <Smartphone className="w-5 h-5 text-blue-500" />
              <span className="text-sm font-medium text-gray-500">Dispositivo</span>
            </div>
            <p className="text-2xl font-bold text-gray-900">Poco M5</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-2">
              <Clock className="w-5 h-5 text-green-500" />
              <span className="text-sm font-medium text-gray-500">Apps Monitorados</span>
            </div>
            <p className="text-2xl font-bold text-gray-900">{data.length}</p>
          </div>
          <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
            <div className="flex items-center gap-3 mb-2">
              <Shield className="w-5 h-5 text-purple-500" />
              <span className="text-sm font-medium text-gray-500">Status</span>
            </div>
            <p className="text-2xl font-bold text-green-600">Protegido</p>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-100">
          <h2 className="text-xl font-semibold text-gray-900 mb-6">Tempo de Uso por Aplicativo (Hoje)</h2>
          {loading ? (
            <div className="h-80 flex items-center justify-center text-gray-400">Carregando...</div>
          ) : data.length === 0 ? (
            <div className="h-80 flex items-center justify-center text-gray-400">Nenhum dado hoje.</div>
          ) : (
            <ResponsiveContainer width="100%" height={400}>
              <BarChart data={data} margin={{ top: 20, right: 30, left: 20, bottom: 60 }}>
                <CartesianGrid strokeDasharray="3 3" vertical={false} />
                <XAxis dataKey="app_name" angle={-45} textAnchor="end" height={80} tick={{ fontSize: 12 }} />
                <YAxis tickFormatter={(value: number) => formatDuration(value)} tick={{ fontSize: 12 }} />
                <Tooltip formatter={(value: number) => formatDuration(value)} contentStyle={{ borderRadius: "8px", border: "none", boxShadow: "0 4px 6px -1px rgb(0 0 0 / 0.1)" }} />
                <Bar dataKey="duration_seconds" radius={[4, 4, 0, 0]}>
                  {data.map((entry, index) => (<Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />))}
                </Bar>
              </BarChart>
            </ResponsiveContainer>
          )}
        </div>
      </div>
    </div>
  );
}
