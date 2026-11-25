package es.unican.ps.uchoteles.businessLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.unican.ps.uchoteles.common.daoLayer.IHotelesDAO;
import es.unican.ps.uchoteles.common.daoLayer.IReservasDAO;
import es.unican.ps.uchoteles.common.domain.Categoria;
import es.unican.ps.uchoteles.common.domain.DatosCliente;
import es.unican.ps.uchoteles.common.domain.DatosPago;
import es.unican.ps.uchoteles.common.domain.DisponibilidadHabitacion;
import es.unican.ps.uchoteles.common.domain.EstadoReserva;
import es.unican.ps.uchoteles.common.domain.Habitacion;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.ReservaTipoHabitacion;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.domain.TipoTarjeta;
import es.unican.ps.uchoteles.common.exceptions.CancelacionNoPermitidaException;
import es.unican.ps.uchoteles.common.exceptions.DataAccessException;
import es.unican.ps.uchoteles.common.exceptions.HabitacionNoDisponibleException;
import es.unican.ps.uchoteles.common.exceptions.ReservaCaducadaException;
import es.unican.ps.uchoteles.common.exceptions.ReservaNoAccesibleException;
import jakarta.ejb.Timer;
import jakarta.ejb.TimerConfig;
import jakarta.ejb.TimerService;

/**
 * Pruebas unitarias de GestionReservas aislando la capa DAO y el TimerService
 * con objetos mock (Mockito).
 */
@ExtendWith(MockitoExtension.class)
class GestionReservasTest {

    private static final Long ID_HOTEL = 1L;
    private static final Long ID_DOBLE = 10L;
    private static final Long ID_SUITE = 11L;
    private static final Long ID_RESERVA = 100L;
    private static final String DNI = "12345678Z";

    @Mock
    private IHotelesDAO hotelesDAO;
    @Mock
    private IReservasDAO reservasDAO;
    @Mock
    private TimerService timerService;

    @InjectMocks
    private GestionReservas sut;

