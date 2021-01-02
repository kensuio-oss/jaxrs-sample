package com.hotjoe.services;

import com.hotjoe.services.dto.OrderDetailsView;
import com.hotjoe.services.logging.Logged;
import com.hotjoe.services.model.OrderDetails;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.List;
import java.util.logging.Logger;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/order-details")
public class OrderDetailsService {
    static final Logger logger = Logger.getLogger(OrderDetailsService.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("classicmodels");

    @Logged  // this request is logged
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

    @Logged  // this request is logged
    @GET
    @Path("/count/by-product")
    @Produces("application/json")
    public Response getCountByProductCode(@QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

        String query = "SELECT new com.hotjoe.services.dto.GroupCountDTO(product, count(*)) FROM OrderDetails orderdetail left outer join orderdetail.product product group by product.productCode";
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

}
