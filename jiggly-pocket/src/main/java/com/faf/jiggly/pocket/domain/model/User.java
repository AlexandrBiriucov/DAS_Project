package com.faf.jiggly.pocket.domain.model;

import java.util.Objects;
// import java.util.UUID;
// import lombok.Builder;

// @Builder
public record User(UserId id,String email,String passwordHash){

    
    public User{
        Objects.requireNonNull(id,"id is null");
        Objects.requireNonNull(email, "email is null");
        Objects.requireNonNull(passwordHash, " passwordHash is null");

    }


}