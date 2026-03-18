package com.restaurant.model;
import jakarta.persistence.*;
import java.util.Set;

@Entity
@Table(name = "restaurant_table")
public class RestaurantTable {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private Integer capacity;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Zone zone;
    
    @Column(nullable = false)
    private Double x;
    
    @Column(nullable = false)
    private Double y;
    
    @ElementCollection(targetClass = TableTag.class)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "table_tags", joinColumns = @JoinColumn(name = "table_id"))
    @Column(name = "tag")
    private Set<TableTag> tags;
    
    @Column(nullable = false)
    private Double width = 8.0;
    
    @Column(nullable = false)
    private Double height = 6.0;

    public RestaurantTable() {}

    public RestaurantTable(String name, Integer capacity, Zone zone, Double x, Double y, Set<TableTag> tags) {
        this.name = name;
        this.capacity = capacity;
        this.zone = zone;
        this.x = x;
        this.y = y;
        this.tags = tags;
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
}
