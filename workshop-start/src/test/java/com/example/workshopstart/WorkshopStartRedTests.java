package com.example.workshopstart;

import com.example.workshopstart.model.*;
import com.example.workshopstart.repository.OrderRepository;
import com.example.workshopstart.service.OrderService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkshopStartRedTests {

    @Mock
    private OrderRepository orderRepository;

    @InjectMocks
    private OrderService orderService;

    @Test
    void should_not_add_item_if_order_is_confirmed() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act / Assert
        assertThatThrownBy(() ->
                orderService.addItem(1L, "BOOK", new BigDecimal("20.00"), 1)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Cannot modify order once confirmed");
    }

    @Test
    void should_not_confirm_empty_order() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act / Assert
        assertThatThrownBy(() ->
                orderService.confirmOrder(1L)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Order must contain at least one item");
    }

    @Test
    void should_not_confirm_order_when_subtotal_less_than_10() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CREATED);

        order.getItems().add(new OrderItem("PEN", new BigDecimal("5.00"), 1));

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));

        // Act / Assert
        assertThatThrownBy(() ->
                orderService.confirmOrder(1L)
        ).isInstanceOf(IllegalStateException.class)
                .hasMessage("Minimum subtotal is 10.00");
    }

    @Test
    void should_ship_confirmed_order() {

        // Arrange
        Order order = new Order();
        order.setId(1L);
        order.setStatus(OrderStatus.CONFIRMED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(order));
        when(orderRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Act
        Order updated = orderService.shipOrder(1L);

        // Assert
        assertThat(updated.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    }
}
