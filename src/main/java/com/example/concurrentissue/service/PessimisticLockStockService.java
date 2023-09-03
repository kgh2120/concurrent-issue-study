package com.example.concurrentissue.service;


import com.example.concurrentissue.domain.Stock;
import com.example.concurrentissue.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
//@Transactional(readOnly = true)
@Service
public class PessimisticLockStockService {

    private final StockRepository stockRepository;


    /*
        충돌이 빈번하게 발생하면 낙관락보다 성능이 좋을 수 있다.
        LOCK을 통해 업데이트를 제어해서 데이터 정합성이 보장된다.
        별도의 LOCK을 이용해 성능 감소가 예상된다.
     */
    @Transactional
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값 저장
        Stock stock = stockRepository.findByIdWithPessimisticLock(id);
        stock.decrease(quantity);
        stockRepository.save(stock);
    }
}
