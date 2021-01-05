package io.kensu.example.jboss;

import io.kensu.example.jboss.model.dto.OrderDetailsView;
import io.kensu.example.jboss.model.entities.OrderDetails;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/order-details")
public class OrderDetailsService {
    static final Logger logger = Logger.getLogger(OrderDetailsService.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("classicmodels");

    @GET
    @Path("/{orderNumber}")
    @Produces("application/json")
    public Response getOrderDetails(@PathParam("orderNumber") Integer orderNumber) throws WebApplicationException {
		EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
    	String query = "SELECT o FROM OrderDetails o WHERE o.orderNumber = :orderNumber";
    	
    	// Issue the query and get a matching Customer
    	TypedQuery<OrderDetails> tq = em.createQuery(query, OrderDetails.class);
    	tq.setParameter("orderNumber", orderNumber);

		OrderDetailsView orderDetailsView = null;
    	try {
    		// Get matching customer object and output
			orderDetailsView = new OrderDetailsView(tq.getSingleResult());
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(orderDetailsView).build();
    }

    @GET
    @Path("/count/by-product")
    @Produces("application/json")
    public Response getCountByProductCode(@QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT new io.kensu.example.jboss.dto.GroupCountDTO(product, count(*)) FROM OrderDetails orderdetail left outer join orderdetail.product product group by product.productCode";
        if (maxResults == null) {
            maxResults = 100;
        }    	
    	// Issue the query and get a matching Customer
		Query tq = em.createQuery(query).setMaxResults(maxResults);
    	List productLineGroupCountView = null;
    	try {
			productLineGroupCountView = tq.getResultList();
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(productLineGroupCountView).build();
    }

    @GET
    @Path("/product-line/{productLine}")
    @Produces("application/json")
    public Response findForProductLine(@PathParam("productLine") String productLine, @QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
		String query = "SELECT p FROM OrderDetails p left join p.product as pp left join pp.productLine as ppp WHERE ppp.productLine = :productLine";

        if (maxResults == null) {
            maxResults = 100;
        }
		TypedQuery<OrderDetails> tq = em.createQuery(query, OrderDetails.class).setMaxResults(maxResults).setParameter("productLine", productLine);
    	List resultView = null;
    	try {
			resultView = tq.getResultList().stream().map(OrderDetailsView::new).collect(Collectors.toList());
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}

        return Response.ok(resultView).build();
    }

}
