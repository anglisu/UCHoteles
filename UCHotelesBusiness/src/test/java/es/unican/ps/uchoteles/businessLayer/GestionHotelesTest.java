package es.unican.ps.uchoteles.businessLayer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import es.unican.ps.uchoteles.common.daoLayer.IHotelesDAO;
import es.unican.ps.uchoteles.common.daoLayer.IReservasDAO;
import es.unican.ps.uchoteles.common.domain.Categoria;
import es.unican.ps.uchoteles.common.domain.Habitacion;
import es.unican.ps.uchoteles.common.domain.Hotel;
import es.unican.ps.uchoteles.common.domain.Reserva;
import es.unican.ps.uchoteles.common.domain.TipoHabitacion;
import es.unican.ps.uchoteles.common.exceptions.DataAccessException;
import es.unican.ps.uchoteles.common.exceptions.OperacionNoValidaException;

/**
 * Pruebas unitarias de GestionHoteles aislando la capa DAO con Mockito.
 */
@ExtendWith(MockitoExtension.class)
class GestionHotelesTest {

    private static final Long ID_HOTEL = 1L;
    private static final Long ID_DOBLE = 10L;

    @Mock
    private IHotelesDAO hotelesDAO;
    @Mock
    private IReservasDAO reservasDAO;

    @InjectMocks
    private GestionHoteles sut;

    private Hotel hotel;
    private Habitacion doble;
    private Habitacion suite;

    @BeforeEach
    void setUp() {
        hotel = new Hotel("Hotel Sardinero", "Plaza Italia 1", "Santander", Categoria.FOUR_STARS);
        hotel.setId(ID_HOTEL);
        doble = new Habitacion(TipoHabitacion.DOBLE, 80.0, 5);
        doble.setId(ID_DOBLE);
        suite = new Habitacion(TipoHabitacion.SUITE, 200.0, 2);
        hotel.addHabitacion(doble);
        hotel.addHabitacion(suite);
    }

    // ------------------------------------------------------------ consultas

    @Nested
    class Consultas {

        @Test
        void buscarHotelPorNombre() {
            when(hotelesDAO.hotelPorNombre("Hotel Sardinero")).thenReturn(hotel);
            assertSame(hotel, sut.buscarHotelPorNombre(" Hotel Sardinero "));
            assertSame(hotel, sut.consultarHotel("Hotel Sardinero"));
        }

        @Test
        void hotelNoEncontradoDevuelveNull() {
            when(hotelesDAO.hotelPorNombre("Otro")).thenReturn(null);
            assertNull(sut.buscarHotelPorNombre("Otro"));
            assertNull(sut.buscarHotelPorNombre(""));
            assertNull(sut.buscarHotelPorNombre(null));
            verify(hotelesDAO, never()).hotelPorNombre("");
        }

        @Test
        void buscarHotelesPorLocalidad() {
            when(hotelesDAO.hotelesPorLocalidad("Santander")).thenReturn(List.of(hotel));
            when(hotelesDAO.hotelesPorLocalidad("Laredo")).thenReturn(List.of());

            assertEquals(List.of(hotel), sut.buscarHotelesPorLocalidad("Santander"));
            assertTrue(sut.buscarHotelesPorLocalidad("Laredo").isEmpty());
            assertTrue(sut.buscarHotelesPorLocalidad(null).isEmpty());
        }

        @Test
        void consultarReservasHotel() {
            List<Reserva> reservas = List.of(new Reserva(), new Reserva());
            when(reservasDAO.reservasHotel(ID_HOTEL)).thenReturn(reservas);
            assertSame(reservas, sut.consultarReservasHotel(ID_HOTEL));
            assertTrue(sut.consultarReservasHotel(null).isEmpty());
        }

        @Test
        void propagaErrorDeAccesoADatos() {
            when(hotelesDAO.hotelPorNombre(anyString())).thenThrow(new DataAccessException("BD caída"));
            assertThrows(DataAccessException.class, () -> sut.buscarHotelPorNombre("Hotel Sardinero"));
        }
    }

    // ------------------------------------------------------------ modificarHabitaciones

    @Nested
    class ModificarHabitaciones {

        @Test
        void actualizaPrecioYDisponibles() throws Exception {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(hotelesDAO.modificarHotel(hotel)).thenReturn(hotel);

            Hotel res = sut.modificarHabitaciones(ID_HOTEL,
                    List.of(new Habitacion(TipoHabitacion.DOBLE, 95.0, 8)));

            assertEquals(95.0, res.getHabitacion(TipoHabitacion.DOBLE).getPrecioPorNoche(), 0.001);
            assertEquals(8, res.getHabitacion(TipoHabitacion.DOBLE).getDisponibles());
            assertEquals(200.0, res.getHabitacion(TipoHabitacion.SUITE).getPrecioPorNoche(), 0.001);
            verify(hotelesDAO).modificarHotel(hotel);
        }

