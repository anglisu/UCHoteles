package es.unican.ps.uchoteles.common.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

class ReservaTest {

    private static final LocalDate ENTRADA = LocalDate.of(2027, 3, 10);

    @Test
    void getNumNoches() {
        Reserva r = new Reserva(null, ENTRADA, ENTRADA.plusDays(3), EstadoReserva.PENDIENTE);
        assertEquals(3, r.getNumNoches());
        r.setFechaSalida(ENTRADA.plusDays(1));
        assertEquals(1, r.getNumNoches());
        r.setFechaSalida(null);
        assertEquals(0, r.getNumNoches());
    }

    @Test
    void calcularImporte() {
        Habitacion doble = new Habitacion(TipoHabitacion.DOBLE, 80.0, 10);
        Habitacion suite = new Habitacion(TipoHabitacion.SUITE, 200.0, 2);
        Reserva r = new Reserva(null, ENTRADA, ENTRADA.plusDays(2), EstadoReserva.PENDIENTE);

        // Sin habitaciones el importe es 0
        assertEquals(0.0, r.calcularImporte(), 0.001);

        ReservaTipoHabitacion rt = new ReservaTipoHabitacion(2, doble);
        r.addReservaTipoHabitacion(rt);
        r.addReservaTipoHabitacion(new ReservaTipoHabitacion(1, suite));

        // (2*80 + 1*200) * 2 noches = 720
        assertEquals(720.0, r.calcularImporte(), 0.001);
        assertEquals(720.0, r.getImporte(), 0.001);
        assertSame(r, rt.getReserva());
    }
}
