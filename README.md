# 인프런 - 재고시스템으로 알아보는 동시성이슈 해결방법 강의 학습 코드


## 상황

거래가 되어서 재고가 감소하는 상황이다. 당신은 재고의 양을 정확히 추적하면 된다.


## 방법1 - 일반적인 코드 작성

```java
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
```

### 결과 : Fail

### 이유 : 
멀티 쓰레드 환경에서 동시성 문제를 처리해주지 않았다.


## 방법 2 - synchronized 키워드 사용
자바에서 동시성 문제를 해결해주는 `synchronized` 키워드를 추가했다.

```java
    @Transactional
    public synchronized void decrease(Long id, Long quantity) {
        // Stock 조회
        // 재고 감소
        // 갱신된 값 저장
        Stock stock = stockRepository.findById(id)
                .orElseThrow();
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);
    }
```

### 결과 FAIL

### 이유

`@Transactionl` 이 걸려있으면 서비스 클래스의 형태가 변경된다. 아래 코드와 같이 변경되기 때문에,
`synchronized`로는 동시성을 보장할 수 없다.
```java
    /*
        Transactional을 걸면 이런 형태의 서비스가 생성된다고 한다.
        자바의 synchronized 키워드가 적용되어도 동시성 이슈가 해결되지 않는 이유는
        트랜잭션이 끝날 때(commit) db에 반영이 되는데 decrease에서만 synchronized를 건다고
        문제가 해결되지 않음.
     */
    public void decrease(Long id, Long quantity){
        startTransaction();

        stockServiceWithSynchronized.decrease(id, quantity);

        endTransaction();

    }
```

그렇기 때문에 `@Transactional`을 지우면 정상적으로 해결이 가능하다. 하지만...

### 문제점
`synchronized` 키워드는 프로세스내에서 동시성 문제를 보장해주지만, 서버가 여러 대라면 동시성을 보장할 수 없다.


## 방법 3 - pessimistic lock(비관적 락) 사용

자바의 동시성 해결 방법으로는 멀티 프로세스 환경에서는 적용되지 않는다는 것을 알았다. 그래서 DB차원에 LOCK을 걸어서 이를 해결하려고 한다.
첫번째로 사용한 것은 JPA의 비관적 락(pessimistic lock)이다.

JPA에서 비관적 락을 이용하는 방법은 `@Lock` 어노테이션을 추가하는 것이다.
`@Lock`은 많은 옵션들이 있는데, 비관적 락의 경우는 3가지인 `PESSIMISTIC_READ`, `PESSIMISTIC_WRITE`, `PESSIMISTIC_FORCE_INCREMENT`가 있다.
여기서 `PESSIMISTIC_READ`, `PESSIMISTIC_WRITE` 는 DB의 `Shared Lock`, `Exclusive Lock`과 동일하다.

방법 3에서는 값에 대한 업데이트가 지속적으로 발생하기 때문에, `PESSIMISTIC_WRITE`를 이용했고 테스트에 통과할 수 있었다.

```java
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithPessimisticLock(Long id);
```

비관적 락의 경우는 충돌이 빈번하게 발생하는 경우, 낙관적 락보다 성능이 좋을 수 있고, 업데이트를 제어하기 때문에 데이터 정합성이 보장된다는 장점이 있다.
하지만 별도의 DB LOCK을 이용하기 때문에 성능의 감소가 발생할 수 있다. (LOCK 유지, 동시성 감소)

## 방법 4 - optimistic lock(낙관적 락) 사용

낙관적 락은 비관적 락과 달리 실제로 존재하는 lock을 이용하지 않습니다. 각 record에 version을 명시하고, update가 된다면 version을 변경해줍니다.
그렇게 된 경우 동시성 문제가 발생할 때 A와 B 쓰레드가 접근을 했다면, A쓰레드에서 version을 업데이트 했기 때문에, B 쓰레드에서 발생하는 변경은 적용될 수 없습니다.
(쿼리가 update stock set product_id=?, quantity=?, version=? where id=? and version=? 로 나가기 때문)

낙관적 락을 구현하는 방법은 다음과 같습니다.

1. entity에 version을 추가합니다.

```java
    @Version // javax.persistance 로 이용
    private Long version;
```

2. repository에 LockMode가 Optimistic인 쿼리를 작성합니다.
```java
    @Lock(LockModeType.OPTIMISTIC)
    @Query("select s from Stock s where s.id = :id")
    Stock findByIdWithOptimisticLock(Long id);
```

3. 낙관적 락으로 인해 update가 실패한 경우 재시도 로직 작성.
```java
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
```

decrease가 실패하게 된다면 ObjectOptimisticLockingFailureException가 발생하여, 잠시 대기 후 다시 decrease 로직을 작동시킵니다.
해당 코드에서 optimisticLockStockService.decrease를 호출한 클래스는 OptimisticLockStockFacade로 Facade 패턴이 적용된 클래스입니다.

Facade 패턴은 아래와 같은 특징을 가지고 있다고 한다. 해당 코드에서 퍼사드 패턴의 장점으로는 OptimisticLockStockFacade를 실제로 호출하게 될 컨트롤러는 낙관적 락에 대한 신경을 쓰지 않을 것이고, 낙관적 락을 위한 처리 코드를 OptimisticLockStockService에 담지 않아 비즈니스 로직에 영향을 주지 않을 수 있다는 점일 것 같다.

 
> _퍼사드는 클래스 라이브러리 같은 어떤 소프트웨어의 다른 커다란 코드 부분에 대한 간략화된 인터페이스를 제공하는 객체이다.<br/> 
퍼사드는 소프트웨어 라이브러리를 쉽게 사용할 수 있게 해준다.<br/> 또한 퍼사드는 소프트웨어 라이브러리를 쉽게 이해할 수 있게 해 준다. 퍼사드는 공통적인 작업에 대해 간편한 메소드들을 제공해준다.<br/>
퍼사드는 라이브러리를 사용하는 코드들을 좀 더 읽기 쉽게 해준다.<br/>
퍼사드는 라이브러리 바깥쪽의 코드가 라이브러리의 안쪽 코드에 의존하는 일을 감소시켜 준다. 대부분의 바깥쪽의 코드가 퍼사드를 이용하기 때문에 시스템을 개발하는 데 있어 유연성이 향상된다. <br/>
퍼사드는 좋게 작성되지 않은 API의 집합을 하나의 좋게 작성된 API로 감싸준다.<br/> - 위키백과 퍼사드 패턴_


낙관적 락은 별도의 lock을 이용하지 않아서 비관적 락보다 성능이 뛰어나지만, update가 실패했을 경우에 재시도 로직을 개발자가 직접 작성해야 한다는 단점이 있다.
또한 충돌이 빈번하게 발생하는 경우나, 그것이 예상되는 경우는 비관적 락을 이용하는 편이 성능이 더 좋다고 한다.(계속 반복을 하니까)
 

## 방법 5 - Named Lock(MySQL) 이용

