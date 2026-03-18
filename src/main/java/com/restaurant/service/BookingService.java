package com.restaurant.service;
import com.restaurant.model.*;
import com.restaurant.repository.BookingRepository;
import com.restaurant.repository.TableRepository;
import com.restaurant.repository.WorkingHoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class BookingService {

    @Autowired
    private TableRepository tableRepository;

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    public List<TableStatus> getTablesWithStatus(LocalDate date, LocalTime time, Integer partySize, Zone zone, Set<TableTag> preferences) {
        List<RestaurantTable> allTables = tableRepository.findAll();

        if (zone != null) {
            allTables = allTables.stream()
                .filter(table -> table.getZone() == zone)
                .collect(Collectors.toList());
        }

        LocalTime endTime = time.plusHours(2);
        Map<Long, TableStatus.Status> tableStatuses = new HashMap<>();
        Map<Long, Integer> tableScores = new HashMap<>();

        for (RestaurantTable table : allTables) {
            boolean isBooked = !bookingRepository.findConflictingBookings(
                table.getId(), date, time, endTime).isEmpty();
            if (isBooked) {
                tableStatuses.put(table.getId(), TableStatus.Status.BOOKED);
                tableScores.put(table.getId(), 0);
            } else {
                tableStatuses.put(table.getId(), TableStatus.Status.FREE);
                tableScores.put(table.getId(), calculateScore(table, partySize, zone, preferences));
            }
        }

        List<Long> recommendedGroup = null;
        if (partySize != null) {
            recommendedGroup = markRecommendedTables(allTables, tableStatuses, tableScores, partySize);
        }

        final List<Long> group = recommendedGroup;
        return allTables.stream()
            .map(table -> {
                TableStatus ts = new TableStatus(table,
                    tableStatuses.get(table.getId()),
                    tableScores.get(table.getId()));
                if (group != null && group.contains(table.getId())) {
                    ts.setRecommendedWith(group);
                }
                return ts;
            })
            .collect(Collectors.toList());
    }

    private int calculateScore(RestaurantTable table, Integer partySize, Zone preferredZone, Set<TableTag> preferences) {
        int score = 100;
        if (partySize != null) {
            int wastedSeats = Math.max(0, table.getCapacity() - partySize);
            score -= wastedSeats * 5;

            if (table.getCapacity() < partySize) {
                score -= 50;
            }
        }
        if (preferredZone != null && table.getZone() == preferredZone) {
            score += 20;
        }
        if (preferences != null && table.getTags() != null) {
            for (TableTag tag : preferences) {
                if (table.getTags().contains(tag)) {
                    score += 30;
                }
            }
        }
        return Math.max(0, score);
    }

    private List<Long> markRecommendedTables(List<RestaurantTable> allTables,
                                     Map<Long, TableStatus.Status> tableStatuses,
                                     Map<Long, Integer> tableScores,
                                     Integer partySize) {

        Optional<RestaurantTable> bestTable = allTables.stream()
            .filter(table -> tableStatuses.get(table.getId()) == TableStatus.Status.FREE)
            .filter(table -> table.getCapacity() >= partySize)
            .max(Comparator.comparing(table -> tableScores.get(table.getId())));

        if (bestTable.isPresent()) {
            tableStatuses.put(bestTable.get().getId(), TableStatus.Status.RECOMMENDED);
            return Collections.singletonList(bestTable.get().getId());
        }
        List<RestaurantTable> freeTables = allTables.stream()
            .filter(table -> tableStatuses.get(table.getId()) == TableStatus.Status.FREE)
            .sorted(Comparator.comparing(table -> -table.getCapacity()))
            .collect(Collectors.toList());
        List<RestaurantTable> bestGroup = null;

        int bestWaste = Integer.MAX_VALUE;
        for (int groupSize = 2; groupSize <= Math.min(4, freeTables.size()); groupSize++) {
            List<RestaurantTable> candidate = findBestGroup(freeTables, partySize, groupSize);
            if (candidate != null) {
                int totalCap = candidate.stream().mapToInt(RestaurantTable::getCapacity).sum();
                int waste = totalCap - partySize;
                if (waste < bestWaste) {
                    bestWaste = waste;
                    bestGroup = candidate;
                }
                break;
            }
        }
        if (bestGroup != null) {
            List<Long> groupIds = new ArrayList<>();
            for (RestaurantTable t : bestGroup) {
                tableStatuses.put(t.getId(), TableStatus.Status.RECOMMENDED);
                groupIds.add(t.getId());
            }
            return groupIds;
        }
        return null;
    }

    private List<RestaurantTable> findBestGroup(List<RestaurantTable> freeTables, int partySize, int groupSize) {
        if (groupSize == 2) {
            for (int i = 0; i < freeTables.size(); i++) {
                for (int j = i + 1; j < freeTables.size(); j++) {
                    RestaurantTable a = freeTables.get(i), b = freeTables.get(j);
                    if (a.getZone() != b.getZone()) continue;
                    if (a.getCapacity() + b.getCapacity() >= partySize && areAdjacent(a, b)) {
                        return Arrays.asList(a, b);
                    }
                }
            }
        }
        if (groupSize == 3) {
            for (int i = 0; i < freeTables.size(); i++) {
                for (int j = i + 1; j < freeTables.size(); j++) {
                    for (int k = j + 1; k < freeTables.size(); k++) {
                        RestaurantTable a = freeTables.get(i), b = freeTables.get(j), c = freeTables.get(k);
                        if (!sameZone(Arrays.asList(a, b, c))) continue;
                        int total = a.getCapacity() + b.getCapacity() + c.getCapacity();
                        if (total >= partySize && areGroupAdjacent(Arrays.asList(a, b, c))) {
                            return Arrays.asList(a, b, c);
                        }
                    }
                }
            }
        }
        if (groupSize == 4) {
            for (int i = 0; i < freeTables.size(); i++) {
                for (int j = i + 1; j < freeTables.size(); j++) {
                    for (int k = j + 1; k < freeTables.size(); k++) {
                        for (int l = k + 1; l < freeTables.size(); l++) {
                            RestaurantTable a = freeTables.get(i), b = freeTables.get(j), c = freeTables.get(k), d = freeTables.get(l);
                            if (!sameZone(Arrays.asList(a, b, c, d))) continue;
                            int total = a.getCapacity() + b.getCapacity() + c.getCapacity() + d.getCapacity();
                            if (total >= partySize && areGroupAdjacent(Arrays.asList(a, b, c, d))) {
                                return Arrays.asList(a, b, c, d);
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private boolean sameZone(List<RestaurantTable> group) {
        Zone zone = group.get(0).getZone();
        for (int i = 1; i < group.size(); i++) {
            if (group.get(i).getZone() != zone) return false;
        }
        return true;
    }

    private boolean areGroupAdjacent(List<RestaurantTable> group) {
        for (int i = 0; i < group.size(); i++) {
            boolean hasNeighbor = false;
            for (int j = 0; j < group.size(); j++) {
                if (i != j && areAdjacent(group.get(i), group.get(j))) {
                    hasNeighbor = true;
                    break;
                }
            }
            if (!hasNeighbor) return false;
        }
        return true;
    }

    private boolean areAdjacent(RestaurantTable t1, RestaurantTable t2) {
        double gapX = Math.max(0, Math.max(t1.getX() - (t2.getX() + t2.getWidth()), t2.getX() - (t1.getX() + t1.getWidth())));
        double gapY = Math.max(0, Math.max(t1.getY() - (t2.getY() + t2.getHeight()), t2.getY() - (t1.getY() + t1.getHeight())));
        double edgeDistance = Math.sqrt(gapX * gapX + gapY * gapY);
        return edgeDistance < 15;
    }

    public Booking createBooking(Long tableId, String guestName, Integer partySize,
                               LocalDate date, LocalTime startTime, LocalTime endTime, String groupId) {

        RestaurantTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found"));

        LocalTime resolvedEnd = endTime != null ? endTime : startTime.plusHours(2);
        WorkingHours wh = workingHoursRepository.findById(1L).orElse(null);
        if (wh != null) {
            if (startTime.isBefore(wh.getOpenTime())) {
                throw new RuntimeException("Restaurant opens at " + wh.getOpenTime() + ". Cannot book before opening time.");
            }
            if (startTime.isAfter(wh.getCloseTime()) || startTime.equals(wh.getCloseTime())) {
                throw new RuntimeException("Restaurant closes at " + wh.getCloseTime() + ". Cannot book at or after closing time.");
            }
            boolean endWraps = resolvedEnd.isBefore(startTime) || resolvedEnd.equals(LocalTime.MIDNIGHT);
            if (endWraps || resolvedEnd.isAfter(wh.getCloseTime())) {
                throw new RuntimeException("Reservation ends at " + resolvedEnd + " but restaurant closes at " + wh.getCloseTime() + ". Please choose an earlier time or shorter duration.");
            }
        }

        List<Booking> conflicts = bookingRepository.findConflictingBookings(tableId, date, startTime, resolvedEnd);
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("Table is not available at the requested time");
        }

        Booking booking = new Booking(tableId, guestName, partySize, date, startTime);
        booking.setEndTime(resolvedEnd);
        if (groupId != null) booking.setGroupId(groupId);
        return bookingRepository.save(booking);
    }

    public List<Booking> getUpcomingBookings() {
        return populateTableNames(bookingRepository.findUpcomingBookings(LocalDate.now()));
    }

    public List<Booking> getBookingsByDate(LocalDate date) {
        return populateTableNames(bookingRepository.findByDate(date));
    }

    public List<Booking> getAllBookings() {
        return populateTableNames(bookingRepository.findAll());
    }

    private List<Booking> populateTableNames(List<Booking> bookings) {
        Map<Long, String> nameMap = new HashMap<>();
        for (RestaurantTable t : tableRepository.findAll()) {
            nameMap.put(t.getId(), t.getName());
        }
        for (Booking b : bookings) {
            String name = nameMap.get(b.getTableId());
            b.setTableName(name != null ? name : "Unknown");
        }
        return bookings;
    }

    @jakarta.transaction.Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Booking not found"));

        if (booking.getGroupId() != null) {
            bookingRepository.deleteByGroupId(booking.getGroupId());
        } else {
            bookingRepository.deleteById(id);
        }
    }

    public RestaurantTable updateTablePosition(Long tableId, Double x, Double y, Zone zone) {
        RestaurantTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found"));

        table.setX(x);
        table.setY(y);
        if (zone != null) {
            table.setZone(zone);
        }
        return tableRepository.save(table);
    }

    public RestaurantTable updateTable(Long tableId, Integer capacity, Zone zone, Set<TableTag> tags) {
        RestaurantTable table = tableRepository.findById(tableId)
            .orElseThrow(() -> new RuntimeException("Table not found"));

        if (capacity != null) table.setCapacity(capacity);
        if (zone != null) table.setZone(zone);
        if (tags != null) table.setTags(tags);
        return tableRepository.save(table);
    }

    public RestaurantTable createTable(Integer capacity, Zone zone, Double x, Double y, Set<TableTag> tags) {
        String name = generateTableName();
        RestaurantTable table = new RestaurantTable(name, capacity, zone, x, y, tags);
        return tableRepository.save(table);
    }

    private String generateTableName() {
        List<RestaurantTable> allTables = tableRepository.findAll();
        Set<Integer> usedNumbers = new HashSet<>();
        for (RestaurantTable table : allTables) {
            String n = table.getName();
            if (n != null && n.startsWith("T")) {
                try {
                    usedNumbers.add(Integer.parseInt(n.substring(1)));
                } catch (NumberFormatException ignored) {}
            }
        }
        int num = 1;
        while (usedNumbers.contains(num)) {
            num++;
        }
        return "T" + num;
    }

    public void deleteTable(Long tableId) {
        if (!tableRepository.existsById(tableId)) {
            throw new RuntimeException("Table not found");
        }
        List<Booking> bookings = bookingRepository.findByTableId(tableId);
        bookingRepository.deleteAll(bookings);
        tableRepository.deleteById(tableId);
    }

    public int getMaxCapacity() {
        return tableRepository.findAll().stream()
            .mapToInt(RestaurantTable::getCapacity)
            .max()
            .orElse(12);
    }
}
