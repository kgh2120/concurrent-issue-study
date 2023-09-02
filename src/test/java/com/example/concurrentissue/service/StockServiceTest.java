package com.example.concurrentissue.service;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

import com.example.concurrentissue.domain.Stock;
import com.example.concurrentissue.repository.StockRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class StockServiceTest {

    @Autowired
    private StockService stockService;
    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    void beforeEach(){
        stockRepository.saveAndFlush(new Stock(1L, 100L));
    }
    @AfterEach
    void afterEach(){
        stockRepository.deleteAll();
    }
    
    @Test
    void 재고감소 () throws Exception{
        //given
        stockService.decrease(1L,1L);
        //when
        Stock finded = stockRepository.findById(1L)
                .orElseThrow();

        //then
        assertThat(finded.getQuantity()).isEqualTo(99);
        
    
    }


}