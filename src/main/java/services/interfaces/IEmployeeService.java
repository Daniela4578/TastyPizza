package services.interfaces;

import objects.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface IEmployeeService {
    List<User> getPendingEmployees();

    List<User> getActiveEmployees();

    Optional<User> findUser(Long userId);

    EmployeeDetails approveEmployee(Long userId, BigDecimal salary, LocalDate hireDate);

    void fireEmployee(Long userId);

    Optional<EmployeeDetails> getEmployeeDetails(Long userId);

    List<EmployeeDetails> getAllEmployeeDetails();

    void updateSalary(Long userId, BigDecimal newSalary);

    Shift assignShift(Long employeeId, String employeeName,
                      LocalDate shiftDate, LocalTime startTime, LocalTime endTime);

    List<Shift> getShiftsForEmployee(Long employeeId);

    List<Shift> getTodaysShifts();

    void removeShift(Long shiftId);
}