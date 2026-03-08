package com.example.workshopstart.service;

import com.example.workshopstart.model.Address;
import com.example.workshopstart.model.Customer;
import com.example.workshopstart.model.Order;
import com.example.workshopstart.model.OrderItem;
import com.example.workshopstart.model.OrderStatus;
import com.example.workshopstart.repository.OrderRepository;
import org.aspectj.weaver.ast.Or;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Order createOrder(boolean vip, String country) {
        Customer customer = new Customer(vip, new Address(country));
        Order order = new Order(customer);
        return orderRepository.save(order);
    }

    public Order addItem(Long orderId, String productId, BigDecimal price, int quantity) {
        Order order = getOrder(orderId);
        order.addItem(productId, price, quantity);
        return orderRepository.save(order);
    }

    public Order confirmOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.confirm();
        return orderRepository.save(order);
    }

    public Order shipOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.ship();
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.cancel();
        return orderRepository.save(order);
    }

    public Order findById(Long orderId) {
        return getOrder(orderId);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow(() -> new IllegalStateException("Order not found: " + orderId));
    }
}
