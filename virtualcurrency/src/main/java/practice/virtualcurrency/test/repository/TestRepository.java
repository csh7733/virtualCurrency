package practice.virtualcurrency.test.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import practice.virtualcurrency.test.domain.TestItem;

public interface TestRepository extends JpaRepository<TestItem,Long> {
}
