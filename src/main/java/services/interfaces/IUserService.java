package services.interfaces;

import objects.Role;
import objects.User;

import java.time.LocalDate;

public interface IUserService {
    User register(String email, String password, String firstName,
                  String lastName, String phoneNumber, LocalDate dateOfBirth, Role role);

    User login(String email, String password);

    void deactivateAccount(Long userId);

    void validateEmail(String email);

    void validatePassword(String password);

    void validateName(String name);

    void validatePhoneNumber(String phone);

    void validateAge(Role role, LocalDate dateOfBirth);
}