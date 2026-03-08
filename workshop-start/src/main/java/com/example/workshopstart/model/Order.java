package com.example.workshopstart.model;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = CascadeType.ALL)
    private Customer customer;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    private BigDecimal total;

    @OneToMany(cascade = CascadeType.ALL)
    private List<OrderItem> items = new ArrayList<>();

    public Order() {
    }

    public Order(Customer customer, BigDecimal total) {
        this.status = OrderStatus.CREATED;
        this.total = total;
        this.customer = customer;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public void setCustomer(Customer customer) {
        this.customer = customer;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public void setStatus(OrderStatus status) {
        this.status = status;
    }

    public BigDecimal getTotal() {
        return total;
    }

    public void setTotal(BigDecimal total) {
        this.total = total;
    }

    public List<OrderItem> getItems() {
        return items;
    }

    public void setItems(List<OrderItem> items) {
        this.items = items;
    }

    public void setId(long l) {
    }

    public void confirm() {
        if(status != OrderStatus.CREATED) {
            throw new IllegalStateException("Order already confirmed");
        }

        if(this.total.compareTo(new BigDecimal("10.00")) < 0) {
            throw new IllegalStateException("Minimum order amount is 10.00");
        }

        if(this.items.isEmpty()) {
            throw new IllegalStateException("Order must contain at least one item");
        }

        this.status = OrderStatus.CONFIRMED;
    }

    public void ship() {
        if(status != OrderStatus.CONFIRMED) {
            throw new IllegalStateException("Only confirmed orders can be shipped");
        }

        this.status = OrderStatus.SHIPPED;
    }

    public void addItem(String book, BigDecimal bigDecimal, int i) {
        items.add(new OrderItem(book, bigDecimal, i));
    }

    public void cancel() {
        if (status == OrderStatus.SHIPPED) {
            throw new IllegalStateException("Cannot cancel an order that has already been shipped");
        }

        this.status = OrderStatus.CANCELLED;
    }

    public BigDecimal shippingCost() {
        // On crée un méthode isInternationnal pour ne pas casser la Loi de Demeter
        if(!customer.isVip() && customer.isInternationnal()) {
            return new BigDecimal("5.00");
        }

        return new BigDecimal("0.00");
    }
}
