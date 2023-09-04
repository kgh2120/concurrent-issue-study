package com.example.concurrentissue.repository;

import com.example.concurrentissue.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface LockRepository extends JpaRepository<Stock,Long> { // 별도의 JDBC 를 이용해서 LOCK을 관리해야함.

    @Query(value = "select get_lock(:key,1000)", nativeQuery = true)
    void getLock(String key);

    @Query(value = "select release_lock(:key)", nativeQuery = true)
    void releaseLock(String key);

}
