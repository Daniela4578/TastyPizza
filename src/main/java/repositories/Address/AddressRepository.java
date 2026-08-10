package repositories.Address;

import objects.Address;
import java.util.List;
import java.util.Optional;

public interface AddressRepository {
    Address save(Address address);
    List<Address> findByUserId(Long userId);
    Optional<Address> findById(Long id);
    void deleteById(Long id);
}