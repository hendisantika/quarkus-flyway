package com.hendisantika.resources;

import com.hendisantika.entity.Fruit;
import org.jboss.resteasy.annotations.jaxrs.PathParam;

import javax.transaction.Transactional;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;

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
    public Collection<Fruit> list() {
        return Fruit.listAll();
    }

    @GET
    @Path("/{id}")
    public Fruit get(@PathParam Integer id) {
        Fruit f = Fruit.findById(id);
        if (f != null) {
            return f;
        } else {
            throw new NotFoundException("Unknown fruit id : " + id);
        }
    }

    @POST
    @Transactional
    public Collection<Fruit> add(Fruit fruit) {
        fruit.id = null; //ignore id
        Fruit.persist(fruit);
        return Fruit.listAll();
    }
}
