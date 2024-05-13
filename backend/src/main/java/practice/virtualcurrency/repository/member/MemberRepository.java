package practice.virtualcurrency.repository.member;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.virtualcurrency.domain.member.Member;

import java.util.Optional;

public interface MemberRepository extends JpaRepository<Member,Long> {
    Optional<Member> findByUsernameAndPassword(String username, String password);
}
