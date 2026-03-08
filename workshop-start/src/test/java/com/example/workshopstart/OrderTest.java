package com.example.workshopstart;

import com.example.workshopstart.model.Address;
import com.example.workshopstart.model.Customer;
import com.example.workshopstart.model.Order;
import com.example.workshopstart.model.OrderStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.lang.NonNull;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class OrderTest {

    @NonNull
    private static Order createOrder(BigDecimal amount) {
        Customer customer = new Customer(true, new Address("FR"));
        return new Order(customer, amount);
    }

    @Test
    public void should_confirm_order_when_status_is_created_and_amount_is_greater_than_10() {
        Order order = createOrder(new BigDecimal("15.00"));
        order.addItem("PEN", new BigDecimal("15.00"), 1);
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    public void should_not_confirm_order_when_status_is_not_created() {
        Order order = createOrder(new BigDecimal("15.00"));
        order.setStatus(OrderStatus.SHIPPED);
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_amount_is_less_than_10() {
        Order order = createOrder(new BigDecimal("5.00"));
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_line_items_are_empty() {
        Order order = createOrder(new BigDecimal("15.00"));
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    void should_ship_confirmed_order() {
        Order order = createOrder(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();

        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void should_not_ship_order_when_is_not_confirmed() {
        Order order = createOrder(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        Assertions.assertThrows(IllegalStateException.class, () -> order.ship());
    }

    @Test
    void should_cancel_order() {
        Order order = createOrder(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void should_not_cancel_order_when_is_shipped() {
        Order order = createOrder(new BigDecimal("20.00"));
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();
        order.ship();

        Assertions.assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void shipping_cost_should_be_free_for_international_orders_and_vip_customer() {
        Order order = createOrder(new BigDecimal("20.00"));

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("0.00"), actuel);
    }

    @Test
    void shipping_cost_should_be_5_for_vip_customer() {
        Order order = createOrder(new BigDecimal("20.00"));

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("0.00"), actuel);
    }

    @Test
    void shipping_cost_should_be_5_for_international_orders() {
        Order order = new Order(new Customer(false, new Address("US")), new BigDecimal("20.00"));

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("5.00"), actuel);
    }
}
