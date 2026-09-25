package com.letsTrade.demo.order.repository;

import com.letsTrade.demo.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("SELECT o FROM Order o JOIN o.user u WHERE u.email = :email ORDER BY o.createdAt DESC")
    List<Order> findByUserEmailOrderByCreatedAtDesc(@Param("email") String email);

    @Query("SELECT o FROM Order o JOIN o.user u WHERE o.id = :id AND u.email = :email")
    Optional<Order> findByIdAndUserEmail(@Param("id") Long id, @Param("email") String email);
}
