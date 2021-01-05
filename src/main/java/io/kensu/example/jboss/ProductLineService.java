package io.kensu.example.jboss;

import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.Persistence;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import io.kensu.example.jboss.dto.ProductLineView;
import io.kensu.example.jboss.model.ProductLine;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/product-line")
public class ProductLineService {
    static final Logger logger = Logger.getLogger(ProductLineService.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("classicmodels");

    @GET
    @Path("/{productLine}")
    @Produces("application/json")
    public Response getProductLine(@PathParam("productLine") String productLine) throws WebApplicationException {
		EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
    	String query = "SELECT p FROM ProductLine p WHERE p.productLine = :productLine";
    	
    	// Issue the query and get a matching Customer
    	TypedQuery<ProductLine> tq = em.createQuery(query, ProductLine.class);
    	tq.setParameter("productLine", productLine);
    	
    	ProductLineView productLineView = null;
    	try {
    		// Get matching customer object and output
    		productLineView = new ProductLineView(tq.getSingleResult());
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(productLineView).build();
    }

    @GET
    @Path("/")
    @Produces("application/json")
    public Response getProductLines(@QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
        String query = "SELECT p FROM ProductLine p";
        
        if (maxResults == null) {
            maxResults = 100;
        }    	
    	// Issue the query and get a matching Customer
        TypedQuery<ProductLine> tq = em.createQuery(query, ProductLine.class).setMaxResults(maxResults);
    	List<ProductLineView> productLines = null;
    	try {
    		// Get matching customer object and output
    		productLines = tq.getResultList().stream().map(ProductLineView::new).collect(Collectors.toList());
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(productLines).build();
    }


}
