package org.arabius.platform.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

import ai.timefold.solver.core.api.domain.lookup.PlanningId;

//@JsonIdentityInfo(scope = Timeslot.class, generator = ObjectIdGenerators.PropertyGenerator.class, property = "id")
public class Timeslot extends ArabiusEntity {

    @PlanningId
    private int id;

    private LocalDate dayOfWeek;
    private LocalTime startTime;
    private LocalTime endTime;

    public Timeslot() {
    }

    public Timeslot(int id, LocalDate dayOfWeek, LocalTime startTime, LocalTime endTime) {
        this.id = id;
        this.dayOfWeek = dayOfWeek;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Timeslot(int id, LocalDate dayOfWeek, LocalTime startTime) {
        this(id, dayOfWeek, startTime, startTime.plusMinutes(50));
    }

    @Override
    public String toString() {
        return dayOfWeek + " " + startTime;
    }

    // ************************************************************************
    // Getters and setters
    // ************************************************************************

    public int getId() {
        return id;
    }

    public LocalDate getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setDayOfWeek(LocalDate dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
    }

    public void setStartTime(LocalTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(LocalTime endTime) {
        this.endTime = endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Timeslot timeslot = (Timeslot) o;
        return Objects.equals(getIdAndDate(), timeslot.getIdAndDate());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIdAndDate());
    }
    
    private String getIdAndDate() {
        return id + "-" + dayOfWeek.toString();
    }
}
