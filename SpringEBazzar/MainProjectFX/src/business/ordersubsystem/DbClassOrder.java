
package business.ordersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;







import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import launch.Start;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;
import business.exceptions.BackendException;
import business.externalinterfaces.Address;
//import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.productsubsystem.ProductSubsystemFacade;
import business.util.Util;


@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
@Repository
class DbClassOrder implements IDbclassOrder{
	
	private static final Logger LOG = Logger.getLogger(DbClassOrder.class.getPackage().getName());

	private JdbcTemplate jdbcTemplate;
    private String query;
    private String queryType;
    private final String GET_ORDER_ITEMS = "GetOrderItems";
    private final String GET_ORDER_IDS = "GetOrderIds";
    private final String GET_ORDER_DATA = "GetOrderData";
    private final String SUBMIT_ORDER_ITEM = "SubmitOrderItem";	
    private CustomerProfile custProfile;
    private Integer orderId;
    private List<Integer> orderIds;
    private List<OrderItemImpl> orderItems;
    private OrderImpl orderData;
	private OrderItem orderItem;
    private Order order;  
    
    DbClassOrder(){}
    
//    DbClassOrder(OrderImpl order){
//        this.order = order;
//    }
//    
//    DbClassOrder(OrderItem orderItem ){
//        this.orderItem = orderItem;
//    }
//    
//    DbClassOrder(CustomerProfile custProfile) {
//    	this.custProfile = custProfile;
//    }
//    
//    DbClassOrder(OrderImpl order, CustomerProfile custProfile){
//        this(order);
//        this.custProfile = custProfile;
//    } 
    
//    public void setOrderandCustProfile(OrderImpl order, CustomerProfile custProfile){
//        this.order = order;
//        this.custProfile = custProfile;    	
//    }
    
    @Inject
    @Named("dataSourceAccounts")
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
    
