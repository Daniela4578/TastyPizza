package services;

import exceptions.AccountNotActiveException;
import exceptions.EmailAlreadyExistsException;
import exceptions.UserNotFoundException;
import objects.AccountStatus;
import objects.Role;
import objects.User;
import repositories.interfaces.UserRepository;
import services.interfaces.IUserService;
import utilitis.ArgumentUtils;
import utilitis.PasswordHasher;

import java.time.LocalDate;
import java.time.Period;
import java.util.Optional;
import java.util.regex.Pattern;

public class UserService implements IUserService {

    private final UserRepository userRepository;

    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PHONE_PATTERN = Pattern.compile(
            "^\\+?[0-9]{10}$");

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public User register(String email, String password, String firstName,
                         String lastName, String phoneNumber,
                         LocalDate dateOfBirth, Role role) {

        email = email.toLowerCase().trim();
        validateEmail(email);
        validatePassword(password);
        validateName(firstName);
        validateName(lastName);
        validatePhoneNumber(phoneNumber);
        validateAge(role, dateOfBirth);

        if (userRepository.findByEmail(email).isPresent()) {
            throw new EmailAlreadyExistsException("An account with this email already exists");
        }

        String passwordHash = PasswordHasher.hash(password);
        AccountStatus status = (role == Role.EMPLOYEE)
                ? AccountStatus.PENDING
                : AccountStatus.ACTIVE;

        User newUser = User.builder()
                .email(email).passwordHash(passwordHash)
                .firstName(firstName).lastName(lastName)
                .phoneNumber(phoneNumber).role(role)
                .status(status).dateOfBirth(dateOfBirth)
                .build();

        return userRepository.save(newUser);
    }

    @Override
    public User login(String email, String password) {
        email = email.toLowerCase().trim();
        validateEmail(email);
        validatePassword(password);

        Optional<User> result = userRepository.findByEmail(email);
        if (result.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = result.get();

        switch (user.getStatus()) {
            case PENDING -> throw new AccountNotActiveException("Your account is pending manager approval.");
            case DEACTIVATED -> throw new AccountNotActiveException("This account has been deactivated.");
            case FIRED -> throw new AccountNotActiveException("Your employment has been terminated.");
            default -> {
            }
        }

        if (!PasswordHasher.verify(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        return user;
    }

    @Override
    public void deactivateAccount(Long userId) {
        userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User not found: " + userId));
        userRepository.updateStatus(userId, AccountStatus.DEACTIVATED);
    }

    @Override
    public void validateEmail(String email) {
        ArgumentUtils.requireNonBlank(email, "Email");
        if (!EMAIL_PATTERN.matcher(email).matches())
            throw new IllegalArgumentException("Invalid email format");
    }

    @Override
    public void validatePassword(String password) {
        ArgumentUtils.requireNonBlank(password, "Password");
        if (password.length() < 6)
            throw new IllegalArgumentException("Password must be at least 6 characters");
    }

    @Override
    public void validateName(String name) {
        ArgumentUtils.requireNonBlank(name, "Name");
        if (name.length() < 3)
            throw new IllegalArgumentException("Name must be at least 3 characters");
    }

    @Override
    public void validatePhoneNumber(String phone) {
        ArgumentUtils.requireNonBlank(phone, "Phone number");
        if (!PHONE_PATTERN.matcher(phone).matches())
            throw new IllegalArgumentException("Invalid phone number");
    }

    @Override
    public void validateAge(Role role, LocalDate dateOfBirth) {
        ArgumentUtils.requireNonNull(dateOfBirth, "Date of birth");
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();
        if (age < 16 || age > 80)
            throw new IllegalArgumentException("User must be between 16 and 80 years old");
        if (role == Role.EMPLOYEE && (age < 18 || age > 65))
            throw new IllegalArgumentException("Employee must be between 18 and 65 years old");
    }
}