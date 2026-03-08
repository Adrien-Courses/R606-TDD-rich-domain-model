# Ecrire les tests dans un Anemic Domain Model

La première étape est de faire les tests sur notre logique méier contenu dans la classe `OrderService`.
Les tests suivant sont demandés :
- On ne peut pas ajouter un produit à une commande confirmée
- On ne peut pas confirmer une commande vide
- On ne peut pas valider une commande infénieur à 10 euros
- On ne peut pas envoyer une commande non confirmée


# Refactoring
business invariants should live inside the aggregate, not in an application service, so the order protects its own consistency.

On va utiliser une approche TDD. Les tests vont nous donner les comportements attendus.

## TDD sur les status
- Ne pas pouvoir confirmer une commande vide ou avant un montant inférieur à 10 euros
- Ne pas pouvoir envoyer une commande qui n'est pas dans l'état confirmé
- Ne pas pouvoir annuler une commande déjà envoyée

Pour illustrer, l'approcher TDD, nous allons coder ensemble le premier test "Ne pas pouvoir confirmer une commande vide ou avant un montant inférieur à 10 euros"

### 1. Start from the business language
Before writing code, write the rules in plain language:
- An order can be confirmed only if it is still being CREATED.
- An order can be confirmed only if its amount is strictly greater than 10.00.
- When confirmation succeeds, the status becomes CONFIRMED.

That gives you 3 behaviors to test:
- success case
- invalid status case
- invalid amount case

### 2. Define the method from the domain point of view

Instead of this: `orderService.confirmOrder(orderId);`

we want this: `order.confirm();`

### 3. First TDD step: write the happy-path test

Start with the simplest valid case.

```
@Test
void should_confirm_order_when_status_is_created_and_amount_is_greater_than_10() {
    Order order = new Order(new BigDecimal("15.00"));

    order.confirm();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
}
```

### 4. Write the minimum code to make it pass

```
class Order {
    public void confirm() {
        status = OrderStatus.CONFIRMED;
    }
}
```

### 5. Add the first business rule: status must be CREATED

```
@Test
void should_not_confirm_order_when_status_is_not_created() {
    Order order = new Order(OrderStatus.SHIPPED, new BigDecimal("15.00"));

    assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only orders in CREATED state can be confirmed");
}
```

Then implement the minimum code:

```
public void confirm() {
    if (status != OrderStatus.CREATED) {
        throw new IllegalStateException("Only orders in CREATED state can be confirmed");
    }
    status = OrderStatus.CONFIRMED;
}
```

### 6. Add the second business rule: amount must be greater than 10 euros

```
@Test
void should_not_confirm_order_when_amount_is_less_than_or_equal_to_10() {
    Order order = new Order(OrderStatus.CREATED, new BigDecimal("10.00"));

    assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order amount must be greater than 10.00 euros");
}
```

```
public void confirm() {
    if (status != OrderStatus.CREATED) {
        throw new IllegalStateException("Only orders in CREATED state can be confirmed");
    }

    if (totalAmount.compareTo(new BigDecimal("10.00")) <= 0) {
        throw new IllegalStateException("Order amount must be greater than 10.00 euros");
    }

    status = OrderStatus.CONFIRMED;
}
```

### 7. De même que la règle 6, il faut au moins une ligne

```
@Test
public void should_not_confirm_order_when_line_items_are_empty() {
    Order order = new Order(new BigDecimal("15.00"));
    Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
}
```

```
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
```

## TDD sur la livraison et le prix de la commande
Attention à la loi de Demeter sur l'adresse de livraison.

## TDD sur la le prix de la commande

## TDD ajouter un article
On le fait à la fin car un peu plus compliqué, il va également être necessaore de refactor notre code.
En effet, juste apr!s avoir supprimé "total" du constructeur de Order on a des tests qui échoue car addLine() n'est pas encore codé correctement
MAis ce n'est pas grave, car grace a nos test on va s'assurer d'aucune regression.