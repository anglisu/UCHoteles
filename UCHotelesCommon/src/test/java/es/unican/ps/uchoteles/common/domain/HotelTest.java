package es.unican.ps.uchoteles.common.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;

class HotelTest {

    @Test
    void getHabitacionYAsociacionBidireccional() {
        Hotel hotel = new Hotel("Hotel Sardinero", "Plaza Italia 1", "Santander", Categoria.FOUR_STARS);
        Habitacion doble = new Habitacion(TipoHabitacion.DOBLE, 90.0, 20);
        Habitacion suite = new Habitacion(TipoHabitacion.SUITE, 250.0, 2);

        assertNull(hotel.getHabitacion(TipoHabitacion.DOBLE));

        hotel.addHabitacion(doble);
        hotel.addHabitacion(suite);
        assertSame(doble, hotel.getHabitacion(TipoHabitacion.DOBLE));
        assertSame(suite, hotel.getHabitacion(TipoHabitacion.SUITE));
        assertNull(hotel.getHabitacion(TipoHabitacion.INDIVIDUAL));
        assertSame(hotel, doble.getHotel());

        hotel.removeHabitacion(doble);
        assertNull(hotel.getHabitacion(TipoHabitacion.DOBLE));
        assertNull(doble.getHotel());
        assertEquals(1, hotel.getHabitaciones().size());
    }
}
