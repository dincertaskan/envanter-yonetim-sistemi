import { useEffect, useMemo, useRef, useState } from "react";
import { Button, Form, Modal } from "react-bootstrap";
import { FaPlus } from "react-icons/fa";
import FullCalendar from "@fullcalendar/react";
import dayGridPlugin from "@fullcalendar/daygrid";
import timeGridPlugin from "@fullcalendar/timegrid";
import interactionPlugin from "@fullcalendar/interaction";
import trLocale from "@fullcalendar/core/locales/tr";
import client, { getErrorMessage } from "../api/client";
import { useAuth } from "../context/AuthContext";
import { useToast } from "../context/ToastContext";
import ReservationFormModal from "../components/ReservationFormModal";
import { formatDateTime, toDateTimeLocal } from "../utils/format";
import "../styles/calendar.css";

// Her ekipman takvimde hep aynı renkte görünsün
const COLORS = ["#2563eb", "#16a34a", "#d97706", "#dc2626", "#7c3aed", "#0891b2", "#db2777", "#4b5563"];
const colorFor = (equipmentId) => COLORS[equipmentId % COLORS.length];

function ReservationsPage() {
  const { user, isAdmin } = useAuth();
  const { showToast } = useToast();
  const calendarRef = useRef(null);

  const [reservations, setReservations] = useState([]);
  const [equipment, setEquipment] = useState([]);
  const [equipmentFilter, setEquipmentFilter] = useState("");
  const [onlyMine, setOnlyMine] = useState(false);

  // newReservation = { initialValues } | null, selected = rezervasyon detayı | null
  const [newReservation, setNewReservation] = useState(null);
  const [selected, setSelected] = useState(null);

  useEffect(() => {
    Promise.all([client.get("/reservations"), client.get("/equipment")])
      .then(([resRes, eqRes]) => {
        setReservations(resRes.data);
        setEquipment(eqRes.data);
      })
      .catch((err) => showToast(getErrorMessage(err, "Veriler yüklenemedi"), "danger"));
  }, [showToast]);

  const events = useMemo(
    () =>
      reservations
        .filter((r) => !equipmentFilter || r.equipmentId === Number(equipmentFilter))
        .filter((r) => !onlyMine || r.userId === user.id)
        .map((r) => ({
          id: String(r.id),
          title: `${r.equipmentName} · ${r.userFullName}`,
          start: r.startTime,
          end: r.endTime,
          backgroundColor: colorFor(r.equipmentId),
          borderColor: colorFor(r.equipmentId),
          extendedProps: r,
        })),
    [reservations, equipmentFilter, onlyMine, user.id]
  );

  // Ay görünümünde güne tıklayınca o günün saatlik görünümüne geç
  const handleDateClick = (info) => {
    const api = calendarRef.current.getApi();
    if (api.view.type === "dayGridMonth") {
      api.changeView("timeGridDay", info.date);
    }
  };

  // Saatlik görünümde bir aralığı sürükleyerek seçince rezervasyon formunu doldur
  const handleSelect = (info) => {
    // Ay görünümündeki tıklamaları dateClick yönetir
    if (info.view.type === "dayGridMonth") return;
    calendarRef.current.getApi().unselect();
    if (info.start < new Date()) {
      showToast("Geçmiş bir zamana rezervasyon yapılamaz", "warning");
      return;
    }
    setNewReservation({
      initialValues: {
        equipmentId: equipmentFilter,
        startTime: toDateTimeLocal(info.start),
        endTime: toDateTimeLocal(info.end),
      },
    });
  };

  const handleCancel = async () => {
    if (!window.confirm("Bu rezervasyon iptal edilsin mi?")) return;
    try {
      await client.delete(`/reservations/${selected.id}`);
      setReservations((prev) => prev.filter((r) => r.id !== selected.id));
      setSelected(null);
      showToast("Rezervasyon iptal edildi");
    } catch (err) {
      showToast(getErrorMessage(err, "İptal edilemedi"), "danger");
    }
  };

  const canCancel = selected && (isAdmin || selected.userId === user.id);

  return (
    <>
      <div className="page-header">
        <h1 className="h3 mb-0">Rezervasyonlar</h1>
        <Button variant="success" onClick={() => setNewReservation({ initialValues: undefined })}>
          <FaPlus className="me-1" /> Yeni Rezervasyon
        </Button>
      </div>

      <div className="d-flex flex-wrap align-items-center gap-3 mb-3">
        <Form.Select
          className="w-auto"
          value={equipmentFilter}
          onChange={(e) => setEquipmentFilter(e.target.value)}
        >
          <option value="">Tüm ekipmanlar</option>
          {equipment.map((eq) => (
            <option key={eq.id} value={eq.id}>
              {eq.name} — {eq.serialNumber}
            </option>
          ))}
        </Form.Select>
        <Form.Check
          type="switch"
          id="only-mine"
          label="Sadece benim rezervasyonlarım"
          checked={onlyMine}
          onChange={(e) => setOnlyMine(e.target.checked)}
        />
      </div>

      <div className="calendar-card">
        <FullCalendar
          ref={calendarRef}
          plugins={[dayGridPlugin, timeGridPlugin, interactionPlugin]}
          locale={trLocale}
          initialView="dayGridMonth"
          firstDay={1}
          headerToolbar={{
            left: "prev,next today",
            center: "title",
            right: "dayGridMonth,timeGridWeek,timeGridDay",
          }}
          events={events}
          dateClick={handleDateClick}
          selectable
          selectMirror
          selectAllow={(info) => info.start.toDateString() === new Date(info.end - 1).toDateString()}
          select={handleSelect}
          eventClick={(info) => setSelected(info.event.extendedProps)}
          slotMinTime="07:00:00"
          slotMaxTime="21:00:00"
          allDaySlot={false}
          dayMaxEvents={3}
          height="auto"
        />
      </div>
      <p className="text-muted small mt-2">
        İpucu: Hafta veya gün görünümünde boş bir saat aralığını sürükleyerek hızlıca rezervasyon oluşturabilirsiniz.
      </p>

      <ReservationFormModal
        show={newReservation !== null}
        equipmentOptions={equipment}
        initialValues={newReservation?.initialValues}
        onHide={() => setNewReservation(null)}
        onSaved={(saved) => {
          setReservations((prev) => [...prev, saved]);
          setNewReservation(null);
        }}
      />

      {/* Rezervasyon detayı */}
      <Modal show={selected !== null} onHide={() => setSelected(null)} centered>
        <Modal.Header closeButton>
          <Modal.Title>Rezervasyon Detayı</Modal.Title>
        </Modal.Header>
        {selected && (
          <Modal.Body>
            <dl className="row mb-0">
              <dt className="col-4">Ekipman</dt>
              <dd className="col-8">{selected.equipmentName}</dd>
              <dt className="col-4">Seri No</dt>
              <dd className="col-8">
                <code>{selected.serialNumber}</code>
              </dd>
              <dt className="col-4">Rezerve Eden</dt>
              <dd className="col-8">{selected.userFullName}</dd>
              <dt className="col-4">Başlangıç</dt>
              <dd className="col-8">{formatDateTime(selected.startTime)}</dd>
              <dt className="col-4">Bitiş</dt>
              <dd className="col-8">{formatDateTime(selected.endTime)}</dd>
            </dl>
          </Modal.Body>
        )}
        <Modal.Footer>
          {canCancel && (
            <Button variant="outline-danger" onClick={handleCancel}>
              Rezervasyonu İptal Et
            </Button>
          )}
          <Button variant="secondary" onClick={() => setSelected(null)}>
            Kapat
          </Button>
        </Modal.Footer>
      </Modal>
    </>
  );
}

export default ReservationsPage;
