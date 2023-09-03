package com.example.concurrentissue.facade;

import com.example.concurrentissue.service.OptimisticLockStockService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class OptimisticLockStockFacade {

    private final OptimisticLockStockService optimisticLockStockService;

    /*
        별도의 lock을 잡지 않아서 비관 락보단 성능이 좋음.
        하지만 update가 실패 했을 때 재시도 로직을 작성해줘야 함.
        충돌이 빈번하게 발생 혹은 발생이 예상되면 비관 락을, 그렇지 않겠다면 낙관 락을 추천함.
     */
    public void decrease(Long id, Long quantity) throws InterruptedException {
        while(true){
            try {
                optimisticLockStockService.decrease(id, quantity);
                break;
            } catch (Exception e) { // org.springframework.orm.ObjectOptimisticLockingFailureException 발생!
                Thread.sleep(50);
            }
        }
    }

}
