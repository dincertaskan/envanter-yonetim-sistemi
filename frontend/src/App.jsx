import { Navigate, Route, Routes } from "react-router-dom";
import AppLayout from "./components/AppLayout";
import ProtectedRoute from "./components/ProtectedRoute";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import EquipmentPage from "./pages/EquipmentPage";
import ReservationsPage from "./pages/ReservationsPage";
import ReportsPage from "./pages/ReportsPage";
import UsersPage from "./pages/UsersPage";

function App() {
  return (
    <Routes>
      {/* Herkese açık */}
      <Route path="/login" element={<LoginPage />} />
      <Route path="/register" element={<RegisterPage />} />

      {/* Giriş gerektirir */}
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route path="/equipment" element={<EquipmentPage />} />
          <Route path="/reservations" element={<ReservationsPage />} />

          {/* Sadece yönetici */}
          <Route element={<ProtectedRoute adminOnly />}>
            <Route path="/reports" element={<ReportsPage />} />
            <Route path="/users" element={<UsersPage />} />
          </Route>
        </Route>
      </Route>

      <Route path="*" element={<Navigate to="/equipment" replace />} />
    </Routes>
  );
}

export default App;
