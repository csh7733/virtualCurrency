package practice.virtualcurrency.test.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Data;

@Data
@Entity
public class TestItem {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String itemName;
    private Long price;

    public TestItem() {
    }

    public TestItem(String itemName, Long price) {
        this.itemName = itemName;
        this.price = price;
    }
}
