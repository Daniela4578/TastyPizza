package objects;

import java.time.LocalDate;
import java.time.Period;
import java.util.Objects;

public class User {

    private final Long id;
    private final String email;
    private final String passwordHash;
    private final String firstName;
    private final String lastName;
    private final String phoneNumber;
    private final Role role;
    private final AccountStatus status;
    private final LocalDate dateOfBirth;

    private User(Builder builder) {
        this.id = builder.id;
        this.email = builder.email;
        this.passwordHash = builder.passwordHash;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.phoneNumber = builder.phoneNumber;
        this.role = builder.role;
        this.status = builder.status;
        this.dateOfBirth = builder.dateOfBirth;
    }

    public static Builder builder() {
        return new Builder();
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public Role getRole() {
        return role;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public AccountStatus getStatus() {
        return status;
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }

    public int getAge() {
        return Period.between(this.dateOfBirth, LocalDate.now()).getYears();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof User user)) return false;
        return Objects.equals(email, user.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }


    @Override
    public String toString() {
        return String.format("User[id: %d, email: '%s', name: '%s', role: %s, status: %s, age: %s]",
                id, email, getFullName(), role, status,
                dateOfBirth != null ? String.valueOf(getAge()) : "unknown");
    }

    public static class Builder {
        private Long id;
        private String email;
        private String passwordHash;
        private String firstName;
        private String lastName;
        private String phoneNumber;
        private Role role;
        private AccountStatus status = AccountStatus.ACTIVE;
        private LocalDate dateOfBirth;

        public Builder id(Long id) {
            this.id = id;
            return this;
        }

        public Builder email(String email) {
            this.email = email;
            return this;
        }

        public Builder passwordHash(String hash) {
            this.passwordHash = hash;
            return this;
        }

        public Builder firstName(String firstName) {
            this.firstName = firstName;
            return this;
        }

        public Builder lastName(String lastName) {
            this.lastName = lastName;
            return this;
        }

        public Builder phoneNumber(String phone) {
            this.phoneNumber = phone;
            return this;
        }

        public Builder role(Role role) {
            this.role = role;
            return this;
        }

        public Builder status(AccountStatus status) {
            this.status = status;
            return this;
        }

        public Builder dateOfBirth(LocalDate dob) {
            this.dateOfBirth = dob;
            return this;
        }

        public User build() {
            Objects.requireNonNull(email, "Email is required");
            Objects.requireNonNull(passwordHash, "Password is required");
            Objects.requireNonNull(firstName, "First name is required");
            Objects.requireNonNull(lastName, "Last name is required");
            Objects.requireNonNull(role, "Role is required");
            Objects.requireNonNull(dateOfBirth, "Date of birth is required");
            return new User(this);
        }
    }
}