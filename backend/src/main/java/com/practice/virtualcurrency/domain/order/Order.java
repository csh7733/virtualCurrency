package com.practice.virtualcurrency.domain.order;

import com.practice.virtualcurrency.domain.member.Member;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Entity
@Table(name = "orders")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String coinName;
    private String time;
    private OrderType orderType;

    @Builder.Default
    private Double price = 0.0;

    @Builder.Default
    private Double quantity = 0.0;

    private State state;

    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member member;

    @Builder.Default
    private Double leverage = 1.0;

    @Override
    public String toString() {
        return "Order{" +
                "coinName='" + coinName + '\'' +
                ", price=" + price +
                ", quantity=" + quantity +
                ", total=" + price * quantity +
                ", state=" + state +
                ", member=" + (member != null ? member.getUsername() : "null") +
                ", leverage=" + leverage +
                '}';
    }
}
