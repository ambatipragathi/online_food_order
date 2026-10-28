
package com.example.foodorder.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.example.foodorder.model.FoodOrder;
import com.example.foodorder.repository.FoodOrderRepository;

import jakarta.validation.Valid;
import jakarta.validation.Validator;

@RestController
@RequestMapping("/api/orders")
public class FoodOrderController {

    private final FoodOrderRepository repository;
    private final Validator validator;

    public FoodOrderController(
            FoodOrderRepository repository,
            Validator validator) {
        this.repository = repository;
        this.validator = validator;
    }

    // 1. GET - Get all orders
    @GetMapping
    public List<FoodOrder> getAllOrders() {
        return repository.findAll();
    }

    // 2. GET - Get order by ID
    @GetMapping("/{id}")
    public ResponseEntity<FoodOrder> getOrderById(
            @PathVariable Long id) {

        return repository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    // 3. POST - Add a new order
    @PostMapping
    public ResponseEntity<FoodOrder> addOrder(
            @Valid @RequestBody FoodOrder order) {

        order.setOrderId(null);

        FoodOrder savedOrder = repository.save(order);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(savedOrder);
    }

    // 4. PUT - Update the entire order
    @PutMapping("/{id}")
    public ResponseEntity<FoodOrder> updateOrder(
            @PathVariable Long id,
            @Valid @RequestBody FoodOrder updatedOrder) {

        return repository.findById(id).map(order -> {

            order.setCustomerName(
                    updatedOrder.getCustomerName());

            order.setFoodItem(
                    updatedOrder.getFoodItem());

            order.setQuantity(
                    updatedOrder.getQuantity());

            order.setPrice(
                    updatedOrder.getPrice());

            FoodOrder savedOrder = repository.save(order);

            return ResponseEntity.ok(savedOrder);

        }).orElse(ResponseEntity.notFound().build());
    }

    // 5. DELETE - Delete an order
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(
            @PathVariable Long id) {

        if (!repository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        repository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    // 6. PATCH - Partially update an order
    @PatchMapping("/{id}")
    public ResponseEntity<?> patchOrder(
            @PathVariable Long id,
            @RequestBody Map<String, Object> updates) {

        FoodOrder order = repository.findById(id)
                .orElse(null);

        if (order == null) {
            return ResponseEntity.notFound().build();
        }

        for (Map.Entry<String, Object> entry
                : updates.entrySet()) {

            String field = entry.getKey();
            Object value = entry.getValue();

            switch (field) {

                case "customerName":
                    if (!(value instanceof String)) {
                        return ResponseEntity.badRequest()
                                .body("Customer name must be text");
                    }
                    order.setCustomerName((String) value);
                    break;

                case "foodItem":
                    if (!(value instanceof String)) {
                        return ResponseEntity.badRequest()
                                .body("Food item must be text");
                    }
                    order.setFoodItem((String) value);
                    break;

                case "quantity":
                    if (!(value instanceof Number)) {
                        return ResponseEntity.badRequest()
                                .body("Quantity must be a number");
                    }

                    Number quantityValue = (Number) value;
                    double quantityNumber =
                            quantityValue.doubleValue();

                    if (!Double.isFinite(quantityNumber)
                            || quantityNumber != Math.floor(quantityNumber)
                            || quantityNumber < 1
                            || quantityNumber > Integer.MAX_VALUE) {
                        return ResponseEntity.badRequest()
                                .body("Quantity must be a positive whole number");
                    }

                    order.setQuantity((int) quantityNumber);
                    break;

                case "price":
                    if (!(value instanceof Number)) {
                        return ResponseEntity.badRequest()
                                .body("Price must be a number");
                    }

                    double priceValue =
                            ((Number) value).doubleValue();

                    if (!Double.isFinite(priceValue)
                            || priceValue < 0) {
                        return ResponseEntity.badRequest()
                                .body("Price must be a valid non-negative number");
                    }

                    order.setPrice(priceValue);
                    break;

                default:
                    return ResponseEntity.badRequest()
                            .body("Invalid field: " + field);
            }
        }

        // Validate the complete order after partial updates
        var errors = validator.validate(order);

        if (!errors.isEmpty()) {
            List<String> messages = errors.stream()
                    .map(error -> error.getMessage())
                    .toList();

            return ResponseEntity.badRequest().body(messages);
        }

        FoodOrder savedOrder = repository.save(order);

        return ResponseEntity.ok(savedOrder);
    }
}
