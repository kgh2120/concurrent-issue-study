package com.example.concurrentissue.service;

import com.example.concurrentissue.domain.Stock;
import com.example.concurrentissue.repository.StockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class StockService {

    private final StockRepository stockRepository;



    @Transactional
    public void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값 저장
        Stock stock = stockRepository.findById(id)
                .orElseThrow();
        stock.decrease(quantity);

        stockRepository.saveAndFlush(stock);

    }


}
