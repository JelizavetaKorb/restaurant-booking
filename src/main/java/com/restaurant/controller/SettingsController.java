package com.restaurant.controller;
import com.restaurant.model.WorkingHours;
import com.restaurant.repository.WorkingHoursRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.time.LocalTime;

@RestController
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private WorkingHoursRepository workingHoursRepository;

    @GetMapping("/working-hours")
    public WorkingHours getWorkingHours() {
        return workingHoursRepository.findById(1L)
                .orElse(new WorkingHours(LocalTime.of(10, 0), LocalTime.of(22, 0)));
    }

    @PutMapping("/working-hours")
    public WorkingHours updateWorkingHours(@RequestBody WorkingHours hours) {
        hours.setId(1L);
        return workingHoursRepository.save(hours);
    }
}

