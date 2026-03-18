package com.restaurant.repository;
import com.restaurant.model.RestaurantTable;
import com.restaurant.model.Zone;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TableRepository extends JpaRepository<RestaurantTable, Long> {
    
    List<RestaurantTable> findByZone(Zone zone);
    List<RestaurantTable> findByCapacityGreaterThanEqual(Integer minCapacity);
    
    @Query("SELECT t FROM RestaurantTable t WHERE t.capacity >= :minCapacity AND (:zone IS NULL OR t.zone = :zone)")
    List<RestaurantTable> findByCapacityAndZone(@Param("minCapacity") Integer minCapacity, @Param("zone") Zone zone);
}
