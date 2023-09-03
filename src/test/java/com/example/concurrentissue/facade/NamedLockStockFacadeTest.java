package com.example.concurrentissue.facade;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.concurrentissue.domain.Stock;
import com.example.concurrentissue.repository.StockRepository;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class NamedLockStockFacadeTest {

    @Autowired
    private NamedLockStockFacade namedLockStockFacade;

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
    void 동시에_100개의_요청_NamedLock() throws Exception{
        //given
        int threadCount = 100;
        // ExecutorService는 비동기로 처리하는 작업을 반복문으로 해준다고하네..?
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try{
                    namedLockStockFacade.decrease(1L,1L);
                }  finally {
                    latch.countDown();
                }
            });
        }
        //when
        latch.await(); // 다른 쓰레드에서 실행되는 작업이 완료될 떄 까지 기다려주는 클래스.
        //then

        Stock stock = stockRepository.findById(1L).orElseThrow();
        // 100 - 1 (1*100) = 0

        assertThat(stock.getQuantity()).isEqualTo(0);
    }

}

