package io.kensu.example.jboss;

import io.kensu.example.jboss.model.dto.ProductLineView;
import io.kensu.example.jboss.model.entities.Customer;
import io.kensu.example.jboss.model.entities.ProductLine;

import javax.persistence.*;
import javax.ws.rs.*;
import javax.ws.rs.core.Response;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Collectors;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/customers")
public class CustomerService {
    static final Logger logger = Logger.getLogger(CustomerService.class.getName());

    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence.createEntityManagerFactory("demodb");

    @GET
    @Path("/{loanId}")
    @Produces("application/json")
    public Response getProductLine(@PathParam("loanId") String loadId) throws WebApplicationException {
		EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();
            
    	String query = "SELECT c FROM Customer c WHERE c.Loan_ID = :loanId";
    	
    	// Issue the query and get a matching Customer
    	TypedQuery<Customer> tq = em.createQuery(query, Customer.class);
    	tq.setParameter("loanId", loadId);
    	
    	Customer customer = null;
    	try {
    		// Get matching customer object and output
    		customer = tq.getSingleResult();
    	}
    	catch(NoResultException ex) {
    		ex.printStackTrace();
    	}
    	finally {
    		em.close();
    	}
        
        return Response.ok(customer).build();
    }

	@GET
	@Path("/")
	@Produces("application/json")
	public Response getProductLines(@QueryParam("maxResults") Integer maxResults) throws WebApplicationException {
		EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

		String query = "SELECT c FROM Customer c";

		if (maxResults == null) {
			maxResults = 100;
		}
		// Issue the query and get a matching Customer
		TypedQuery<Customer> tq = em.createQuery(query, Customer.class).setMaxResults(maxResults);
		List<Customer> customers = null;
		try {
			// Get matching customer object and output
			customers = tq.getResultList();
		}
		catch(NoResultException ex) {
			ex.printStackTrace();
		}
		finally {
			em.close();
		}

		return Response.ok(customers).build();
	}


	@GET
	@Path("/big-ones")
	@Produces("application/json")
	public Response getBigOnes(@QueryParam("greaterThanAmount") Integer greaterThanAmount) throws WebApplicationException {
		EntityManager em = ENTITY_MANAGER_FACTORY.createEntityManager();

		String query = "SELECT c FROM Customer c where LoanAmount >= :greaterThanAmount";

		if (greaterThanAmount == null) {
			greaterThanAmount = 150;
		}
		// Issue the query and get a matching Customer
		TypedQuery<Customer> tq = em.createQuery(query, Customer.class)
										.setParameter("greaterThanAmount", greaterThanAmount);
		List<Customer> customers = null;
		try {
			// Get matching customer object and output
			customers = tq.getResultList();
		}
		catch(NoResultException ex) {
			ex.printStackTrace();
		}
		finally {
			em.close();
		}

		return Response.ok(customers).build();
	}


}
