package com.hotjoe.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.NoResultException;
import javax.persistence.TypedQuery;
import javax.persistence.Persistence;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

import com.hotjoe.services.model.Product;
import com.hotjoe.services.model.Visit;

import org.hibernate.HibernateException;
import org.hibernate.engine.spi.SessionFactoryImplementor;
import org.hibernate.event.service.spi.EventListenerRegistry;
import org.hibernate.event.spi.EventType;
import org.hibernate.event.spi.InitializeCollectionEvent;
import org.hibernate.event.spi.InitializeCollectionEventListener;

import com.hotjoe.services.exception.BadRequestServiceException;
import com.hotjoe.services.exception.ConflictServiceException;
import com.hotjoe.services.logging.Logged;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/visit")
public class VisitService {
    static final Logger logger = Logger.getLogger(VisitService.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("tutorialapp");

    // public static class KensuInitializeCollectionEventListener implements InitializeCollectionEventListener {
    //     static final Logger logger = Logger.getLogger(KensuInitializeCollectionEventListener.class.getName());

    //     /**
    //      *
    //      */
    //     private static final long serialVersionUID = -1330483903172879658L;

    //     @Override
    //     public void onInitializeCollection(InitializeCollectionEvent event) throws HibernateException {
    //         logger.log(Level.WARNING, "onInitializeCollection HIBERNATE:" + event);            
    //     }
        
    // }

    // static {
    //     InitializeCollectionEventListener initializeCollectionEventListener = new KensuInitializeCollectionEventListener();
    //     SessionFactoryImplementor sessionFactory = ENTITY_MANAGER_FACTORY.unwrap( SessionFactoryImplementor.class );
    //     EventListenerRegistry registry = ((SessionFactoryImplementor) sessionFactory).getServiceRegistry().getService( EventListenerRegistry.class );
	// 	registry.setListeners( EventType.INIT_COLLECTION, initializeCollectionEventListener );
    // }
    
    // static {
    //     SessionFactoryImplementor sessionFactory = ENTITY_MANAGER_FACTORY.unwrap( SessionFactoryImplementor.class );
    //     sessionFactory
    //         .getServiceRegistry()
    //         .getService(org.hibernate.event.service.spi.EventListenerRegistry.class )
    //         .prependListeners(org.hibernate.event.spi.EventType.LOAD, new KensuLoadEntityListener() );
    // }

    @Logged  // this request is logged
    @GET
    @Path("/{visitId}")
    @Produces("application/json")
    public Response getVisit(@PathParam("visitId") Long visitId) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
    	String query = "SELECT c FROM Visit c WHERE c.visitId = :visitId";
    	
    	// Issue the query and get a matching Customer
    	TypedQuery<Visit> tq = em.createQuery(query, Visit.class);
    	tq.setParameter("visitId", visitId);
    	
    	Visit visit = null;
    	try {
    		// Get matching customer object and output
    		visit = tq.getSingleResult();
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(visit).build();
    }

    @Logged  // this request is logged
    @GET
    @Path("/")
    @Produces("application/json")
    public Response getVisits(@QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
        EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
        String query = "SELECT c FROM Visit c";
        
        if (maxResults == null) {
            maxResults = 100;
        }
    	
    	// Issue the query and get a matching Customer
        TypedQuery<Visit> tq = em.createQuery(query, Visit.class).setMaxResults(maxResults);
        
    	
    	List<Visit> visits = null;
    	try {
    		// Get matching customer object and output
    		visits = tq.getResultList();
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(visits).build();
    }

    // @Logged  // this request is logged
    // @POST
    // @Path("/")
    // @Consumes("application/json")
    // @Produces("application/json")
    // public Response createProduct(Product product) throws WebApplicationException {

    //     logger.log(Level.FINE, "got a create product request");

    //     if(product == null)
    //         throw new BadRequestServiceException("record body is missing");

    //     if( product.getProductId() == null )
    //         product.setProductId( ThreadLocalRandom.current().nextInt(10000, 1000000 + 1));
    //     else if( products.containsKey(product.getProductId()))
    //         throw new ConflictServiceException("product id " + product.getProductId() + " already exists");

    //     //
    //     // arbitrary business rule to show other exceptions - record description can't have dashes.
    //     //
    //     if( (product.getDescription() != null) && product.getDescription().contains("-"))
    //         throw new BadRequestServiceException("record description " + product.getDescription() +
    //                 " is invalid. it cannot contain the minus sign (dash) character");

    //     logger.log(Level.INFO, "Created product record with description \"" + product.getDescription() + "\"");

    //     products.put(product.getProductId(), product);

    //     return Response.status(Response.Status.CREATED).entity(product).build();
    // }
}
