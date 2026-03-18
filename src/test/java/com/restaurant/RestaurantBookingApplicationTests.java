package com.restaurant;
import com.restaurant.model.*;
import com.restaurant.repository.BookingRepository;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.WorkingHoursRepository;
import com.restaurant.service.BookingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class RestaurantBookingApplicationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    private RestaurantTable testTable;

    @BeforeEach
    void setUp() {
        bookingRepository.deleteAll();
        tableRepository.deleteAll();
        workingHoursRepository.deleteAll();

        testTable = new RestaurantTable("T1", 4, Zone.INDOOR, 10.0, 10.0, Set.of(TableTag.WINDOW));
        testTable = tableRepository.save(testTable);

        WorkingHours wh = new WorkingHours(LocalTime.of(10, 0), LocalTime.of(22, 0));
        workingHoursRepository.save(wh);
    }

    @Test
    void createBooking_withinWorkingHours_shouldSucceed() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime start = LocalTime.of(12, 0);
        LocalTime end = LocalTime.of(14, 0);

        Booking booking = bookingService.createBooking(
                testTable.getId(), "Alice", 2, date, start, end, null);

        assertNotNull(booking.getId());
        assertEquals("Alice", booking.getGuestName());
        assertEquals(testTable.getId(), booking.getTableId());
        assertEquals(start, booking.getStartTime());
        assertEquals(end, booking.getEndTime());
    }

    @Test
    void createBooking_beforeOpeningTime_shouldFail() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime start = LocalTime.of(8, 0);
        LocalTime end = LocalTime.of(10, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(testTable.getId(), "Bob", 2, date, start, end, null));
        assertTrue(ex.getMessage().contains("opens at"));
    }

    @Test
    void createBooking_endTimeAfterClosing_shouldFail() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime start = LocalTime.of(21, 0);
        LocalTime end = LocalTime.of(23, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(testTable.getId(), "Carol", 2, date, start, end, null));
        assertTrue(ex.getMessage().contains("closes at"));
    }

    @Test
    void createBooking_atClosingTime_shouldFail() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime start = LocalTime.of(22, 0);
        LocalTime end = LocalTime.of(23, 30);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(testTable.getId(), "Dave", 2, date, start, end, null));
        assertTrue(ex.getMessage().contains("closes at"));
    }

    @Test
    void createBooking_endTimeWrappingMidnight_shouldFail() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime start = LocalTime.of(21, 0);
        LocalTime end = LocalTime.of(0, 0);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(testTable.getId(), "Eve", 2, date, start, end, null));
        assertTrue(ex.getMessage().contains("closes at"));
    }

    @Test
    void createBooking_conflictingTime_shouldFail() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        bookingService.createBooking(testTable.getId(), "First", 2, date,
                LocalTime.of(12, 0), LocalTime.of(14, 0), null);

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                bookingService.createBooking(testTable.getId(), "Second", 2, date,
                        LocalTime.of(13, 0), LocalTime.of(15, 0), null));
        assertTrue(ex.getMessage().contains("not available"));
    }

    @Test
    void getTablesWithStatus_noBookings_allShouldBeFree() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime time = LocalTime.of(12, 0);

        List<TableStatus> statuses = bookingService.getTablesWithStatus(date, time, null, null, null);

        assertFalse(statuses.isEmpty());
        assertTrue(statuses.stream().allMatch(ts -> ts.getStatus() == TableStatus.Status.FREE));
    }

    @Test
    void getTablesWithStatus_withBooking_shouldShowBooked() {
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime time = LocalTime.of(12, 0);
        bookingService.createBooking(testTable.getId(), "Guest", 2, date,
                LocalTime.of(12, 0), LocalTime.of(14, 0), null);

        List<TableStatus> statuses = bookingService.getTablesWithStatus(date, time, null, null, null);

        TableStatus ts = statuses.stream()
                .filter(s -> s.getId().equals(testTable.getId()))
                .findFirst().orElseThrow();
        assertEquals(TableStatus.Status.BOOKED, ts.getStatus());
    }

    @Test
    void getTablesWithStatus_withZoneFilter_shouldReturnOnlyMatchingZone() {
        RestaurantTable terraceTable = new RestaurantTable("T2", 6, Zone.TERRACE, 20.0, 70.0, Set.of());
        tableRepository.save(terraceTable);
        LocalDate date = LocalDate.of(2026, 3, 20);
        LocalTime time = LocalTime.of(12, 0);

        List<TableStatus> statuses = bookingService.getTablesWithStatus(date, time, null, Zone.TERRACE, null);

        assertTrue(statuses.stream().allMatch(ts -> ts.getZone() == Zone.TERRACE));
        assertEquals(1, statuses.size());
    }

    @Test
    void bookingEndpoint_validRequest_shouldReturn200() throws Exception {
        String json = """
                {
                    "tableId": %d,
                    "guestName": "API User",
                    "partySize": 2,
                    "date": "2026-03-20",
                    "startTime": "14:00",
                    "endTime": "16:00"
                }
                """.formatted(testTable.getId());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.guestName").value("API User"))
                .andExpect(jsonPath("$.tableId").value(testTable.getId()));
    }

    @Test
    void bookingEndpoint_beforeOpening_shouldReturn400WithError() throws Exception {
        String json = """
                {
                    "tableId": %d,
                    "guestName": "Early Bird",
                    "partySize": 2,
                    "date": "2026-03-20",
                    "startTime": "08:00",
                    "endTime": "10:00"
                }
                """.formatted(testTable.getId());

        mockMvc.perform(post("/api/bookings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value(org.hamcrest.Matchers.containsString("opens at")));
    }

    @Test
    void workingHoursEndpoint_getDefault_shouldReturnHours() throws Exception {
        mockMvc.perform(get("/api/settings/working-hours"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTime").value("10:00:00"))
                .andExpect(jsonPath("$.closeTime").value("22:00:00"));
    }

    @Test
    void workingHoursEndpoint_update_shouldPersist() throws Exception {
        String json = """
                { "openTime": "09:00", "closeTime": "23:00" }
                """;

        mockMvc.perform(put("/api/settings/working-hours")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.openTime").value("09:00:00"))
                .andExpect(jsonPath("$.closeTime").value("23:00:00"));

        Booking booking = bookingService.createBooking(testTable.getId(), "Late",
                2, LocalDate.of(2026, 3, 20), LocalTime.of(22, 0), LocalTime.of(23, 0), null);
        assertNotNull(booking.getId());
    }
}
