package practice.virtualcurrency.domain.order;

import jakarta.persistence.*;
import lombok.Data;
import practice.virtualcurrency.domain.member.Member;

@Data
@Entity
@Table(name = "orders")
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String coinName;
    private Double price;
    private Double quantity;
    private State state;
    private Double leverage;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    public Order(String coinName, Double price, Double quantity, State state, Member member,Double leverage) {
        this.coinName = coinName;
        this.price = price;
        this.quantity = quantity;
        this.state = state;
        this.member = member;
        this.leverage = leverage;
    }

    public Order() {

    }

    public Order(String coinName) {
        this.coinName = coinName;
        price = quantity = 0.0;
        leverage = 1.0;
    }

    @Override
    public String toString() {
        return "Order{" +
                "coinName='" + coinName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", total=" + price*quantity +
                ", state=" + state +
                ", member=" + member.getUsername() +
                ", leverage=" + leverage +
                '}';
    }
}
