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
    

}
