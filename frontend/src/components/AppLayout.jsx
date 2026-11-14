import { NavLink, Outlet, useNavigate } from "react-router-dom";
import { Button, Badge } from "react-bootstrap";
import { FaBoxes, FaCalendarAlt, FaChartBar, FaUsers, FaSignOutAlt, FaCube } from "react-icons/fa";
import { useAuth } from "../context/AuthContext";

const menu = [
  { to: "/equipment", label: "Ekipmanlar", icon: FaBoxes },
  { to: "/reservations", label: "Rezervasyonlar", icon: FaCalendarAlt },
  { to: "/reports", label: "Raporlar", icon: FaChartBar, adminOnly: true },
  { to: "/users", label: "Kullanıcılar", icon: FaUsers, adminOnly: true },
];

/** Giriş sonrası tüm sayfaların ortak iskeleti: üst bar + yan menü + içerik. */
function AppLayout() {
  const { user, isAdmin, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    if (window.confirm("Çıkış yapmak istediğinize emin misiniz?")) {
      logout();
      navigate("/login");
    }
  };

  return (
    <div className="app-shell">
      <header className="app-header">
        <div className="brand">
          <FaCube size={22} />
          <span>Envanter Yönetimi</span>
        </div>
        <div className="d-flex align-items-center gap-3">
          <span className="d-none d-sm-inline">
            {user.fullName}{" "}
            <Badge bg={isAdmin ? "primary" : "secondary"}>{isAdmin ? "Yönetici" : "Kullanıcı"}</Badge>
          </span>
          <Button variant="outline-light" size="sm" onClick={handleLogout}>
            <FaSignOutAlt className="me-1" /> Çıkış
          </Button>
        </div>
      </header>

      <div className="app-body">
        <nav className="app-sidebar">
          {menu
            .filter((item) => !item.adminOnly || isAdmin)
            .map(({ to, label, icon: Icon }) => (
              <NavLink key={to} to={to} className="sidebar-link">
                <Icon className="me-2" /> {label}
              </NavLink>
            ))}
        </nav>

        <main className="app-content">
          <Outlet />
        </main>
      </div>
    </div>
  );
}

export default AppLayout;
