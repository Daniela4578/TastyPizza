package repositories.interfaces;

import objects.EmployeeDetails;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface EmployeeDetailsRepository {
    EmployeeDetails save(EmployeeDetails employee);

    Optional<EmployeeDetails> findByUserId(Long userId);

    List<EmployeeDetails> findAll();

    void updateSalary(Long userId, BigDecimal newSalary);
}
