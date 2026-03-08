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
        Order order = new Order();
        order.setCustomer(customer);
        return orderRepository.save(order);
    }

    public Order addItem(Long orderId, String productId, BigDecimal price, int quantity) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.CREATED) {
            throw new IllegalStateException("Cannot modify order once confirmed");
        }

        order.getItems().add(new OrderItem(productId, price, quantity));
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    public Order confirmOrder(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getItems().isEmpty()) {
            throw new IllegalStateException("Order must contain at least one item");
        }

        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        if (subtotal.compareTo(new BigDecimal("10.00")) < 0) {
            throw new IllegalStateException("Minimum subtotal is 10.00");
        }

        order.setStatus(OrderStatus.CONFIRMED);
        return orderRepository.save(order);
    }

    public Order shipOrder(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getStatus() != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }

        order.setStatus(OrderStatus.SHIPPED);
        return orderRepository.save(order);
    }

    public Order cancelOrder(Long orderId) {
        Order order = getOrder(orderId);

        if (order.getStatus() == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel a shipped order");
        }

        order.setStatus(OrderStatus.CANCELLED);
        return orderRepository.save(order);
    }

    public Order findById(Long orderId) {
        return getOrder(orderId);
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId).orElseThrow();
    }

    private void recalculateTotal(Order order) {
        BigDecimal subtotal = order.getItems().stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountedSubtotal = applyDiscount(subtotal);
        BigDecimal shipping = calculateShipping(order);

        order.setTotal(discountedSubtotal.add(shipping));
    }

    private BigDecimal applyDiscount(BigDecimal subtotal) {
        if (subtotal.compareTo(new BigDecimal("500.00")) > 0) {
            return subtotal.multiply(new BigDecimal("0.80"));
        }
        if (subtotal.compareTo(new BigDecimal("100.00")) > 0) {
            return subtotal.multiply(new BigDecimal("0.90"));
        }
        return subtotal;
    }

    // Intentional Law of Demeter violation
    private BigDecimal calculateShipping(Order order) {
        boolean international = !"FR".equals(order.getCustomer().getAddress().getCountry());
        boolean vip = order.getCustomer().isVip();
        return international && !vip ? new BigDecimal("5.00") : BigDecimal.ZERO;
    }
}
