import { useEffect, useState } from "react";
import { Badge, Form, Table } from "react-bootstrap";
import client, { getErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";

function UsersPage() {
  const { user: currentUser } = useAuth();
  const { showToast } = useToast();
  const [users, setUsers] = useState([]);

  useEffect(() => {
    client
      .get("/users")
      .then((res) => setUsers(res.data))
      .catch((err) => showToast(getErrorMessage(err, "Kullanıcılar yüklenemedi"), "danger"));
  }, [showToast]);

  const changeRole = async (target, role) => {
    const label = role === "ADMIN" ? "yönetici" : "kullanıcı";
    if (!window.confirm(`${target.fullName} adlı kişinin rolü "${label}" olarak değiştirilsin mi?`)) {
      return;
    }
    try {
      const res = await client.patch(`/users/${target.id}/role`, { role });
      setUsers((prev) => prev.map((u) => (u.id === target.id ? res.data : u)));
      showToast("Rol güncellendi");
    } catch (err) {
      showToast(getErrorMessage(err, "Rol değiştirilemedi"), "danger");
    }
  };

  return (
    <>
      <div className="page-header">
        <h1 className="h3 mb-0">Kullanıcılar</h1>
      </div>

      <Table striped bordered hover responsive className="bg-white">
        <thead>
          <tr>
            <th>Ad Soyad</th>
            <th>Email</th>
            <th>Telefon</th>
            <th style={{ width: 180 }}>Rol</th>
          </tr>
        </thead>
        <tbody>
          {users.map((u) => (
            <tr key={u.id}>
              <td>
                {u.fullName}{" "}
                {u.id === currentUser.id && (
                  <Badge bg="info" className="ms-1">
                    Siz
                  </Badge>
                )}
              </td>
              <td>{u.email}</td>
              <td>{u.phone}</td>
              <td>
                <Form.Select
                  size="sm"
                  value={u.role}
                  disabled={u.id === currentUser.id}
                  onChange={(e) => changeRole(u, e.target.value)}
                >
                  <option value="USER">Kullanıcı</option>
                  <option value="ADMIN">Yönetici</option>
                </Form.Select>
              </td>
            </tr>
          ))}
        </tbody>
      </Table>
    </>
  );
}

export default UsersPage;