    class OrderRowMapper implements RowMapper<OrderImpl> {
		public OrderImpl mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			OrderImpl order = new OrderImpl();
			order.setDate(Util.localDateForString(resultSet.getString("orderdate")));
			return order;
		}
    }
    
    class OrderItemRowMapper implements RowMapper<OrderItemImpl> {
		public OrderItemImpl mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			OrderItemImpl item = new OrderItemImpl();
			item.setOrderId(orderId);
			item.setQuantity(resultSet.getInt("quantity"));
			item.setProductId(resultSet.getInt("productid"));
			
			// productName and UnitPrice should retrieve from product ss with the productId
//			ProductSubsystem pss = new ProductSubsystemFacade();
			ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");

			try {
				Product product = pss.getProductFromId(item.getProductId());
				item.setProductName(product.getProductName());
				item.setUnitPrice(product.getUnitPrice());
			} catch (BackendException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			 
			return item;
		}
    }    
	
    @Transactional(value = "txManagerAccounts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
	public List<Integer> getAllOrderIds(CustomerProfile custProfile) throws DatabaseException {
    	orderIds = new ArrayList<Integer>();
    	this.custProfile=custProfile;
        queryType = GET_ORDER_IDS;
        buildQuery();
        try{
        	orderIds = jdbcTemplate.queryForList(query, Integer.class);
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for GET_ORDER_IDS with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        return Collections.unmodifiableList(orderIds);      
    }
    
    @Transactional(value = "txManagerAccounts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
    public OrderImpl getOrderData(Integer orderId) throws DatabaseException {
    	orderData = new OrderImpl();
    	queryType = GET_ORDER_DATA;
    	this.orderId=orderId;
    	buildQuery();
    	try{
    		String orderDate = jdbcTemplate.queryForObject(query, String.class);
    		orderData.setDate(Util.localDateForString(orderDate));
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for GET_ORDER_DATA with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}

        return orderData;
    }
    
    // Precondition: CustomerProfile has been set by the constructor
    @Transactional(value = "txManagerAccounts", propagation=Propagation.REQUIRES_NEW, readOnly=false)
    public void submitOrder(ShoppingCart shopCart,OrderImpl order, CustomerProfile custProfile) throws DatabaseException {
    	this.order = order;
    	this.custProfile = custProfile;
	    try {   
	    	//First, get order id
	    	Integer orderId = submitOrderData();
			//Second, create order items, and 
			List<OrderItem> items = Util.cartItemsToOrderItems(shopCart.getCartItems(), orderId);
			for(OrderItem item:items){
				submitOrderItem(item);
			}	   
	    } catch(Exception e) {
	    	LOG.warning("Rolling back...");
	    }
   }
    
    /** This is part of the general submitOrder method */
	private Integer submitOrderData() throws DatabaseException {	
		Integer orderId;
		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcInsert.withTableName("Ord").usingGeneratedKeyColumns("orderid");
        
		Address shipAddr = order.getShipAddress();
        Address billAddr = order.getBillAddress();
        CreditCard cc = order.getPaymentInfo();
        
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("custid", custProfile.getCustId());
        parameters.put("shipaddress1", shipAddr.getStreet());
        parameters.put("shipcity", shipAddr.getCity());
        parameters.put("shipstate", shipAddr.getState());
        parameters.put("shipzipcode", shipAddr.getZip());
        parameters.put("billaddress1", billAddr.getStreet());
        parameters.put("billcity", billAddr.getCity());
        parameters.put("billstate", billAddr.getState());
        parameters.put("billzipcode", billAddr.getZip());
        parameters.put("nameoncard", cc.getNameOnCard());
        parameters.put("cardnum", cc.getCardNum());
        parameters.put("cardtype", cc.getCardType());
        parameters.put("expdate", cc.getExpirationDate());
        parameters.put("orderdate", Util.localDateAsString(order.getOrderDate()));
        parameters.put("totalpriceamount", order.getTotalPrice());
        
        try{
        	// execute insert
        	Number key = jdbcInsert.executeAndReturnKey(parameters);
        	orderId = ((Number) key).intValue();
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for Save Order Data and retrieve orderId");
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        return orderId;
	}
	
	/** This is part of the general submitOrder method */
	private void submitOrderItem(OrderItem item) throws DatabaseException {
        this.orderItem = item;
        queryType=SUBMIT_ORDER_ITEM;
        buildQuery();
        try{
        	jdbcTemplate.update(query);
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for SUBMIT_ORDER_ITEM with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}

	}
   
	@Transactional(value = "txManagerAccounts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
	public List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
        queryType = GET_ORDER_ITEMS;
        this.orderId=orderId;
        buildQuery();
        try{
        	orderItems = jdbcTemplate.query(query, new OrderItemRowMapper());
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for GET_ORDER_ITEMS with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        	
        return Collections.unmodifiableList(orderItems);        
    }
    
    
    public void buildQuery() {
        if(queryType.equals(GET_ORDER_ITEMS)) {
            buildGetOrderItemsQuery();
        }
        else if(queryType.equals(GET_ORDER_IDS)) {
            buildGetOrderIdsQuery();
        }
        else if(queryType.equals(GET_ORDER_DATA)) {
        	buildGetOrderDataQuery();
        }
//        else if(queryType.equals(SUBMIT_ORDER)) {
//            buildSaveOrderQuery();
//        }
        else if(queryType.equals(SUBMIT_ORDER_ITEM)) {
            buildSaveOrderItemQuery();
        }
    }

    private void buildSaveOrderItemQuery(){
    	query = "INSERT INTO orderitem (orderitemid, orderid, productid, quantity, totalprice, shipmentcost, taxamount) " +
        		"VALUES (NULL, " + orderItem.getOrderId() + ", " + orderItem.getProductId() + ", '" + orderItem.getQuantity() + "', '" + 
        		orderItem.getTotalPrice() + "', '0','0')";
    }

    private void buildGetOrderDataQuery() {
//        query = "SELECT orderdate, totalpriceamount FROM Ord WHERE orderid = " + orderId;     
        query = "SELECT orderdate FROM Ord WHERE orderid = " + orderId;     
    }
    
    private void buildGetOrderIdsQuery() {
        query = "SELECT orderid FROM Ord WHERE custid = "+custProfile.getCustId();     
    }
    
    private void buildGetOrderItemsQuery() {
        query = "SELECT * FROM OrderItem WHERE orderid = "+orderId; 
    }
    
    public String getQuery() {
        return query;
    }
     
    public void setOrderId(Integer orderId){
        this.orderId = orderId;       
    }

}
