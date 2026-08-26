package repositories.interfaces;

import objects.Shift;

import java.time.LocalDate;
import java.util.List;

public interface ShiftRepository {
    Shift save(Shift shift);

    List<Shift> findByEmployeeId(Long employeeId);

    List<Shift> findByDate(LocalDate date);

    void deleteById(Long shiftId);
}