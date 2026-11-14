import axios from "axios";

export const TOKEN_KEY = "inventory_token";

// Geliştirmede Vite proxy'si /api isteklerini backend'e iletir.
// Farklı bir adres için .env dosyasına VITE_API_URL yazılabilir.
const client = axios.create({
  baseURL: import.meta.env.VITE_API_URL || "/api",
});

// Her isteğe token'ı ekle
client.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Token süresi dolmuşsa (401) oturumu kapat
client.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY)) {
      localStorage.removeItem(TOKEN_KEY);
      window.dispatchEvent(new Event("auth:expired"));
    }
    return Promise.reject(error);
  }
);

/** Backend'in döndürdüğü hata mesajını (veya genel bir mesajı) verir. */
export function getErrorMessage(error, fallback = "Bir hata oluştu") {
  if (!error.response) {
    return "Sunucuya ulaşılamıyor. Backend çalışıyor mu?";
  }
  return error.response.data?.message || fallback;
}

export default client;
