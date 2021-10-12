package com.hendisantika.entity;

import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.Uni;
import io.smallrye.mutiny.tuples.Tuple4;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.reactive.mutiny.Mutiny;

import javax.persistence.*;
import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * Created by IntelliJ IDEA.
 * Project : quarkus-rest-api-crud
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 02/10/21
 * Time: 07.05
 */
@Entity
@Table(name = "ShoppingCart", schema = "product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@NamedQueries(value = {@NamedQuery(name = "ShoppingCart.findAll",
        query = "SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.cartItems item LEFT JOIN FETCH item.product"),
        @NamedQuery(name = "ShoppingCart.getById",
                query = "SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.cartItems item LEFT JOIN FETCH item.product " +
                        "where c.id = ?1")})
public class ShoppingCart extends PanacheEntityBase {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public Long id;

    public int cartTotal;

    @OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL,
            orphanRemoval = true)
    @JoinColumn
    public Set<ShoppingCartItem> cartItems;

    public String name;

    public static Uni<ShoppingCart> findByShoppingCartId(Long id) {
        return find("#ShoppingCart.getById", id).firstResult();
    }

    public static Uni<List<ShoppingCart>> getAllShoppingCarts() {
        return find("#ShoppingCart.findAll").list();
    }

    public static Multi<ShoppingCart> findAllWithJoinFetch() {
        return stream("SELECT c FROM ShoppingCart c LEFT JOIN FETCH c.cartItems");
    }

    public static Uni<ShoppingCart> createShoppingCart(ShoppingCart shoppingCart) {
        return Panache
                .withTransaction(shoppingCart::persist)
                .replaceWith(shoppingCart)
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .transform(t -> new IllegalStateException(t));
    }

    public static Uni<ShoppingCart> addProductToShoppingCart(Long shoppingCartId, Long productId) {

        Uni<ShoppingCart> cart = findById(shoppingCartId);
        Uni<Set<ShoppingCartItem>> cartItemsUni = cart
                .chain(shoppingCart -> Mutiny.fetch(shoppingCart.cartItems)).onFailure().recoverWithNull();
        Uni<Product> productUni = Product.findByProductId(productId);
        Uni<ShoppingCartItem> item = ShoppingCartItem.findByCartIdByProductId(shoppingCartId, productId).toUni();

        Uni<Tuple4<ShoppingCart, Set<ShoppingCartItem>, ShoppingCartItem, Product>> responses = Uni.combine()
                .all().unis(cart, cartItemsUni, item, productUni).asTuple();

        return Panache
                .withTransaction(() -> responses
                        .onItem().ifNotNull()
                        .transform(entity -> {

                            if (entity.getItem1() == null || entity.getItem4() == null
                                    || entity.getItem2() == null) {
                                return null;
                            }

                            if (entity.getItem3() == null) {
                                ShoppingCartItem cartItem = ShoppingCartItem.builder()
                                        .cart(entity.getItem1())
                                        .product(entity.getItem4())
                                        .quantity(1)
                                        .build();
                                entity.getItem2().add(cartItem);
                            } else {
                                entity.getItem3().quantity++;
                            }
                            entity.getItem1().calculateCartTotal();
                            return entity.getItem1();
                        }));
    }

    public static Uni<ShoppingCart> deleteProductFromShoppingCart(Long shoppingCartId, Long productId) {

        Uni<ShoppingCart> cart = findById(shoppingCartId);
        Uni<Set<ShoppingCartItem>> cartItemsUni = cart
                .chain(shoppingCart -> Mutiny.fetch(shoppingCart.cartItems)).onFailure().recoverWithNull();

        Uni<Product> productUni = Product.findByProductId(productId);
        Uni<ShoppingCartItem> item = ShoppingCartItem.findByCartIdByProductId(shoppingCartId, productId).toUni();

        Uni<Tuple4<ShoppingCart, Set<ShoppingCartItem>, ShoppingCartItem, Product>> responses = Uni.combine()
                .all().unis(cart, cartItemsUni, item, productUni).asTuple();

        return Panache
                .withTransaction(() -> responses
                        .onItem().ifNotNull()
                        .transform(entity -> {
                            if (entity.getItem1() == null || entity.getItem4() == null
                                    || entity.getItem3() == null) {
                                return null;
                            }
                            entity.getItem3().quantity--;
                            if (entity.getItem3().quantity == 0) {
                                entity.getItem2().remove(entity.getItem3());
                            }
                            entity.getItem1().calculateCartTotal();
                            return entity.getItem1();
                        }));


    }

    public void calculateCartTotal() {
        cartTotal = cartItems.stream().mapToInt(ShoppingCartItem::getQuantity).sum();
    }

    public String toString() {
        return this.getClass().getSimpleName() + "<" + this.id + ">";
    }
}
