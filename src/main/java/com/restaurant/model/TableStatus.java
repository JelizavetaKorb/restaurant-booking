package com.restaurant.model;
import java.util.List;
import java.util.Set;

public class TableStatus {
    public enum Status {
        FREE, BOOKED, RECOMMENDED
    }
    
    private Long id;
    private String name;
    private Integer capacity;
    private Zone zone;
    private Double x;
    private Double y;
    private Set<TableTag> tags;
    private Double width;
    private Double height;
    private Status status;
    private Integer score;
    private List<Long> recommendedWith;
    
    public TableStatus() {}
    
    public TableStatus(RestaurantTable table, Status status, Integer score) {
        this.id = table.getId();
        this.name = table.getName();
        this.capacity = table.getCapacity();
        this.zone = table.getZone();
        this.x = table.getX();
        this.y = table.getY();
        this.tags = table.getTags();
        this.width = table.getWidth();
        this.height = table.getHeight();
        this.status = status;
        this.score = score;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public Integer getCapacity() { return capacity; }
    public void setCapacity(Integer capacity) { this.capacity = capacity; }

    public Zone getZone() { return zone; }
    public void setZone(Zone zone) { this.zone = zone; }

    public Double getX() { return x; }
    public void setX(Double x) { this.x = x; }

    public Double getY() { return y; }
    public void setY(Double y) { this.y = y; }

    public Set<TableTag> getTags() { return tags; }
    public void setTags(Set<TableTag> tags) { this.tags = tags; }

    public Double getWidth() { return width; }
    public void setWidth(Double width) { this.width = width; }

    public Double getHeight() { return height; }
    public void setHeight(Double height) { this.height = height; }

    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }

    public Integer getScore() { return score; }
    public void setScore(Integer score) { this.score = score; }

    public List<Long> getRecommendedWith() { return recommendedWith; }
    public void setRecommendedWith(List<Long> recommendedWith) { this.recommendedWith = recommendedWith; }
}
