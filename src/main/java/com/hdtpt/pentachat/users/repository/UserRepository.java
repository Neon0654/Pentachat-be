package com.hdtpt.pentachat.users.repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.hdtpt.pentachat.users.model.User;

<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findByUsername(String username);
<<<<<<< HEAD
    List<User> findByUsernameContainingIgnoreCase(String username);
}


=======
}

>>>>>>> c99ecabafa9ac82c979d4fa63bf5d7254224336b
