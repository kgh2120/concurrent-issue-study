package com.example.concurrentissue.domain;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Version;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Entity
public class Stock {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;

    private Long quantity;

    @Version // javax.persistance 로 이용
    private Long version;
    public Stock(Long productId, Long quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public Long getQuantity() {
        return quantity;
    }

    public void decrease(Long quantity){
        if(this.quantity - quantity <0) throw new IllegalArgumentException("재고는 0개 미만이 될 수 없습니다.");
        this.quantity -= quantity;
    }
}