        @Test
        void valorNoValidoNoModificaNada() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);

            assertThrows(OperacionNoValidaException.class, () -> sut.modificarHabitaciones(ID_HOTEL,
                    List.of(new Habitacion(TipoHabitacion.DOBLE, 95.0, 8),
                            new Habitacion(TipoHabitacion.SUITE, -1.0, 2))));

            assertEquals(80.0, doble.getPrecioPorNoche(), 0.001);
            assertEquals(5, doble.getDisponibles());
            verify(hotelesDAO, never()).modificarHotel(any());
        }

        @Test
        void disponiblesNegativasNoSonValidas() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class, () -> sut.modificarHabitaciones(ID_HOTEL,
                    List.of(new Habitacion(TipoHabitacion.DOBLE, 95.0, -1))));
        }

        @Test
        void tipoQueElHotelNoTiene() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class, () -> sut.modificarHabitaciones(ID_HOTEL,
                    List.of(new Habitacion(TipoHabitacion.INDIVIDUAL, 50.0, 3))));
        }

        @Test
        void hotelInexistente() {
            when(hotelesDAO.hotel(99L)).thenReturn(null);
            assertThrows(OperacionNoValidaException.class, () -> sut.modificarHabitaciones(99L,
                    List.of(new Habitacion(TipoHabitacion.DOBLE, 95.0, 8))));
        }
    }

    // ------------------------------------------------------------ anhadirTipoHabitacion

    @Nested
    class AnhadirTipoHabitacion {

        @Test
        void anhadeTipoNuevo() throws Exception {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(hotelesDAO.modificarHotel(hotel)).thenReturn(hotel);
            Habitacion individual = new Habitacion(TipoHabitacion.INDIVIDUAL, 50.0, 10);

            Hotel res = sut.anhadirTipoHabitacion(ID_HOTEL, individual);

            assertSame(individual, res.getHabitacion(TipoHabitacion.INDIVIDUAL));
            assertSame(hotel, individual.getHotel());
            assertEquals(3, res.getHabitaciones().size());
        }

        @Test
        void tipoRepetidoNoSePermite() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class, () -> sut.anhadirTipoHabitacion(ID_HOTEL,
                    new Habitacion(TipoHabitacion.DOBLE, 70.0, 3)));
            verify(hotelesDAO, never()).modificarHotel(any());
        }

        @Test
        void datosNoValidos() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class, () -> sut.anhadirTipoHabitacion(ID_HOTEL,
                    new Habitacion(TipoHabitacion.INDIVIDUAL, 0.0, 3)));
            assertThrows(OperacionNoValidaException.class, () -> sut.anhadirTipoHabitacion(ID_HOTEL,
                    new Habitacion(null, 50.0, 3)));
            assertThrows(OperacionNoValidaException.class, () -> sut.anhadirTipoHabitacion(ID_HOTEL, null));
        }
    }

    // ------------------------------------------------------------ eliminarTipoHabitacion

    @Nested
    class EliminarTipoHabitacion {

        @Test
        void eliminaTipoSinReservasFuturas() throws Exception {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.existenReservasDesde(ID_DOBLE, LocalDate.now())).thenReturn(false);
            when(hotelesDAO.modificarHotel(hotel)).thenReturn(hotel);

            Hotel res = sut.eliminarTipoHabitacion(ID_HOTEL, TipoHabitacion.DOBLE);

            assertNull(res.getHabitacion(TipoHabitacion.DOBLE));
            assertEquals(1, res.getHabitaciones().size());
        }

        @Test
        void conReservasFuturasNoSeElimina() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            when(reservasDAO.existenReservasDesde(ID_DOBLE, LocalDate.now())).thenReturn(true);

            assertThrows(OperacionNoValidaException.class,
                    () -> sut.eliminarTipoHabitacion(ID_HOTEL, TipoHabitacion.DOBLE));
            assertSame(doble, hotel.getHabitacion(TipoHabitacion.DOBLE));
            verify(hotelesDAO, never()).modificarHotel(any());
        }

        @Test
        void tipoInexistente() {
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class,
                    () -> sut.eliminarTipoHabitacion(ID_HOTEL, TipoHabitacion.INDIVIDUAL));
        }

        @Test
        void noSePuedeEliminarElUnicoTipo() {
            hotel.removeHabitacion(suite);
            when(hotelesDAO.hotel(ID_HOTEL)).thenReturn(hotel);
            assertThrows(OperacionNoValidaException.class,
                    () -> sut.eliminarTipoHabitacion(ID_HOTEL, TipoHabitacion.DOBLE));
        }
    }
}
