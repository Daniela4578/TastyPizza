package objects;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;

public class EmployeeDetails {

    private final Long userId;
    private final BigDecimal salary;
    private final LocalDate hireDate;

    private EmployeeDetails(Builder builder) {
        this.userId = builder.userId;
        this.salary = builder.salary;
        this.hireDate = builder.hireDate;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getUserId() {
        return userId;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public LocalDate getHireDate() {
        return hireDate;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof EmployeeDetails e)) return false;
        return Objects.equals(userId, e.userId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId);
    }

    @Override
    public String toString() {
        return String.format("EmployeeDetails[userId: %d, salary: %.2f, hireDate: %s]",
                userId, salary, hireDate);
    }

    public static class Builder {
        private Long userId;
        private BigDecimal salary;
        private LocalDate hireDate;

        public Builder userId(Long userId) {
            this.userId = userId;
            return this;
        }

        public Builder salary(BigDecimal salary) {
            this.salary = salary;
            return this;
        }

        public Builder hireDate(LocalDate date) {
            this.hireDate = date;
            return this;
        }

        public EmployeeDetails build() {
            Objects.requireNonNull(userId, "User ID is required");
            Objects.requireNonNull(salary, "Salary is required");
            Objects.requireNonNull(hireDate, "Hire date is required");
            return new EmployeeDetails(this);
        }
    }
}