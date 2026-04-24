package com.example.ecommerce.order.repo;

import com.example.ecommerce.order.domain.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, String> {

    @Query("select distinct o from OrderEntity o left join fetch o.lines where o.id = :id")
    Optional<OrderEntity> findWithLinesById(@Param("id") String id);
}
