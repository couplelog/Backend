package com.Lubee.Lubee.couple.repository;

import com.Lubee.Lubee.couple.domain.Couple;
import com.Lubee.Lubee.user.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CoupleRepository extends JpaRepository<Couple, Long> {

    @Query("SELECT c FROM Couple c WHERE :user IN (SELECT u FROM c.user u)")
    Optional<Couple> findCoupleByUser(@Param("user") User user);

}
