package com.example.concurrentissue.repository;

import com.example.concurrentissue.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock,Long> {

}
