package com.faf.jiggly.pocket.domain.port;
import com.faf.jiggly.pocket.domain.model.User;
// import com.faf.jiggly.pocket.domain.model.UserId;
import java.util.Optional;



public interface UserRepository {


    User save(User user);
    Optional <User> findByEmail(String email);


}