    private Hotel hotel;
    private Habitacion doble;
    private Habitacion suite;
    private LocalDate entrada;
    private LocalDate salida;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Hotel Sardinero", "Plaza Italia 1", "Santander", Categoria.FOUR_STARS);
        hotel.setId(ID_HOTEL);
        doble = new Habitacion(TipoHabitacion.DOBLE, 80.0, 5);
        doble.setId(ID_DOBLE);
        suite = new Habitacion(TipoHabitacion.SUITE, 200.0, 2);
        suite.setId(ID_SUITE);
        hotel.addHabitacion(doble);
        hotel.addHabitacion(suite);
        entrada = LocalDate.now().plusDays(30);
        salida = entrada.plusDays(3);
    }

    private Reserva reservaConfirmada(LocalDate fechaEntrada) {
        Reserva r = new Reserva(hotel, fechaEntrada, fechaEntrada.plusDays(2), EstadoReserva.CONFIRMADA);
        r.setId(ID_RESERVA);
        r.addReservaTipoHabitacion(new ReservaTipoHabitacion(1, doble));
        r.setCliente(new DatosCliente(DNI, "Ana", "Pérez", "López", "ana@example.com"));
        return r;
    }

    // ------------------------------------------------------------ consultarDisponibilidad

    @Nested
    class ConsultarDisponibilidad {

        @Test
        void devuelveLibresYPrecioPorCadaTipo() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_DOBLE, entrada, salida)).thenReturn(3);
            when(reservasDAO.habitacionesReservadas(ID_SUITE, entrada, salida)).thenReturn(0);

            List<DisponibilidadHabitacion> res = sut.consultarDisponibilidad(ID_HOTEL, entrada, salida);

            assertEquals(2, res.size());
            assertEquals(TipoHabitacion.DOBLE, res.get(0).getTipo());
            assertEquals(2, res.get(0).getLibres());
            assertEquals(80.0, res.get(0).getPrecioPorNoche(), 0.001);
            assertEquals(TipoHabitacion.SUITE, res.get(1).getTipo());
            assertEquals(2, res.get(1).getLibres());
        }

        @Test
        void libresNuncaNegativas() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(anyLong(), any(), any())).thenReturn(7);

            List<DisponibilidadHabitacion> res = sut.consultarDisponibilidad(ID_HOTEL, entrada, salida);

            assertTrue(res.stream().allMatch(d -> d.getLibres() == 0));
        }

        @Test
        void hotelInexistenteDevuelveListaVacia() {
            when(hotelesDAO.hotel(99L)).thenReturn(null);
            assertTrue(sut.consultarDisponibilidad(99L, entrada, salida).isEmpty());
        }

        @Test
        void fechasNoValidasDevuelvenListaVacia() {
            assertTrue(sut.consultarDisponibilidad(ID_HOTEL, salida, entrada).isEmpty());
            assertTrue(sut.consultarDisponibilidad(ID_HOTEL, entrada, entrada).isEmpty());
            assertTrue(sut.consultarDisponibilidad(ID_HOTEL, null, salida).isEmpty());
            verify(hotelesDAO, never()).hotel(any());
        }

        @Test
        void propagaErrorDeAccesoADatos() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenThrow(new DataAccessException("BD caída"));
            assertThrows(DataAccessException.class,
                    () -> sut.consultarDisponibilidad(ID_HOTEL, entrada, salida));
        }
    }

    // ------------------------------------------------------------ bloquearHabitaciones

    @Nested
    class BloquearHabitaciones {

        private Map<TipoHabitacion, Integer> seleccion;

        @BeforeEach
        void setUp() {
            seleccion = new EnumMap<>(TipoHabitacion.class);
        }

        @Test
        void creaReservaPendienteConImporteYProgramaTemporizador() throws Exception {
            seleccion.put(TipoHabitacion.DOBLE, 2);
            seleccion.put(TipoHabitacion.SUITE, 1);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_DOBLE, entrada, salida)).thenReturn(3);
            when(reservasDAO.habitacionesReservadas(ID_SUITE, entrada, salida)).thenReturn(1);
            when(reservasDAO.crearReserva(any(Reserva.class))).thenAnswer(inv -> {
                Reserva r = inv.getArgument(0);
                r.setId(ID_RESERVA);
                return r;
            });

            Reserva r = sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion);

            assertEquals(ID_RESERVA, r.getId());
            assertEquals(EstadoReserva.PENDIENTE, r.getEstado());
            assertSame(hotel, r.getHotel());
            assertEquals(entrada, r.getFechaEntrada());
            assertEquals(salida, r.getFechaSalida());
            assertEquals(2, r.getReservasPorTipo().size());
            // (2*80 + 1*200) * 3 noches
            assertEquals(1080.0, r.getImporte(), 0.001);
            assertTrue(r.getFechaBloqueo() != null);

            ArgumentCaptor<TimerConfig> config = ArgumentCaptor.forClass(TimerConfig.class);
            verify(timerService).createSingleActionTimer(eq(10 * 60_000L), config.capture());
            assertEquals(ID_RESERVA, config.getValue().getInfo());
            assertTrue(!config.getValue().isPersistent());
        }

        @Test
        void ignoraTiposConCeroHabitaciones() throws Exception {
            seleccion.put(TipoHabitacion.DOBLE, 1);
            seleccion.put(TipoHabitacion.SUITE, 0);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_DOBLE, entrada, salida)).thenReturn(0);
            when(reservasDAO.crearReserva(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

            Reserva r = sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion);

            assertEquals(1, r.getReservasPorTipo().size());
            assertSame(doble, r.getReservasPorTipo().get(0).getHabitacion());
            verify(reservasDAO, never()).habitacionesReservadas(eq(ID_SUITE), any(), any());
        }

        @Test
        void justoLasHabitacionesLibresSePuedenReservar() throws Exception {
            seleccion.put(TipoHabitacion.SUITE, 2);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_SUITE, entrada, salida)).thenReturn(0);
            when(reservasDAO.crearReserva(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));

            Reserva r = sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion);

            assertEquals(2, r.getReservasPorTipo().get(0).getNumHabitaciones());
        }

        @Test
        void sinHabitacionesSuficientesLanzaExcepcionYNoGuarda() {
            seleccion.put(TipoHabitacion.DOBLE, 3);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_DOBLE, entrada, salida)).thenReturn(3);

            HabitacionNoDisponibleException e = assertThrows(HabitacionNoDisponibleException.class,
                    () -> sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion));

            assertEquals(TipoHabitacion.DOBLE, e.getTipo());
            assertEquals("Habitación de tipo DOBLE no disponible", e.getMessage());
            verify(reservasDAO, never()).crearReserva(any());
            verify(timerService, never()).createSingleActionTimer(anyLong(), any());
        }

        @Test
        void tipoQueElHotelNoTieneLanzaExcepcion() {
            seleccion.put(TipoHabitacion.INDIVIDUAL, 1);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);

            HabitacionNoDisponibleException e = assertThrows(HabitacionNoDisponibleException.class,
                    () -> sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion));
            assertEquals(TipoHabitacion.INDIVIDUAL, e.getTipo());
        }

        @Test
        void seleccionVaciaNoEsValida() {
            seleccion.put(TipoHabitacion.DOBLE, 0);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(IllegalArgumentException.class,
                    () -> sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion));
        }

        @Test
        void fechasOHotelNoValidos() {
            seleccion.put(TipoHabitacion.DOBLE, 1);
            assertThrows(IllegalArgumentException.class,
                    () -> sut.bloquearHabitaciones(ID_HOTEL, salida, entrada, seleccion));
            when(hotelesDAO.hotel(99L)).thenReturn(null);
            assertThrows(IllegalArgumentException.class,
                    () -> sut.bloquearHabitaciones(99L, entrada, salida, seleccion));
        }

        @Test
        void propagaErrorDeAccesoADatos() {
            seleccion.put(TipoHabitacion.DOBLE, 1);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.habitacionesReservadas(ID_DOBLE, entrada, salida)).thenReturn(0);
            when(reservasDAO.crearReserva(any())).thenThrow(new DataAccessException("BD caída"));

            assertThrows(DataAccessException.class,
                    () -> sut.bloquearHabitaciones(ID_HOTEL, entrada, salida, seleccion));
            verify(timerService, never()).createSingleActionTimer(anyLong(), any());
        }
    }

    // ------------------------------------------------------------ confirmarReserva

    @Nested
    class ConfirmarReserva {

        private final DatosCliente cliente = new DatosCliente(DNI, "Ana", "Pérez", "López", "ana@example.com");
        private final DatosPago pago = new DatosPago("4111111111111111", "123", 12, 2030, TipoTarjeta.VISA);

        private Reserva pendiente(LocalDateTime fechaBloqueo) {
            Reserva r = new Reserva(hotel, entrada, salida, EstadoReserva.PENDIENTE);
            r.setId(ID_RESERVA);
            r.setFechaBloqueo(fechaBloqueo);
            return r;
        }

        @Test
        void confirmaReservaPendiente() throws Exception {
            Reserva r = pendiente(LocalDateTime.now().minusMinutes(2));
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(r);
            when(reservasDAO.modificarReserva(r)).thenReturn(r);

            Reserva res = sut.confirmarReserva(ID_RESERVA, cliente, pago);

            assertEquals(EstadoReserva.CONFIRMADA, res.getEstado());
            assertSame(cliente, res.getCliente());
            assertSame(pago, res.getDatosPago());
            assertEquals(ID_RESERVA, res.getId());
            verify(reservasDAO).modificarReserva(r);
        }

        @Test
        void reservaYaLiberadaLanzaCaducada() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(null);
            assertThrows(ReservaCaducadaException.class,
                    () -> sut.confirmarReserva(ID_RESERVA, cliente, pago));
            verify(reservasDAO, never()).modificarReserva(any());
        }

        @Test
        void bloqueoExpiradoLanzaCaducada() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(pendiente(LocalDateTime.now().minusMinutes(11)));
            assertThrows(ReservaCaducadaException.class,
                    () -> sut.confirmarReserva(ID_RESERVA, cliente, pago));
        }

        @Test
        void reservaYaConfirmadaNoSePuedeConfirmarOtraVez() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(reservaConfirmada(entrada));
            assertThrows(ReservaCaducadaException.class,
                    () -> sut.confirmarReserva(ID_RESERVA, cliente, pago));
        }

        @Test
        void faltanDatos() {
            assertThrows(IllegalArgumentException.class, () -> sut.confirmarReserva(ID_RESERVA, null, pago));
            assertThrows(IllegalArgumentException.class, () -> sut.confirmarReserva(ID_RESERVA, cliente, null));
        }
    }

    // ------------------------------------------------------------ liberarBloqueo (@Timeout)

    @Nested
    class LiberarBloqueo {

        @Mock
        private Timer timer;

        @Test
        void eliminaReservaQueSiguePendiente() {
            Reserva r = new Reserva(hotel, entrada, salida, EstadoReserva.PENDIENTE);
            when(timer.getInfo()).thenReturn(ID_RESERVA);
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(r);

            sut.liberarBloqueo(timer);

            verify(reservasDAO).eliminarReserva(r);
        }

        @Test
        void noTocaReservaConfirmadaNiInexistente() {
            when(timer.getInfo()).thenReturn(ID_RESERVA);
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(reservaConfirmada(entrada), (Reserva) null);

            sut.liberarBloqueo(timer);
            sut.liberarBloqueo(timer);

            verify(reservasDAO, never()).eliminarReserva(any());
        }
    }

    // ------------------------------------------------------------ consultarReserva

    @Nested
    class ConsultarReserva {

        @Test
        void devuelveReservaSiCoincideElDni() throws Exception {
            Reserva r = reservaConfirmada(entrada);
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(r);

            assertSame(r, sut.consultarReserva(ID_RESERVA, DNI));
            assertSame(r, sut.consultarReserva(ID_RESERVA, " 12345678z "));
        }

        @Test
        void reservaInexistenteDevuelveNull() throws Exception {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(null);
            assertNull(sut.consultarReserva(ID_RESERVA, DNI));
        }

        @Test
        void reservaPendienteNoSeConsidera() throws Exception {
            when(reservasDAO.reserva(ID_RESERVA))
                    .thenReturn(new Reserva(hotel, entrada, salida, EstadoReserva.PENDIENTE));
            assertNull(sut.consultarReserva(ID_RESERVA, DNI));
        }

        @Test
        void dniDistintoLanzaNoAccesible() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(reservaConfirmada(entrada));
            ReservaNoAccesibleException e = assertThrows(ReservaNoAccesibleException.class,
                    () -> sut.consultarReserva(ID_RESERVA, "87654321X"));
            assertEquals("Reserva no accesible", e.getMessage());
        }
    }

    // ------------------------------------------------------------ cancelarReserva

    @Nested
    class CancelarReserva {

        @Test
        void cancelaConDosDiasOMasDeAntelacion() throws Exception {
            Reserva r = reservaConfirmada(LocalDate.now().plusDays(2));
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(r);

            sut.cancelarReserva(ID_RESERVA, DNI);

            verify(reservasDAO).eliminarReserva(r);
        }

        @Test
        void conMenosDeDosDiasNoSeCancela() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(reservaConfirmada(LocalDate.now().plusDays(1)));

            CancelacionNoPermitidaException e = assertThrows(CancelacionNoPermitidaException.class,
                    () -> sut.cancelarReserva(ID_RESERVA, DNI));
            assertEquals("Límite de cancelación de reserva sobrepasado", e.getMessage());
            verify(reservasDAO, never()).eliminarReserva(any());
        }

        @Test
        void reservaDeOtroClienteNoSeCancela() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(reservaConfirmada(entrada));
            assertThrows(ReservaNoAccesibleException.class, () -> sut.cancelarReserva(ID_RESERVA, "87654321X"));
            verify(reservasDAO, never()).eliminarReserva(any());
        }

        @Test
        void reservaInexistente() {
            when(reservasDAO.reserva(ID_RESERVA)).thenReturn(null);
            assertThrows(ReservaNoAccesibleException.class, () -> sut.cancelarReserva(ID_RESERVA, DNI));
        }
    }
}
