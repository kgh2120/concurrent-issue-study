package com.example.concurrentissue.service;

import com.example.concurrentissue.domain.Stock;
import com.example.concurrentissue.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
//@Transactional(readOnly = true)
@Service
public class StockService {

    private final StockRepository stockRepository;


    /*
        synchronized의 문제점 : synchronized는 각 프로세스에서만 동시성 문제를 보장한다.
        하지만 서버가 여러 대라면, synchronized키워드는 동시성 문제를 보장할 수 없다.
     */
//    @Transactional(isolation = Isolation.SERIALIZABLE)
    public synchronized void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값 저장
        Stock stock = stockRepository.findById(id)
                .orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);

    }


}
