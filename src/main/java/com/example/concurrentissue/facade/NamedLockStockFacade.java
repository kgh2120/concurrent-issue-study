package com.example.concurrentissue.facade;


import com.example.concurrentissue.repository.LockRepository;
import com.example.concurrentissue.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Component
public class NamedLockStockFacade {

    private final LockRepository lockRepository;

    private final StockService stockService;



    /*
        named lock은 주로 분산 락을 구현할 때 사용된다.
        비관락을 timeout을 구현하기 힘들지만, named lock은 타임아웃을 설정하기 쉬움.
        데이터 삽입 시 정합성을 맞춰야 하는 경우에도, named lock을 사용할 수 있다.
        하지만, transaction 종료 시, lock해제와 session 관리를 잘 해줘야 하기 때문에, 구현 방법이 복잡하다.
     */
    @Transactional
    public void decrease(Long id, Long quantity) {
        try{
            lockRepository.getLock(id.toString());
            stockService.decrease(id, quantity);
        }finally {
            lockRepository.releaseLock(id.toString());
        }
    }
}
