# Atelier TDD + DDD --- Gestion de Commande

Ce workshop a pour objectif d'apprendre à **écrire du code métier guidé
par les tests (TDD)** tout en appliquant les principes du
**Domain‑Driven Design (DDD)**.

L'exercice consiste à faire évoluer un modèle de commande afin que **les
règles métier soient protégées par le domaine lui‑même**.

Nous allons partir d'un **Anemic Domain Model** (logique métier dans un
service) puis progressivement déplacer cette logique **dans l'agrégat
`Order`**.

------------------------------------------------------------------------

# Écrire les tests dans un Anemic Domain Model

Aller sur le commit initial :
`git checkout 054bc8984288db629af929265beacc82d6b7e9a0`

Dans la version initiale, la logique métier se trouve dans la classe
`OrderService`.

La première étape consiste à écrire des **tests métier** pour décrire le
comportement attendu du système.

Les tests suivants sont demandés :

-   On ne peut pas ajouter un produit à une commande confirmée
-   On ne peut pas confirmer une commande vide
-   On ne peut pas valider une commande inférieure à 10 euros
-   On ne peut pas envoyer une commande non confirmée

Ces tests décrivent **les invariants métier** du système.

> Un invariant métier est une règle qui doit toujours être respectée
> pour garantir la cohérence du domaine.

------------------------------------------------------------------------

# Refactoring

En DDD, **les invariants métier doivent vivre dans l'agrégat**, et non
dans un service applicatif.

Cela signifie que l'objet `Order` doit protéger lui‑même sa cohérence
interne.

Au lieu d'avoir :

``` java
orderService.confirmOrder(orderId);
```

on veut que le domaine exprime directement l'intention métier :

``` java
order.confirm();
```

Ainsi : - le **service orchestre** - le **domaine décide**

------------------------------------------------------------------------

# Approche TDD

Nous allons utiliser la boucle classique du **Test Driven Development**
:

1.  **Red** → écrire un test qui échoue
2.  **Green** → écrire le minimum de code pour le faire passer
3.  **Refactor** → améliorer le code sans casser les tests

Les tests deviennent alors **la documentation vivante du comportement
métier**.

------------------------------------------------------------------------

# TDD sur les statuts

Les règles métier suivantes doivent être implémentées :

-   Ne pas pouvoir confirmer une commande vide ou dont le montant est
    inférieur à 10 euros
-   Ne pas pouvoir envoyer une commande qui n'est pas dans l'état
    `CONFIRMED`
-   Ne pas pouvoir annuler une commande déjà envoyée

Pour illustrer l'approche TDD, nous allons implémenter la règle :

**"Ne pas pouvoir confirmer une commande vide ou dont le montant est
inférieur à 10 euros".**

------------------------------------------------------------------------

## 1. Partir du langage métier

Avant d'écrire du code, on décrit les règles en **langage métier**.

-   Une commande ne peut être confirmée que si elle est encore dans
    l'état `CREATED`
-   Une commande ne peut être confirmée que si son montant est
    strictement supérieur à **10,00 euros**
-   Lorsque la confirmation réussit, le statut devient `CONFIRMED`

Ces règles définissent **trois comportements à tester** :

-   le cas nominal (succès)
-   le statut invalide
-   le montant invalide

------------------------------------------------------------------------

## 2. Définir la méthode du point de vue du domaine

Au lieu d'exposer une logique technique via un service, nous exprimons
directement le comportement métier.

``` java
order.confirm();
```

Cela permet d'avoir un domaine **expressif et riche en comportement**.

------------------------------------------------------------------------

## 3. Première étape TDD : écrire le test du cas nominal

``` java
@Test
void should_confirm_order_when_status_is_created_and_amount_is_greater_than_10() {
    Order order = new Order(new BigDecimal("15.00"));

    order.confirm();

    assertThat(order.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
}
```

------------------------------------------------------------------------

## 4. Écrire le minimum de code pour faire passer le test

``` java
class Order {
    public void confirm() {
        status = OrderStatus.CONFIRMED;
    }
}
```

On écrit **le code le plus simple possible**.

------------------------------------------------------------------------

## 5. Ajouter la première règle métier : le statut doit être CREATED

Test :

``` java
@Test
void should_not_confirm_order_when_status_is_not_created() {
    Order order = new Order(OrderStatus.SHIPPED, new BigDecimal("15.00"));

    assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Only orders in CREATED state can be confirmed");
}
```

Implémentation minimale :

``` java
public void confirm() {
    if (status != OrderStatus.CREATED) {
        throw new IllegalStateException("Only orders in CREATED state can be confirmed");
    }
    status = OrderStatus.CONFIRMED;
}
```

------------------------------------------------------------------------

## 6. Ajouter la règle métier sur le montant (> 10 euros)

``` java
@Test
void should_not_confirm_order_when_amount_is_less_than_or_equal_to_10() {
    Order order = new Order(OrderStatus.CREATED, new BigDecimal("10.00"));

    assertThatThrownBy(order::confirm)
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Order amount must be greater than 10.00 euros");
}
```

Implémentation :

``` java
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

------------------------------------------------------------------------

## 7. Une commande doit contenir au moins une ligne

``` java
@Test
public void should_not_confirm_order_when_line_items_are_empty() {
    Order order = new Order(new BigDecimal("15.00"));
    Assertions.assertThrows(IllegalStateException.class, () -> order.confirm());
}
```

Implémentation :

``` java
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

------------------------------------------------------------------------

# TDD sur la livraison et le prix de la commande

Lors de l'implémentation de la livraison, attention à respecter la **loi
de Déméter**.

Un objet ne doit communiquer qu'avec : 
- lui‑même 
- ses dépendances directes

Cela évite les dépendances trop fortes entre objets.

------------------------------------------------------------------------

# TDD sur le prix de la commande

Le prix total de la commande doit être **calculé à partir des lignes de
commande**, et non stocké comme une simple donnée modifiable.

Cela permet d'éviter les incohérences.

------------------------------------------------------------------------

# TDD : ajouter un article

Cette partie est réalisée à la fin car elle est légèrement plus
complexe.

Elle nécessite également un **refactoring du modèle**.

Après avoir supprimé `total` du constructeur de `Order`, certains tests
vont échouer car `addLine()` n'est pas encore correctement implémentée.

Ce comportement est normal dans une approche TDD.

Grâce aux tests : - nous pouvons refactoriser sereinement - nous
détectons immédiatement toute régression

------------------------------------------------------------------------

# Refactoring du Service

Maintenant que la logique métier est dans le **Domain Model**, elle doit
être retirée de la couche **Service**.

Le service devient alors une simple couche d'orchestration.

Les tests présents dans `WorkshopStartRedTests` étaient des **tests
métier**.\
Ils n'ont donc plus lieu d'exister dans cette couche.

Les tests métier doivent désormais cibler directement **l'agrégat
`Order`**.
