package com.restaurant.model;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@Table(name = "booking")
public class Booking {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long tableId;
    
    @Column(nullable = false)
    private String guestName;
    
    @Column(nullable = false)
    private Integer partySize;
    
    @Column(nullable = false)
    private LocalDate date;
    
    @Column(nullable = false)
    private LocalTime startTime;
    
    @Column(nullable = false)
    private LocalTime endTime;
    
    @Column
    private String groupId;

    @Transient
    private String tableName;

    public Booking() {}

    public Booking(Long tableId, String guestName, Integer partySize, LocalDate date, LocalTime startTime) {
        this.tableId = tableId;
        this.guestName = guestName;
        this.partySize = partySize;
        this.date = date;
        this.startTime = startTime;
        this.endTime = startTime.plusHours(2);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getTableId() { return tableId; }
    public void setTableId(Long tableId) { this.tableId = tableId; }

    public String getGuestName() { return guestName; }
    public void setGuestName(String guestName) { this.guestName = guestName; }

    public Integer getPartySize() { return partySize; }
    public void setPartySize(Integer partySize) { this.partySize = partySize; }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { 
        this.startTime = startTime;
        this.endTime = startTime.plusHours(2);
    }

    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }
    
    public String getGroupId() { return groupId; }
    public void setGroupId(String groupId) { this.groupId = groupId; }
    
    public String getTableName() { return tableName; }
    public void setTableName(String tableName) { this.tableName = tableName; }
}
