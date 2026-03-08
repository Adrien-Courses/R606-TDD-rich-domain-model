package com.example.workshopstart;

import com.example.workshopstart.model.Order;
import com.example.workshopstart.model.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    @Test
    public void should_confirm_order_when_status_is_created_and_amount_is_greater_than_10() {
        Order order = new Order(new BigDecimal("15.00"));
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    public void should_not_confirm_order_when_status_is_not_created() {
        Order order = new Order(new BigDecimal("15.00"));
        order.setStatus(OrderStatus.SHIPPED);
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_amount_is_less_than_10() {
        Order order = new Order(new BigDecimal("5.00"));
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_line_items_are_empty() {
        Order order = new Order(new BigDecimal("15.00"));
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    void should_ship_confirmed_order() {

        Order order = new Order(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();

        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void should_not_ship_order_when_is_not_confirmed() {
        Order order = new Order(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        Assertions.assertThrows(IllegalStateException.class, () -> order.ship());
    }

    @Test
    void should_cancel_order() {
        Order order = new Order(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void should_not_cancel_order_when_is_shipped() {
        Order order = new Order(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();
        order.ship();

        Assertions.assertThrows(IllegalStateException.class, () -> order.cancel());
    }
}
