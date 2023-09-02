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
