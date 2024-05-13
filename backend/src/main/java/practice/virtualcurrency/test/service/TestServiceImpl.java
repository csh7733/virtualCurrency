package practice.virtualcurrency.test.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import practice.virtualcurrency.test.domain.TestItem;
import practice.virtualcurrency.test.repository.TestRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TestServiceImpl implements TestService {
    private final TestRepository testRepository;
    @Override
    public TestItem save(TestItem testItem) {
        return testRepository.save(testItem);
    }

    @Override
    public Optional<TestItem> findById(Long id) {
        return testRepository.findById(id);
    }

    @Override
    public List<TestItem> findAll() {
        return testRepository.findAll();
    }

    @Override
    public void delteAll() {
        testRepository.deleteAll();
    }
}
