package com.stockledger.repository;

import com.stockledger.model.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<CustomerOrder, Long> {
    List<CustomerOrder> findAllByOrderByCreatedAtDesc();
    List<CustomerOrder> findByStatusOrderByCreatedAtDesc(CustomerOrder.Status status);
}
