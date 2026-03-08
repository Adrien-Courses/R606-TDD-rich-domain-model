# Workshop – Refactoring from Anemic Domain Model to Rich Domain Model

This archive contains two Spring Boot projects:

- `workshop-start`: intentionally layered Spring architecture (`controller / service / repository`) with an **anemic domain model**, a **Law of Demeter violation**, and **red tests**.
- `solution-final`: one possible **DDD-oriented correction** with a richer `Order` aggregate and **green tests**.

## Business context

You work on a B2B ordering system.

An order:
- belongs to a customer
- contains multiple order lines
- supports state transitions: `CREATED`, `CONFIRMED`, `SHIPPED`, `CANCELLED`
- applies a discount on product subtotal
- applies international shipping when the customer is outside France and is not VIP

### Rules

1. An order can be confirmed only if:
   - it contains at least one item
   - product subtotal is at least `10.00`
2. A confirmed order cannot be modified.
3. A shipped order cannot be cancelled.
4. Discount policy:
   - subtotal `> 100` => `10%`
   - subtotal `> 500` => `20%`
5. Shipping:
   - international + non VIP => `5.00`
   - otherwise `0.00`
6. Adding the same product twice should merge the line instead of duplicating it.

---

## Workshop flow

### 1) Start with the broken project

```bash
cd workshop-start
mvn test
```

You should see failing tests. They highlight three design problems:

- mutable aggregate internals are exposed
- duplicate business logic / missing aggregate behavior
- Law of Demeter violation through `order.getCustomer().getAddress().getCountry()`

### 2) Suggested refactoring path

#### Step 1 – Protect the aggregate

Goals:
- hide mutable collections
- stop external code from mutating order internals
- move `recalculateTotal()` inside the aggregate

Questions:
- should `items` still be publicly mutable?
- who is responsible for keeping total consistent?

#### Step 2 – Move behavior into `Order`

Introduce domain methods such as:
- `order.addItem(...)`
- `order.confirm()`
- `order.cancel()`
- `order.ship()`

Make the service thinner: load aggregate, call behavior, save aggregate.

#### Step 3 – Remove the Law of Demeter violation

In the start project, shipping is computed like this:

```java
order.getCustomer().getAddress().getCountry()
```

Refactor so the domain speaks in business terms:
- `customer.isInternational()`
- or even better `order.shippingCost()`

#### Step 4 – Merge responsibilities coherently

Push rules toward the aggregate root:
- item merge policy
- state transition rules
- total recomputation
- shipping / discount orchestration

#### Step 5 – Optional advanced step

Introduce value objects:
- `Money`
- `ProductId`
- immutable `Address`

---

## Teaching goals

Participants should be able to identify:
- anemic domain model
- feature envy in `OrderService`
- Tell, Don’t Ask violations
- Law of Demeter violations
- leaky aggregate boundaries

---

## Final correction

A complete corrected version is available in:

```bash
cd solution-final
mvn test
```

The final version demonstrates:
- thin application service
- rich `Order` aggregate
- immutable or protected internal state
- domain-oriented methods instead of chains of getters

---

## Suggested workshop timing

- 15 min: read the code and failing tests
- 30 min: move behavior into `Order`
- 20 min: remove LoD / protect invariants
- 15 min: debrief and compare with `solution-final`
