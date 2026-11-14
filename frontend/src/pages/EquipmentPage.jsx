import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Form, InputGroup, Spinner, Table } from "react-bootstrap";
import { FaCalendarPlus, FaEdit, FaPlus, FaSearch, FaTrash } from "react-icons/fa";
import client, { getErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import EquipmentFormModal from "../components/EquipmentFormModal";
import ReservationFormModal from "../components/ReservationFormModal";
import { formatDate } from "../utils/format";

// Kalibrasyon tarihi geçmişse uyarı rozeti göster
function CalibrationCell({ date }) {
  if (!date) return <span className="text-muted">-</span>;
  const overdue = new Date(`${date}T23:59:59`) < new Date();
  return (
    <>
      {formatDate(date)}{" "}
      {overdue && (
        <Badge bg="danger" title="Kalibrasyon tarihi geçmiş">
          Süresi geçti
        </Badge>
      )}
    </>
  );
}

function EquipmentPage() {
  const { isAdmin } = useAuth();
  const { showToast } = useToast();
  const [equipment, setEquipment] = useState([]);
  const [loading, setLoading] = useState(true);
  const [search, setSearch] = useState("");

  // Modallar: formModal = { equipment?: {...} } | null
  const [formModal, setFormModal] = useState(null);
  const [reserveFor, setReserveFor] = useState(null);

  useEffect(() => {
    client
      .get("/equipment")
      .then((res) => setEquipment(res.data))
      .catch((err) => showToast(getErrorMessage(err, "Ekipmanlar yüklenemedi"), "danger"))
      .finally(() => setLoading(false));
  }, [showToast]);

  const reserveInitialValues = useMemo(
    () => (reserveFor ? { equipmentId: String(reserveFor.id) } : undefined),
    [reserveFor]
  );

  const filtered = useMemo(() => {
    const q = search.trim().toLocaleLowerCase("tr");
    if (!q) return equipment;
    return equipment.filter((e) =>
      [e.name, e.brand, e.model, e.serialNumber].some((v) => v.toLocaleLowerCase("tr").includes(q))
    );
  }, [equipment, search]);

  const handleSaved = (saved) => {
    setEquipment((prev) => {
      const exists = prev.some((e) => e.id === saved.id);
      const next = exists ? prev.map((e) => (e.id === saved.id ? saved : e)) : [...prev, saved];
      return next.sort((a, b) => a.name.localeCompare(b.name, "tr"));
    });
    setFormModal(null);
  };

  const handleDelete = async (item) => {
    if (!window.confirm(`"${item.name}" silinsin mi? İleri tarihli rezervasyonları da iptal edilecek.`)) {
      return;
    }
    try {
      await client.delete(`/equipment/${item.id}`);
      setEquipment((prev) => prev.filter((e) => e.id !== item.id));
      showToast("Ekipman silindi");
    } catch (err) {
      showToast(getErrorMessage(err, "Silinemedi"), "danger");
    }
  };

  return (
    <>
      <div className="page-header">
        <h1 className="h3 mb-0">Ekipmanlar</h1>
        {isAdmin && (
          <Button onClick={() => setFormModal({})}>
            <FaPlus className="me-1" /> Yeni Ekipman
          </Button>
        )}
      </div>

      <InputGroup className="mb-3 search-box">
        <InputGroup.Text>
          <FaSearch />
        </InputGroup.Text>
        <Form.Control
          placeholder="Ad, marka, model veya seri no ile ara"
          value={search}
          onChange={(e) => setSearch(e.target.value)}
        />
      </InputGroup>

      {loading ? (
        <div className="text-center py-5">
          <Spinner animation="border" variant="primary" />
        </div>
      ) : (
        <Table striped bordered hover responsive className="bg-white">
          <thead>
            <tr>
              <th>Ekipman Adı</th>
              <th>Marka</th>
              <th>Model</th>
              <th>Seri No</th>
              <th>Kalibrasyon</th>
              <th>Açıklama</th>
              <th className="text-center">İşlemler</th>
            </tr>
          </thead>
          <tbody>
            {filtered.length === 0 && (
              <tr>
                <td colSpan={7} className="text-center text-muted py-4">
                  {search ? "Aramaya uygun ekipman bulunamadı" : "Henüz ekipman eklenmemiş"}
                </td>
              </tr>
            )}
            {filtered.map((item) => (
              <tr key={item.id}>
                <td>{item.name}</td>
                <td>{item.brand}</td>
                <td>{item.model}</td>
                <td>
                  <code>{item.serialNumber}</code>
                </td>
                <td>
                  <CalibrationCell date={item.calibrationDate} />
                </td>
                <td>{item.description || <span className="text-muted">-</span>}</td>
                <td className="text-center text-nowrap">
                  <Button size="sm" variant="outline-success" title="Rezerve et" onClick={() => setReserveFor(item)}>
                    <FaCalendarPlus />
                  </Button>
                  {isAdmin && (
                    <>
                      <Button
                        size="sm"
                        variant="outline-primary"
                        className="ms-1"
                        title="Düzenle"
                        onClick={() => setFormModal({ equipment: item })}
                      >
                        <FaEdit />
                      </Button>
                      <Button
                        size="sm"
                        variant="outline-danger"
                        className="ms-1"
                        title="Sil"
                        onClick={() => handleDelete(item)}
                      >
                        <FaTrash />
                      </Button>
                    </>
                  )}
                </td>
              </tr>
            ))}
          </tbody>
        </Table>
      )}

      <EquipmentFormModal
        show={formModal !== null}
        equipment={formModal?.equipment}
        onHide={() => setFormModal(null)}
        onSaved={handleSaved}
      />

      <ReservationFormModal
        show={reserveFor !== null}
        equipmentOptions={equipment}
        initialValues={reserveInitialValues}
        onHide={() => setReserveFor(null)}
        onSaved={() => setReserveFor(null)}
      />
    </>
  );
}

export default EquipmentPage;
