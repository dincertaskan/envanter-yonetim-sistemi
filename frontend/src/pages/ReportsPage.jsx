import { useEffect, useMemo, useState } from "react";
import { Badge, Button, Card, Col, Form, Nav, Row, Tab, Table } from "react-bootstrap";
import client, { getErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";
import { FIELD_LABELS, formatDateTime } from "../utils/format";

const ACTIONS = {
  CREATE: { label: "Eklenen", variant: "success" },
  UPDATE: { label: "Güncellenen", variant: "primary" },
  DELETE: { label: "Silinen", variant: "danger" },
};

const emptyFilters = { user: "", equipment: "", date: "" };

function ReportsPage() {
  const { showToast } = useToast();
  const [logs, setLogs] = useState([]);
  const [reservations, setReservations] = useState([]);
  const [filters, setFilters] = useState(emptyFilters);

  useEffect(() => {
    Promise.all([client.get("/audit-logs"), client.get("/reservations")])
      .then(([logRes, resRes]) => {
        setLogs(logRes.data);
        setReservations(resRes.data);
      })
      .catch((err) => showToast(getErrorMessage(err, "Raporlar yüklenemedi"), "danger"));
  }, [showToast]);

  const logsByAction = useMemo(() => {
    const groups = { CREATE: [], UPDATE: [], DELETE: [] };
    logs.forEach((log) => groups[log.action]?.push(log));
    return groups;
  }, [logs]);

  // Filtreler yazıldıkça liste anında güncellenir
  const filteredReservations = useMemo(() => {
    const contains = (value, q) => value.toLocaleLowerCase("tr").includes(q.trim().toLocaleLowerCase("tr"));
    return reservations
      .filter((r) => contains(r.userFullName, filters.user))
      .filter((r) => contains(`${r.equipmentName} ${r.serialNumber}`, filters.equipment))
      .filter((r) => !filters.date || r.startTime.startsWith(filters.date))
      .sort((a, b) => b.startTime.localeCompare(a.startTime));
  }, [reservations, filters]);

  const setFilter = (e) => setFilters({ ...filters, [e.target.name]: e.target.value });

  return (
    <>
      <div className="page-header">
        <h1 className="h3 mb-0">Raporlar</h1>
      </div>

      {/* Özet kartları */}
      <Row className="g-3 mb-4">
        {Object.entries(ACTIONS).map(([action, { label, variant }]) => (
          <Col sm={6} lg={3} key={action}>
            <Card className={`stat-card border-${variant}`}>
              <Card.Body>
                <div className="text-muted small">{label} Ekipman</div>
                <div className="fs-3 fw-semibold">{logsByAction[action].length}</div>
              </Card.Body>
            </Card>
          </Col>
        ))}
        <Col sm={6} lg={3}>
          <Card className="stat-card border-warning">
            <Card.Body>
              <div className="text-muted small">Toplam Rezervasyon</div>
              <div className="fs-3 fw-semibold">{reservations.length}</div>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <Tab.Container defaultActiveKey="CREATE">
        <Nav variant="tabs" className="mb-3">
          {Object.entries(ACTIONS).map(([action, { label }]) => (
            <Nav.Item key={action}>
              <Nav.Link eventKey={action}>{label}</Nav.Link>
            </Nav.Item>
          ))}
          <Nav.Item>
            <Nav.Link eventKey="RESERVATIONS">Rezervasyonlar</Nav.Link>
          </Nav.Item>
        </Nav>

        <Tab.Content>
          {/* Ekleme ve silme kayıtları */}
          {["CREATE", "DELETE"].map((action) => (
            <Tab.Pane eventKey={action} key={action}>
              <Table striped bordered hover responsive className="bg-white">
                <thead>
                  <tr>
                    <th>Tarih</th>
                    <th>Ekipman</th>
                    <th>İşlemi Yapan</th>
                  </tr>
                </thead>
                <tbody>
                  {logsByAction[action].length === 0 && <EmptyRow colSpan={3} />}
                  {logsByAction[action].map((log) => (
                    <tr key={log.id}>
                      <td className="text-nowrap">{formatDateTime(log.createdAt)}</td>
                      <td>
                        {log.equipmentName} <span className="text-muted">#{log.equipmentId}</span>
                      </td>
                      <td>{log.performedBy}</td>
                    </tr>
                  ))}
                </tbody>
              </Table>
            </Tab.Pane>
          ))}

          {/* Güncelleme kayıtları: değişen her alan eski → yeni değeriyle */}
          <Tab.Pane eventKey="UPDATE">
            <Table bordered hover responsive className="bg-white">
              <thead>
                <tr>
                  <th>Tarih</th>
                  <th>Ekipman</th>
                  <th>İşlemi Yapan</th>
                  <th>Değişiklikler</th>
                </tr>
              </thead>
              <tbody>
                {logsByAction.UPDATE.length === 0 && <EmptyRow colSpan={4} />}
                {logsByAction.UPDATE.map((log) => (
                  <tr key={log.id}>
                    <td className="text-nowrap">{formatDateTime(log.createdAt)}</td>
                    <td>
                      {log.equipmentName} <span className="text-muted">#{log.equipmentId}</span>
                    </td>
                    <td>{log.performedBy}</td>
                    <td>
                      {log.changes.map((c) => (
                        <div key={c.field} className="change-row">
                          <Badge bg="light" text="dark" className="me-2">
                            {FIELD_LABELS[c.field] || c.field}
                          </Badge>
                          <span className="text-danger text-decoration-line-through">{c.oldValue ?? "boş"}</span>
                          {" → "}
                          <span className="text-success">{c.newValue ?? "boş"}</span>
                        </div>
                      ))}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Tab.Pane>

          {/* Rezervasyonlar */}
          <Tab.Pane eventKey="RESERVATIONS">
            <Row className="g-2 mb-3">
              <Col md={4}>
                <Form.Control name="user" placeholder="Kişi adı" value={filters.user} onChange={setFilter} />
              </Col>
              <Col md={4}>
                <Form.Control
                  name="equipment"
                  placeholder="Ekipman adı veya seri no"
                  value={filters.equipment}
                  onChange={setFilter}
                />
              </Col>
              <Col md={3}>
                <Form.Control type="date" name="date" value={filters.date} onChange={setFilter} />
              </Col>
              <Col md={1} className="d-grid">
                <Button variant="outline-secondary" onClick={() => setFilters(emptyFilters)} title="Filtreleri temizle">
                  Temizle
                </Button>
              </Col>
            </Row>

            <Table striped bordered hover responsive className="bg-white">
              <thead>
                <tr>
                  <th>Rezerve Eden</th>
                  <th>Ekipman</th>
                  <th>Seri No</th>
                  <th>Başlangıç</th>
                  <th>Bitiş</th>
                </tr>
              </thead>
              <tbody>
                {filteredReservations.length === 0 && <EmptyRow colSpan={5} />}
                {filteredReservations.map((r) => (
                  <tr key={r.id}>
                    <td>{r.userFullName}</td>
                    <td>{r.equipmentName}</td>
                    <td>
                      <code>{r.serialNumber}</code>
                    </td>
                    <td className="text-nowrap">{formatDateTime(r.startTime)}</td>
                    <td className="text-nowrap">{formatDateTime(r.endTime)}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Tab.Pane>
        </Tab.Content>
      </Tab.Container>
    </>
  );
}

function EmptyRow({ colSpan }) {
  return (
    <tr>
      <td colSpan={colSpan} className="text-center text-muted py-4">
        Kayıt bulunamadı
      </td>
    </tr>
  );
}

export default ReportsPage;
