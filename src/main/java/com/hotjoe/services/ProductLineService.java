package com.hotjoe.services;

import java.util.Collection;
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

import com.hotjoe.services.model.Product;
import com.hotjoe.services.model.ProductLine;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.hotjoe.services.logging.Logged;

@org.eclipse.microprofile.opentracing.Traced
@Path("/v1/product-line")
public class ProductLineService {
    static final Logger logger = Logger.getLogger(ProductLineService.class.getName());
    private static final EntityManagerFactory ENTITY_MANAGER_FACTORY = Persistence
            .createEntityManagerFactory("classicmodels");

    @Logged  // this request is logged
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

    @Logged  // this request is logged
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
	
	
	public static class ProductView {
		@JsonProperty public final String productCode;
		@JsonProperty public final String productName;
		@JsonProperty public final String productScale;
		@JsonProperty public final String productVendor;
		@JsonProperty public final String productDescription;
		@JsonProperty public final Short  quantityInStock;
		@JsonProperty public final Double buyPrice;
		@JsonProperty public final Double MSRP;

		public ProductView(Product product) {
			this.productCode = product.getProductCode();
			this.productName = product.getProductName();
			this.productScale = product.getProductScale();
			this.productVendor = product.getProductVendor();
			this.productDescription = product.getProductDescription();
			this.quantityInStock = product.getQuantityInStock();
			this.buyPrice = product.getBuyPrice();
			this.MSRP = product.getMSRP();
		}
	}

	public static class ProductLineView {
		@JsonProperty public final String productLine;
		@JsonProperty public final String textDescription;
		@JsonProperty public final String htmlDescription;
		@JsonProperty public final Collection<ProductView> products;
		public ProductLineView(ProductLine productLine) {
			this.productLine = productLine.getProductLine();
			this.textDescription = productLine.getTextDescription();
			this.htmlDescription = productLine.getHtmlDescription();
			this.products = productLine.getProducts().stream().map(ProductView::new).collect(Collectors.toList());
		}
	}

}