package com.example.workshopstart.controller;

import com.example.workshopstart.dto.AddItemRequest;
import com.example.workshopstart.dto.CreateOrderRequest;
import com.example.workshopstart.model.Order;
import com.example.workshopstart.service.OrderService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Order create(@RequestBody CreateOrderRequest request) {
        return orderService.createOrder(request.vip(), request.country());
    }

    @PostMapping("/{id}/items")
    public Order addItem(@PathVariable Long id, @RequestBody AddItemRequest request) {
        return orderService.addItem(id, request.productId(), request.price(), request.quantity());
    }

    @PostMapping("/{id}/confirm")
    public Order confirm(@PathVariable Long id) {
        return orderService.confirmOrder(id);
    }

    @PostMapping("/{id}/ship")
    public Order ship(@PathVariable Long id) {
        return orderService.shipOrder(id);
    }

    @PostMapping("/{id}/cancel")
    public Order cancel(@PathVariable Long id) {
        return orderService.cancelOrder(id);
    }

    @GetMapping("/{id}")
    public Order get(@PathVariable Long id) {
        return orderService.findById(id);
    }
}
