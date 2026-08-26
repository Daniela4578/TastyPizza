package services;

import exceptions.UserNotFoundException;
import objects.*;
import repositories.interfaces.EmployeeDetailsRepository;
import repositories.interfaces.ShiftRepository;
import repositories.interfaces.UserRepository;
import services.interfaces.IEmployeeService;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class EmployeeService implements IEmployeeService {

    private final EmployeeDetailsRepository employeeDetailsRepository;
    private final ShiftRepository shiftRepository;
    private final UserRepository userRepository;

    public EmployeeService(EmployeeDetailsRepository employeeDetailsRepository,
                           ShiftRepository shiftRepository,
                           UserRepository userRepository) {
        this.employeeDetailsRepository = employeeDetailsRepository;
        this.shiftRepository = shiftRepository;
        this.userRepository = userRepository;
    }

    public List<User> getPendingEmployees() {
        return userRepository.findByStatus(AccountStatus.PENDING);
    }

    public List<User> getActiveEmployees() {
        return userRepository.findByStatus(AccountStatus.ACTIVE).stream()
                .filter(u -> u.getRole() == Role.EMPLOYEE)
                .collect(Collectors.toList());
    }

    public Optional<User> findUser(Long userId) {
        return userRepository.findById(userId);
    }

    public EmployeeDetails approveEmployee(Long userId, BigDecimal salary, LocalDate hireDate) {
        if (salary == null || salary.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Salary must be zero or greater");

        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User not found: " + userId));

        if (user.getRole() != Role.EMPLOYEE)
            throw new IllegalArgumentException("User is not an employee");
        if (user.getStatus() != AccountStatus.PENDING)
            throw new IllegalArgumentException("Employee is not in PENDING status");

        userRepository.updateStatus(userId, AccountStatus.ACTIVE);

        return employeeDetailsRepository.save(EmployeeDetails.builder()
                .userId(userId).salary(salary).hireDate(hireDate).build());
    }

    public void fireEmployee(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() ->
                new UserNotFoundException("User not found: " + userId));
        if (user.getRole() != Role.EMPLOYEE)
            throw new IllegalArgumentException("User is not an employee");
        userRepository.updateStatus(userId, AccountStatus.FIRED);
    }

    public Optional<EmployeeDetails> getEmployeeDetails(Long userId) {
        return employeeDetailsRepository.findByUserId(userId);
    }

    public List<EmployeeDetails> getAllEmployeeDetails() {
        return employeeDetailsRepository.findAll();
    }

    public void updateSalary(Long userId, BigDecimal newSalary) {
        if (newSalary == null || newSalary.compareTo(BigDecimal.ZERO) < 0)
            throw new IllegalArgumentException("Salary must be zero or greater");
        employeeDetailsRepository.findByUserId(userId).orElseThrow(() ->
                new UserNotFoundException("No employee details found for user: " + userId));
        employeeDetailsRepository.updateSalary(userId, newSalary);
    }


    public Shift assignShift(Long employeeId, String employeeName,
                             LocalDate shiftDate, LocalTime startTime, LocalTime endTime) {
        if (shiftDate.isBefore(LocalDate.now()))
            throw new IllegalArgumentException("Cannot assign a shift in the past");
        return shiftRepository.save(Shift.builder()
                .employeeId(employeeId).employeeName(employeeName)
                .shiftDate(shiftDate).startTime(startTime).endTime(endTime).build());
    }

    public List<Shift> getShiftsForEmployee(Long employeeId) {
        return shiftRepository.findByEmployeeId(employeeId);
    }

    public List<Shift> getTodaysShifts() {
        return shiftRepository.findByDate(LocalDate.now());
    }

    public void removeShift(Long shiftId) {
        shiftRepository.deleteById(shiftId);
    }
}