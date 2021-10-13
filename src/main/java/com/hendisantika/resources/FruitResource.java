package com.hendisantika.resources;

import com.hendisantika.entity.Fruit;
import io.smallrye.mutiny.Uni;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.net.URI;

import static javax.ws.rs.core.Response.Status.NOT_FOUND;
import static javax.ws.rs.core.Response.Status.OK;

/**
 * Created by IntelliJ IDEA.
 * Project : quarkus-flyway
 * User: hendisantika
 * Email: hendisantika@gmail.com
 * Telegram : @hendisantika34
 * Date: 12/10/21
 * Time: 12.47
 */

@Path("/fruits")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class FruitResource {
    @GET
    public Uni<Response> getFruitsList() {
        return Fruit.getAllFruits()
                .onItem().transform(products -> Response.ok(products))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @GET
    @Path("/{id}")
//    public Fruit get(@PathParam Integer id) {
//        Fruit f = Fruit.findById(id);
//        if (f != null) {
//            return f;
//        } else {
//            throw new NotFoundException("Unknown fruit id : " + id);
//        }
//    }
    public Uni<Response> getSingleFruit(@PathParam("id") Long id) {
        return Fruit.findByFruitId(id)
                .onItem().ifNotNull().transform(product -> Response.ok(product).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @POST
    @Transactional
//    public Collection<Fruit> add(Fruit fruit) {
//        fruit.id = null; //ignore id
//        Fruit.persist(fruit);
//        return Fruit.listAll();
//    }
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    public Uni<Response> add(Fruit fruit) {
        return Fruit.addFruit(fruit)
                .onItem().transform(id -> URI.create("/v1/fruits/" + id.id))
                .onItem().transform(uri -> Response.created(uri))
                .onItem().transform(Response.ResponseBuilder::build);
    }

    @Path("{id}")
    @Transactional
//    public Fruit update(Fruit fruit) {
//        Fruit fruitUpdated = Fruit.findById(fruit.id);
//        if (fruitUpdated != null) {
//            fruitUpdated.name = fruit.name;
//            fruitUpdated.description = fruit.description;
//            Fruit.persist(fruitUpdated);
//            return fruitUpdated;
//        } else {
//            throw new NotFoundException("Unknown fruit id : " + fruit.id);
//        }
//    }
    public Uni<Response> update(@PathParam("id") Long id, Fruit fruit) {
        if (fruit == null || fruit.description == null) {
            throw new WebApplicationException("Product description was not set on request.", 422);
        }
        return Fruit.updateFruit(id, fruit)
                .onItem().ifNotNull().transform(entity -> Response.ok(entity).build())
                .onItem().ifNull().continueWith(Response.ok().status(NOT_FOUND)::build);
    }

    @DELETE
    @Path("{id}")
    @Transactional
//    public void delete(Fruit fruit) {
//        Fruit f = Fruit.findById(fruit.id);
//        if (f != null) {
//            Fruit.deleteById(fruit.id);
//        } else {
//            throw new NotFoundException("Unknown fruit id : " + fruit.id);
//        }
//    }
    public Uni<Response> delete(@PathParam("id") Long id) {
        return Fruit.deleteFruit(id)
                .onItem().transform(entity -> !entity ? Response.serverError().status(NOT_FOUND).build()
                        : Response.ok().status(OK).build());
    }
}
