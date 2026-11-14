import { useEffect, useState } from "react";
import { Button, Col, Form, Modal, Row, Spinner } from "react-bootstrap";
import client, { getErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";
import { toDateTimeLocal } from "../utils/format";

// Varsayılan aralık: bir sonraki tam saatten başlayan 1 saat
function defaultRange() {
  const start = new Date();
  start.setHours(start.getHours() + 1, 0, 0, 0);
  const end = new Date(start);
  end.setHours(end.getHours() + 1);
  return { startTime: toDateTimeLocal(start), endTime: toDateTimeLocal(end) };
}

/**
 * Yeni rezervasyon modalı.
 * initialValues ile ekipman veya zaman aralığı önceden seçilebilir.
 */
function ReservationFormModal({ show, equipmentOptions, initialValues, onHide, onSaved }) {
  const { showToast } = useToast();
  const [form, setForm] = useState({ equipmentId: "", startTime: "", endTime: "" });
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (show) {
      setForm({ equipmentId: "", ...defaultRange(), ...initialValues });
    }
  }, [show, initialValues]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (form.endTime <= form.startTime) {
      showToast("Bitiş zamanı başlangıçtan sonra olmalıdır", "danger");
      return;
    }

    setSaving(true);
    try {
      const res = await client.post("/reservations", {
        equipmentId: Number(form.equipmentId),
        startTime: form.startTime,
        endTime: form.endTime,
      });
      showToast("Rezervasyon oluşturuldu");
      onSaved(res.data);
    } catch (err) {
      showToast(getErrorMessage(err, "Rezervasyon oluşturulamadı"), "danger");
    } finally {
      setSaving(false);
    }
  };

  return (
    <Modal show={show} onHide={onHide} centered>
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>Yeni Rezervasyon</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Form.Group className="mb-3">
            <Form.Label>Ekipman</Form.Label>
            <Form.Select name="equipmentId" value={form.equipmentId} onChange={handleChange} required>
              <option value="">Ekipman seçiniz</option>
              {equipmentOptions.map((eq) => (
                <option key={eq.id} value={eq.id}>
                  {eq.name} — {eq.serialNumber}
                </option>
              ))}
            </Form.Select>
          </Form.Group>
          <Row>
            <Col sm={6}>
              <Form.Group className="mb-3">
                <Form.Label>Başlangıç</Form.Label>
                <Form.Control
                  type="datetime-local"
                  name="startTime"
                  value={form.startTime}
                  onChange={handleChange}
                  required
                />
              </Form.Group>
            </Col>
            <Col sm={6}>
              <Form.Group className="mb-3">
                <Form.Label>Bitiş</Form.Label>
                <Form.Control
                  type="datetime-local"
                  name="endTime"
                  value={form.endTime}
                  min={form.startTime}
                  onChange={handleChange}
                  required
                />
              </Form.Group>
            </Col>
          </Row>
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={onHide}>
            Vazgeç
          </Button>
          <Button type="submit" variant="success" disabled={saving}>
            {saving ? <Spinner size="sm" animation="border" /> : "Rezerve Et"}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default ReservationFormModal;
