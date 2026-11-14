const dateTimeFormatter = new Intl.DateTimeFormat("tr-TR", {
  dateStyle: "short",
  timeStyle: "short",
});

const dateFormatter = new Intl.DateTimeFormat("tr-TR", { dateStyle: "short" });

export function formatDateTime(value) {
  return value ? dateTimeFormatter.format(new Date(value)) : "-";
}

export function formatDate(value) {
  // "2026-05-01" gibi tarihleri saat dilimi kaymasından korumak için yerel tarih olarak oku
  return value ? dateFormatter.format(new Date(`${value}T00:00:00`)) : "-";
}

/** Date nesnesini <input type="datetime-local"> formatına çevirir: "2026-05-01T14:30" */
export function toDateTimeLocal(date) {
  const pad = (n) => String(n).padStart(2, "0");
  return (
    `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}` +
    `T${pad(date.getHours())}:${pad(date.getMinutes())}`
  );
}

// Ekipman alanlarının ekranda gösterilecek Türkçe adları
export const FIELD_LABELS = {
  name: "Ekipman Adı",
  brand: "Marka",
  model: "Model",
  serialNumber: "Seri No",
  calibrationDate: "Kalibrasyon Tarihi",
  description: "Açıklama",
};
