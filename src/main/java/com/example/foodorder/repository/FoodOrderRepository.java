
package com.example.foodorder.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.foodorder.model.FoodOrder;

public interface FoodOrderRepository
        extends JpaRepository<FoodOrder, Long> {

}
