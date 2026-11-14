import { useEffect, useState } from "react";
import { Button, Col, Form, Modal, Row, Spinner } from "react-bootstrap";
import client, { getErrorMessage } from "../api/client";
import { useToast } from "../context/ToastContext";

const emptyForm = {
  name: "",
  brand: "",
  model: "",
  serialNumber: "",
  calibrationDate: "",
  description: "",
};

/**
 * Ekipman ekleme ve düzenleme için ortak modal.
 * equipment verilirse düzenleme, verilmezse ekleme modunda açılır.
 */
function EquipmentFormModal({ show, equipment, onHide, onSaved }) {
  const { showToast } = useToast();
  const [form, setForm] = useState(emptyForm);
  const [fieldErrors, setFieldErrors] = useState({});
  const [saving, setSaving] = useState(false);
  const isEdit = Boolean(equipment);

  // Modal her açıldığında formu doldur / temizle
  useEffect(() => {
    if (show) {
      setForm(
        equipment
          ? {
              ...emptyForm,
              ...equipment,
              calibrationDate: equipment.calibrationDate ?? "",
              description: equipment.description ?? "",
            }
          : emptyForm
      );
      setFieldErrors({});
    }
  }, [show, equipment]);

  const handleChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const handleSubmit = async (e) => {
    e.preventDefault();
    setSaving(true);
    const payload = {
      name: form.name,
      brand: form.brand,
      model: form.model,
      serialNumber: form.serialNumber,
      calibrationDate: form.calibrationDate || null,
      description: form.description,
    };

    try {
      const res = isEdit
        ? await client.put(`/equipment/${equipment.id}`, payload)
        : await client.post("/equipment", payload);
      showToast(isEdit ? "Ekipman güncellendi" : "Ekipman eklendi");
      onSaved(res.data);
    } catch (err) {
      setFieldErrors(err.response?.data?.fieldErrors || {});
      showToast(getErrorMessage(err, "Kaydedilemedi"), "danger");
    } finally {
      setSaving(false);
    }
  };

  const field = (name, label, props = {}) => (
    <Form.Group className="mb-3">
      <Form.Label>{label}</Form.Label>
      <Form.Control
        name={name}
        value={form[name]}
        onChange={handleChange}
        isInvalid={!!fieldErrors[name]}
        {...props}
      />
      <Form.Control.Feedback type="invalid">{fieldErrors[name]}</Form.Control.Feedback>
    </Form.Group>
  );

  return (
    <Modal show={show} onHide={onHide} centered>
      <Form onSubmit={handleSubmit}>
        <Modal.Header closeButton>
          <Modal.Title>{isEdit ? "Ekipmanı Düzenle" : "Yeni Ekipman"}</Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <Row>
            <Col sm={6}>{field("brand", "Marka", { required: true, maxLength: 100 })}</Col>
            <Col sm={6}>{field("model", "Model", { required: true, maxLength: 100 })}</Col>
          </Row>
          <Row>
            <Col sm={6}>{field("name", "Ekipman Adı", { required: true, maxLength: 100 })}</Col>
            <Col sm={6}>{field("serialNumber", "Seri No", { required: true, maxLength: 100 })}</Col>
          </Row>
          {field("calibrationDate", "Kalibrasyon Tarihi", { type: "date" })}
          {field("description", "Açıklama", { as: "textarea", rows: 3, maxLength: 500 })}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="secondary" onClick={onHide}>
            Vazgeç
          </Button>
          <Button type="submit" disabled={saving}>
            {saving ? <Spinner size="sm" animation="border" /> : "Kaydet"}
          </Button>
        </Modal.Footer>
      </Form>
    </Modal>
  );
}

export default EquipmentFormModal;
