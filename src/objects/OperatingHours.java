package objects;

import java.time.LocalTime;
import java.util.Objects;

public class OperatingHours {

    private static final String[] DAY_NAMES = {
            "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"
    };

    private final Long id;
    private final int dayOfWeek;
    private final LocalTime openTime;
    private final LocalTime closeTime;
    private final boolean isClosed;

    private OperatingHours(Builder builder) {
        this.id = builder.id;
        this.dayOfWeek = builder.dayOfWeek;
        this.openTime = builder.openTime;
        this.closeTime = builder.closeTime;
        this.isClosed = builder.isClosed;
    }

    public static Builder builder() {
        return new Builder();
    }

    public boolean isOpenNow(LocalTime time) {
        if (isClosed) return false;
        return !time.isBefore(openTime) && !time.isAfter(closeTime);
    }

    public String getDayName() {
        return DAY_NAMES[dayOfWeek];
    }

    public Long getId() {
        return id;
    }

    public int getDayOfWeek() {
        return dayOfWeek;
    }

    public LocalTime getOpenTime() {
        return openTime;
    }

    public LocalTime getCloseTime() {
        return closeTime;
    }

    public boolean isClosed() {
        return isClosed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof OperatingHours h)) return false;
        return dayOfWeek == h.dayOfWeek;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dayOfWeek);
    }

    @Override
    public String toString() {
        if (isClosed) return String.format("%s: CLOSED", getDayName());
        return String.format("%s: %s - %s", getDayName(), openTime, closeTime);
    }

    public static class Builder {
        private Long id;
        private int dayOfWeek;
        private LocalTime openTime;
        private LocalTime closeTime;
        private boolean isClosed = false;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder dayOfWeek(int day) {
            this.dayOfWeek = day;
            return this;
        }

        public Builder openTime(LocalTime open) {
            this.openTime = open;
            return this;
        }

        public Builder closeTime(LocalTime close) {
            this.closeTime = close;
            return this;
        }

        public Builder isClosed(boolean closed) {
            this.isClosed = closed;
            return this;
        }

        public OperatingHours build() {
            if (dayOfWeek < 0 || dayOfWeek > 6) {
                throw new IllegalArgumentException("Day of week must be between 0 (Monday) and 6 (Sunday)");
            }
            if (!isClosed) {
                Objects.requireNonNull(openTime, "Open time is required when not closed");
                Objects.requireNonNull(closeTime, "Close time is required when not closed");
            }
            return new OperatingHours(this);
        }
    }
}