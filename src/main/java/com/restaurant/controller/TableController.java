package com.restaurant.controller;
import com.restaurant.model.*;
import com.restaurant.service.BookingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@RestController
@RequestMapping("/api/tables")
public class TableController {

    @Autowired
    private BookingService bookingService;

    @GetMapping
    public List<TableStatus> getTables(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.TIME) LocalTime time,
            @RequestParam(required = false) Integer partySize,
            @RequestParam(required = false) Zone zone,
            @RequestParam(required = false) Set<TableTag> prefs) {
        
        LocalDate queryDate = date != null ? date : LocalDate.now();
        LocalTime queryTime = time != null ? time : LocalTime.now();
        
        return bookingService.getTablesWithStatus(queryDate, queryTime, partySize, zone, prefs);
    }
    
    @GetMapping("/max-capacity")
    public int getMaxCapacity() {
        return bookingService.getMaxCapacity();
    }
    
    @PutMapping("/{id}/position")
    public RestaurantTable updateTablePosition(@PathVariable Long id, @RequestBody PositionUpdate position) {
        return bookingService.updateTablePosition(id, position.getX(), position.getY(), position.getZone());
    }
    
    @PutMapping("/{id}")
    public RestaurantTable updateTable(@PathVariable Long id, @RequestBody TableUpdate update) {
        return bookingService.updateTable(id, update.getCapacity(), update.getZone(), update.getTags());
    }
    
    @PostMapping
    public RestaurantTable createTable(@RequestBody TableUpdate update) {
        return bookingService.createTable(
            update.getCapacity(), update.getZone(),
            update.getX() != null ? update.getX() : 40.0,
            update.getY() != null ? update.getY() : 30.0,
            update.getTags()
        );
    }
    
    @DeleteMapping("/{id}")
    public void deleteTable(@PathVariable Long id) {
        bookingService.deleteTable(id);
    }
    
    public static class PositionUpdate {
        private Double x;
        private Double y;
        private Zone zone;
        
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }
        
        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }

        public Zone getZone() { return zone; }
        public void setZone(Zone zone) { this.zone = zone; }
    }
    
    public static class TableUpdate {
        private String name;
        private Integer capacity;
        private Zone zone;
        private Set<TableTag> tags;
        private Double x;
        private Double y;
        
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        
        public Integer getCapacity() { return capacity; }
        public void setCapacity(Integer capacity) { this.capacity = capacity; }
        
        public Zone getZone() { return zone; }
        public void setZone(Zone zone) { this.zone = zone; }
        
        public Set<TableTag> getTags() { return tags; }
        public void setTags(Set<TableTag> tags) { this.tags = tags; }
        
        public Double getX() { return x; }
        public void setX(Double x) { this.x = x; }
        
        public Double getY() { return y; }
        public void setY(Double y) { this.y = y; }
    }
}
