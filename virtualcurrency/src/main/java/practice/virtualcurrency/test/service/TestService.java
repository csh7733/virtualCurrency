package practice.virtualcurrency.test.service;

import practice.virtualcurrency.test.domain.TestItem;

import java.util.List;
import java.util.Optional;

public interface TestService {
    TestItem save(TestItem testItem);

    Optional<TestItem> findById(Long id);

    List<TestItem> findAll();

    void delteAll();
}
