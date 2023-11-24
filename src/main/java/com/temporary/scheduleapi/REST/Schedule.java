package com.temporary.scheduleapi.REST;

import com.temporary.scheduleapi.Repos.LessonRepo;
import com.temporary.scheduleapi.ScheduleParse;
import com.temporary.scheduleapi.models.Lesson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.List;

@RestController
@RequestMapping("/api/Schedule")
public class Schedule {
    @Autowired
    private LessonRepo lessonRepo;

    @GetMapping("/availability")
    public List<Integer> getAvailability() {
        return lessonRepo.getDaysOfYear();
    }

    @GetMapping
    public List<Lesson> getSchedule(@RequestParam int firstDate, @RequestParam int secondDate) {
        return lessonRepo.getLessonsFromDateToDate(firstDate, secondDate);
    }

    @PostMapping
    public void firstParseSchedule(@RequestBody String password) throws GeneralSecurityException, IOException {
        ScheduleParse schedule = new ScheduleParse(lessonRepo);
        if (password.equals("19021995")) {
            schedule.saveSchedule("1NlUU1ulotC5Kjiz-ctVhzwyAcyEWbK2ZY5eA4Z2PKoQ");
        }
    }
//    @GetMapping
//    public List<AvailabilityOfLessons> getAvailability(){
//        return
//    }
}
