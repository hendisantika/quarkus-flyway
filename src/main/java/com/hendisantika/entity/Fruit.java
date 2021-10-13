package com.hendisantika.entity;


import io.quarkus.hibernate.reactive.panache.Panache;
import io.quarkus.hibernate.reactive.panache.PanacheEntityBase;
import io.quarkus.panache.common.Sort;
import io.smallrye.mutiny.Uni;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.Duration;
import java.util.Collections;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 * Project : quarkus-flyway
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 12/10/21
 * Time: 12.41
 */
@Entity
@Table(name = "fruit", schema = "product")
public class Fruit extends PanacheEntityBase {
    @Id
    @SequenceGenerator(
            name = "fruitSequence",
            sequenceName = "product.fruit_sequence",
            allocationSize = 1,
            initialValue = 1)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "fruitSequence")
    public Integer id;

    public String name;
    public String description;

    public static Uni<Fruit> findByFruitId(Long id) {
        return findById(id);
    }

    public static Uni<Fruit> updateFruit(Long id, Fruit fruit) {
        return Panache
                .withTransaction(() -> findByFruitId(id)
                        .onItem().ifNotNull()
                        .transform(entity -> {
                            entity.name = fruit.name;
                            entity.description = fruit.description;
                            return entity;
                        })
                        .onFailure().recoverWithNull());
    }

    public static Uni<Fruit> addFruit(Fruit fruit) {
        return Panache
                .withTransaction(fruit::persist)
                .replaceWith(fruit)
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .transform(t -> new IllegalStateException(t));
    }

    public static Uni<List<Fruit>> getAllFruits() {
        return Fruit
                .listAll(Sort.by("createdAt"))
                .ifNoItem()
                .after(Duration.ofMillis(10000))
                .fail()
                .onFailure()
                .recoverWithUni(Uni.createFrom().<List<PanacheEntityBase>>item(Collections.EMPTY_LIST));

    }

    public static Uni<Boolean> deleteFruit(Long id) {
        return Panache.withTransaction(() -> deleteById(id));
    }

    public String toString() {
        return this.getClass().getSimpleName() + "<" + this.id + ">";
    }
}
