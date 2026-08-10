package repositories.User;

import objects.AccountStatus;
import objects.User;

import java.util.List;
import java.util.Optional;

public interface UserRepository {

    User save(User user);
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    List<User> findByStatus(AccountStatus status);
    void updateStatus(Long userId, AccountStatus status);


}
