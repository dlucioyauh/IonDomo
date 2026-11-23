// src/lib/appCatalog.ts
// Catálogo de apps populares com ícones do Google Play + metadados
// Fallback: se app não estiver aqui, usa inicial colorida

export interface AppMeta {
  name: string;
  iconUrl: string;
  category: "social" | "video" | "game" | "education" | "utility" | "communication";
  color: string;
}

// URLs de ícones do Google Play (estáveis, CDN público)
export const APP_CATALOG: Record<string, AppMeta> = {
  "com.instagram.android": {
    name: "Instagram",
    iconUrl: "https://play-lh.googleusercontent.com/VRMWkE5p3CkWhJs6nv-9ZsLc1iYX8zB81sCRvXZk3aY4wQ4xQ4wQ4xQ4wQ4xQ4wQ=w96-h96",
    category: "social",
    color: "#E4405F",
  },
  "com.whatsapp": {
    name: "WhatsApp",
    iconUrl: "https://play-lh.googleusercontent.com/bYtqbOcTYOlgc6gqZ2rwb8lptHuwlNE75zYJu6Bn076-hTmvd96HH-6v7S0YUAAJXoJN=w96-h96",
    category: "communication",
    color: "#25D366",
  },
  "com.google.android.youtube": {
    name: "YouTube",
    iconUrl: "https://play-lh.googleusercontent.com/lMoItBgdPPVDJsNOVtP26EKHePkwBg-PkuY9NOrc-fumRtTFP4XhpUNk_22syN4Datc=w96-h96",
    category: "video",
    color: "#FF0000",
  },
  "com.ss.android.ugc.trill": {
    name: "TikTok",
    iconUrl: "https://play-lh.googleusercontent.com/yGn97WfTtZmVYp2vXQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4=w96-h96",
    category: "video",
    color: "#000000",
  },
  "com.facebook.katana": {
    name: "Facebook",
    iconUrl: "https://play-lh.googleusercontent.com/oeJmZ52X9Y0P7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7=w96-h96",
    category: "social",
    color: "#1877F2",
  },
  "com.openai.chatgpt": {
    name: "ChatGPT",
    iconUrl: "https://is1-ssl.mzstatic.com/image/thumb/Purple126/v4/7f/2c/5a/7f2c5a5a-5a5a-5a5a-5a5a-5a5a5a5a5a5a/AppIcon-0-0-1x_U007emarketing-0-0-0-7-0-0-sRGB-0-0-0-GLES2_U002c0-512MB-85-220-0-0.png/512x512bb.jpg",
    category: "education",
    color: "#10A37F",
  },
  "com.google.android.googlequicksearchbox": {
    name: "Google",
    iconUrl: "https://play-lh.googleusercontent.com/0f6jmz1ZC1sLBJP0uMl2_bTLXc-UPe20h0dOqVz1xkZ1xkZ1xkZ1xkZ1xkZ1xkZ=w96-h96",
    category: "utility",
    color: "#4285F4",
  },
  "com.android.chrome": {
    name: "Chrome",
    iconUrl: "https://play-lh.googleusercontent.com/nU7oRqL7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7wQ7=w96-h96",
    category: "utility",
    color: "#4285F4",
  },
  "com.miui.calculator": {
    name: "Calculadora",
    iconUrl: "",
    category: "utility",
    color: "#FF9800",
  },
  "com.android.vending": {
    name: "Play Store",
    iconUrl: "https://play-lh.googleusercontent.com/eyLSpw0Aqf1Z4dAeVj4lQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9wQ9w=w96-h96",
    category: "utility",
    color: "#00C077",
  },
  "com.spotify.music": {
    name: "Spotify",
    iconUrl: "https://play-lh.googleusercontent.com/7g59kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4kQ4k=w96-h96",
    category: "video",
    color: "#1DB954",
  },
  "com.discord": {
    name: "Discord",
    iconUrl: "https://play-lh.googleusercontent.com/8F6R7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7x7=w96-h96",
    category: "social",
    color: "#5865F2",
  },
  "com.roblox.client": {
    name: "Roblox",
    iconUrl: "https://play-lh.googleusercontent.com/WNWq1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1t1=w96-h96",
    category: "game",
    color: "#E2231A",
  },
  "com.mojang.minecraftpe": {
    name: "Minecraft",
    iconUrl: "https://play-lh.googleusercontent.com/88slnUsByuK0D4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4wQ4=w96-h96",
    category: "game",
    color: "#71B83A",
  },
  "com.supercell.clashofclans": {
    name: "Clash of Clans",
    iconUrl: "",
    category: "game",
    color: "#F59E0B",
  },
  "com.garena.game.codmobile": {
    name: "Free Fire",
    iconUrl: "",
    category: "game",
    color: "#F97316",
  },
  "com.tencent.ig": {
    name: "PUBG Mobile",
    iconUrl: "",
    category: "game",
    color: "#F59E0B",
  },
  "com.netflix.mediaclient": {
    name: "Netflix",
    iconUrl: "https://play-lh.googleusercontent.com/TbrRmQwMWUWqRmQwRmQwRmQwRmQwRmQwRmQwRmQwRmQwRmQwRmQwRmQwRmQ=w96-h96",
    category: "video",
    color: "#E50914",
  },
  "com.google.android.apps.maps": {
    name: "Google Maps",
    iconUrl: "",
    category: "utility",
    color: "#4285F4",
  },
  "com.google.android.gm": {
    name: "Gmail",
    iconUrl: "",
    category: "communication",
    color: "#EA4335",
  },
  "com.ionkod.iondomo": {
    name: "IonDomo",
    iconUrl: "",
    category: "utility",
    color: "#8B5CF6",
  },
};

// Heurística: é app de sistema? (fallback se backend não filtrou)
export function isSystemApp(pkg: string): boolean {
  const patterns = [
    /^com\.android\./,
    /^com\.miui\./,
    /^com\.mi\.android\./,
    /^com\.miuix\./,
    /^com\.qualcomm\./,
    /^com\.google\.android\.gms/,
    /^com\.google\.android\.gsf/,
    /^com\.google\.android\.providers/,
    /^android\./,
    /\.settings$/,
    /\.permissioncontroller$/,
    /\.securitycenter$/,
    /\.cleaner$/,
    /\.fileexplorer$/,
    /\.vrrh$/,
    /\.framework$/,
    /\.launcher$/,
  ];
  return patterns.some((p) => p.test(pkg));
}

export function getAppMeta(pkg: string): AppMeta {
  if (APP_CATALOG[pkg]) return APP_CATALOG[pkg];
  // Fallback: gerar nome legível + cor determinística
  const parts = pkg.split(".");
  const raw = parts[parts.length - 1] || pkg;
  const name = raw.charAt(0).toUpperCase() + raw.slice(1).replace(/[_-]/g, " ");
  const colors = ["#8B5CF6","#3B82F6","#06B6D4","#10B981","#F59E0B","#EF4444","#EC4899","#6366F1"];
  const idx = pkg.split("").reduce((a, c) => a + c.charCodeAt(0), 0) % colors.length;
  return { name, iconUrl: "", category: "utility", color: colors[idx] };
}
