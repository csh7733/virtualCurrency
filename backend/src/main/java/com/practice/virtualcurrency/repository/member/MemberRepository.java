package com.practice.virtualcurrency.repository.member;

import com.practice.virtualcurrency.domain.member.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByEmailAndPassword(String email, String password);
    Optional<Member> findByUsername(String username);
    Optional<Member> findByEmail(String email);
    @Query("SELECT m FROM Member m LEFT JOIN FETCH m.orders WHERE m.username = :username")
    Optional<Member> findByUsernameWithOrders(@Param("username") String username);
}
