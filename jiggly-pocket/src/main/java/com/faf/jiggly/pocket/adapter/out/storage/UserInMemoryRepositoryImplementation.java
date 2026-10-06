package com.faf.jiggly.pocket.adapter.out.storage;

import com.faf.jiggly.pocket.domain.model.User;
import com.faf.jiggly.pocket.domain.port.UserRepository;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;






@Repository
public class UserInMemoryRepositoryImplementation implements UserRepository{

            private final Map<String, User> usersByEmail = new ConcurrentHashMap<>();


        @Override 
       public User save(User user){
            usersByEmail.put(user.email(), user);
            return user;
        }

            @Override 
          public  Optional <User> findByEmail(String email){
            return Optional.ofNullable(usersByEmail.get(email));



            }






}
