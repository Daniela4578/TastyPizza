package objects;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Objects;

public class Shift {

    private final Long id;
    private final Long employeeId;
    private final String employeeName;
    private final LocalDate shiftDate;
    private final LocalTime startTime;
    private final LocalTime endTime;

    private Shift(Builder builder) {
        this.id = builder.id;
        this.employeeId = builder.employeeId;
        this.employeeName = builder.employeeName;
        this.shiftDate = builder.shiftDate;
        this.startTime = builder.startTime;
        this.endTime = builder.endTime;
    }

    public static Builder builder() {
        return new Builder();
    }

    public long getDurationHours() {
        return Duration.between(startTime, endTime).toHours();
    }

    public Long getId() {
        return id;
    }

    public Long getEmployeeId() {
        return employeeId;
    }

    public String getEmployeeName() {
        return employeeName;
    }

    public LocalDate getShiftDate() {
        return shiftDate;
    }

    public LocalTime getStartTime() {
        return startTime;
    }

    public LocalTime getEndTime() {
        return endTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Shift s)) return false;
        return Objects.equals(id, s.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Shift[%s | %s %s-%s | %dh]",
                employeeName, shiftDate, startTime, endTime, getDurationHours());
    }

    public static class Builder {
        private Long id;
        private Long employeeId;
        private String employeeName;
        private LocalDate shiftDate;
        private LocalTime startTime;
        private LocalTime endTime;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder employeeId(Long employeeId) {
            this.employeeId = employeeId;
            return this;
        }

        public Builder employeeName(String name) {
            this.employeeName = name;
            return this;
        }

        public Builder shiftDate(LocalDate date) {
            this.shiftDate = date;
            return this;
        }

        public Builder startTime(LocalTime startTime) {
            this.startTime = startTime;
            return this;
        }

        public Builder endTime(LocalTime endTime) {
            this.endTime = endTime;
            return this;
        }

        public Shift build() {
            Objects.requireNonNull(employeeId, "Employee ID is required");
            Objects.requireNonNull(shiftDate, "Shift date is required");
            Objects.requireNonNull(startTime, "Start time is required");
            Objects.requireNonNull(endTime, "End time is required");
            if (!endTime.isAfter(startTime)) {
                throw new IllegalArgumentException("End time must be after start time");
            }
            return new Shift(this);
        }
    }
}