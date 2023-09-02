package com.example.concurrentissue.transaction;

import com.example.concurrentissue.service.StockService;

public class TransactionStockService {

    private StockService stockService;

    /*
        Transactional을 걸면 이런 형태의 서비스가 생성된다고 한다.
        자바의 synchronized 키워드가 적용되어도 동시성 이슈가 해결되지 않는 이유는
        트랜잭션이 끝날 때(commit) db에 반영이 되는데 decrease에서만 synchronized를 건다고
        문제가 해결되지 않음.
     */
    public void decrease(Long id, Long quantity){
        startTransaction();

        stockService.decrease(id, quantity);

        endTransaction();

    }

    private void endTransaction() {
        System.out.println("Commit");

    }

    private void startTransaction() {
        System.out.println("Transaction start");
    }

}
