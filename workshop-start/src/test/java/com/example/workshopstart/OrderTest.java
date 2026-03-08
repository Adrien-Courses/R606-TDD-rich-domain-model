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
    private static Order createOrder() {
        Customer customer = new Customer(true, new Address("FR"));
        return new Order(customer);
    }

    @Test
    public void should_confirm_order_when_status_is_created_and_amount_is_greater_than_10() {
        Order order = createOrder();
        order.addItem("PEN", new BigDecimal("15.00"), 1);
        order.confirm();
        assertEquals(OrderStatus.CONFIRMED, order.getStatus());
    }

    @Test
    public void should_not_confirm_order_when_status_is_not_created() {
        Order order = createOrder();
        order.setStatus(OrderStatus.SHIPPED);
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_amount_is_less_than_10() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("5"), 1);
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    public void should_not_confirm_order_when_line_items_are_empty() {
        Order order = createOrder();
        Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
    }

    @Test
    void should_ship_confirmed_order() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();

        order.ship();

        assertEquals(OrderStatus.SHIPPED, order.getStatus());
    }

    @Test
    void should_not_ship_order_when_is_not_confirmed() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        Assertions.assertThrows(IllegalStateException.class, () -> order.ship());
    }

    @Test
    void should_cancel_order() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
    }

    @Test
    void should_not_cancel_order_when_is_shipped() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.confirm();
        order.ship();

        Assertions.assertThrows(IllegalStateException.class, () -> order.cancel());
    }

    @Test
    void shipping_cost_should_be_free_for_international_orders_and_vip_customer() {
        Order order = createOrder();

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("0.00"), actuel);
    }

    @Test
    void shipping_cost_should_be_5_for_vip_customer() {
        Order order = createOrder();

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("0.00"), actuel);
    }

    @Test
    void shipping_cost_should_be_5_for_international_orders() {
        Order order = new Order(new Customer(false, new Address("US")));

        BigDecimal actuel = order.shippingCost();

        Assertions.assertEquals(new BigDecimal("5.00"), actuel);
    }

    @Test
    void should_return_total_price_of_order() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        BigDecimal actual = order.subtotal();

        Assertions.assertEquals(new BigDecimal("20.00"), actual);
    }

    @Test
    void should_return_total_price_of_order_with_multiple_items() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.addItem("BOOK2", new BigDecimal("30.00"), 2);

        BigDecimal actual = order.subtotal();

        Assertions.assertEquals(new BigDecimal("80.00"), actual);
    }

    @Test
    void should_add_item_to_order() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("20.00"), order.subtotal());
    }

    @Test
    void should_add_item_to_order_when_status_is_created() {
        Order order = createOrder();
        order.cancel();

        Assertions.assertThrows(IllegalStateException.class, () -> order.addItem("BOOK", new BigDecimal("20.00"), 1));
    }

    @Test
    void should_add_quantity_to_existing_item() {
        Order order = createOrder();
        order.addItem("BOOK", new BigDecimal("20.00"), 1);
        order.addItem("BOOK", new BigDecimal("20.00"), 1);

        assertEquals(1, order.getItems().size());
        assertEquals(new BigDecimal("40.00"), order.subtotal());
    }
}
