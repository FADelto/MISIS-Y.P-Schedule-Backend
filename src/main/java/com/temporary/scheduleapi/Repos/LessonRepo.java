package com.temporary.scheduleapi.Repos;

import com.temporary.scheduleapi.models.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface LessonRepo extends JpaRepository<Lesson, Long> {
    @Query(value = "SELECT * FROM lesson WHERE day_of_year>:firstDate AND day_of_year<:secondDate", nativeQuery = true)
    List<Lesson> getLessonsFromDateToDate(@Param("firstDate") int firstDate, @Param("secondDate") int secondDate);

    @Query(value = "SELECT DISTINCT day_of_year FROM lesson", nativeQuery = true)
    List<Integer> getDaysOfYear();
}