
package business.ordersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

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



class DbClassOrder implements DbClass {
	private static final Logger LOG = 
		Logger.getLogger(DbClassOrder.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
    private String query;
    private String queryType;
    private final String GET_ORDER_ITEMS = "GetOrderItems";
    private final String GET_ORDER_IDS = "GetOrderIds";
    private final String GET_ORDER_DATA = "GetOrderData";
    private final String SUBMIT_ORDER = "SubmitOrder";
    private final String SUBMIT_ORDER_ITEM = "SubmitOrderItem";	
    private CustomerProfile custProfile;
    private Integer orderId;
    private List<Integer> orderIds;
    private List<OrderItem> orderItems;
    private OrderImpl orderData;
	private OrderItem orderItem;
    private Order order;  
    
    DbClassOrder(){}
    
    DbClassOrder(OrderImpl order){
        this.order = order;
    }
    
    DbClassOrder(OrderItem orderItem ){
        this.orderItem = orderItem;
    }
    
    DbClassOrder(CustomerProfile custProfile) {
    	this.custProfile = custProfile;
    }
    
    DbClassOrder(OrderImpl order, CustomerProfile custProfile){
        this(order);
        this.custProfile = custProfile;
    } 
    
    List<Integer> getAllOrderIds(CustomerProfile custProfile) throws DatabaseException {
        this.custProfile=custProfile;
        queryType = GET_ORDER_IDS;
        dataAccessSS.atomicRead(this);
        return Collections.unmodifiableList(orderIds);      
    }
    
    OrderImpl getOrderData(Integer orderId) throws DatabaseException {
    	queryType = GET_ORDER_DATA;
    	this.orderId=orderId;  
    	dataAccessSS.atomicRead(this);      	
        return orderData;
    }
    
    // Precondition: CustomerProfile has been set by the constructor
    void submitOrder(ShoppingCart shopCart) throws DatabaseException {
    	dataAccessSS.createConnection(this);
	    dataAccessSS.startTransaction();
	    try {   
	    	//First, get order id
	    	Integer orderId = submitOrderData();
	    	
			//Second, create order items, and 
			List<OrderItem> items = Util.cartItemsToOrderItems(shopCart.getCartItems(), orderId);
			for(OrderItem item:items){
				submitOrderItem(item);
			}
		    dataAccessSS.commit();
		    
	    } catch(DatabaseException e) {
        	dataAccessSS.rollback();
        	LOG.warning("Rolling back...");
        	throw (e);
        }  finally {
        	dataAccessSS.releaseConnection();
        }	
    }
	    
    
    /** This is part of the general submitOrder method */
	private Integer submitOrderData() throws DatabaseException {	
		queryType = SUBMIT_ORDER;
		//creation and release of connection handled by submitOrder
		return dataAccessSS.save();    
	}
	
	/** This is part of the general submitOrder method */
	private void submitOrderItem(OrderItem item) throws DatabaseException {
        this.orderItem = item;
        queryType=SUBMIT_ORDER_ITEM;
        //creation and release of connection handled by submitOrder
        dataAccessSS.save();        
	}
   
    public List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
        queryType = GET_ORDER_ITEMS;
        this.orderId=orderId;
    	dataAccessSS.atomicRead(this);
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
        else if(queryType.equals(SUBMIT_ORDER)) {
            buildSaveOrderQuery();
        }
        else if(queryType.equals(SUBMIT_ORDER_ITEM)) {
            buildSaveOrderItemQuery();
        }
    }
    
	private void buildSaveOrderQuery(){
        Address shipAddr = order.getShipAddress();
        Address billAddr = order.getBillAddress();
        CreditCard cc = order.getPaymentInfo();
        query = "INSERT into Ord "+
        "(orderid, custid, shipaddress1, shipcity, shipstate, shipzipcode, billaddress1, billcity, billstate,"+
           "billzipcode, nameoncard,  cardnum,cardtype, expdate, orderdate, totalpriceamount)" +
        "VALUES(NULL," + custProfile.getCustId() + ",'"+
                  shipAddr.getStreet()+"','"+
                  shipAddr.getCity()+"','"+
                  shipAddr.getState()+"','"+
                  shipAddr.getZip()+"','"+
                  billAddr.getStreet()+"','"+
                  billAddr.getCity()+"','"+
                  billAddr.getState()+"','"+
                  billAddr.getZip()+"','"+
                  cc.getNameOnCard()+"','"+
                  cc.getCardNum()+"','"+
                  cc.getCardType()+"','"+
                  cc.getExpirationDate()+"','"+
                  Util.localDateAsString(order.getOrderDate())+"',"+
                  order.getTotalPrice()+")";       
    }
	
    private void buildSaveOrderItemQuery(){
    	query = "INSERT INTO orderitem (orderitemid, orderid, productid, quantity, totalprice, shipmentcost, taxamount) " +
        		"VALUES (NULL, " + orderItem.getOrderId() + ", " + orderItem.getProductId() + ", '" + orderItem.getQuantity() + "', '" + 
        		orderItem.getTotalPrice() + "', '0','0')";
    }

    private void buildGetOrderDataQuery() {
        query = "SELECT orderdate, totalpriceamount FROM Ord WHERE orderid = " + orderId;     
    }
    
    private void buildGetOrderIdsQuery() {
        query = "SELECT orderid FROM Ord WHERE custid = "+custProfile.getCustId();     
    }
    
    private void buildGetOrderItemsQuery() {
        query = "SELECT * FROM OrderItem WHERE orderid = "+orderId; 
    }
    
    private void populateOrderItems(ResultSet rs) throws DatabaseException {
    	orderItems = new LinkedList<OrderItem>();
    	try {
			while(rs.next()){
				//Get Product Info From ProductSubsystem
				ProductSubsystem pss = new ProductSubsystemFacade();
				try {
					Product product = pss.getProductFromId(rs.getInt("productid"));
					OrderItem item= new OrderItemImpl(product.getProductName(), rs.getInt("quantity"), product.getUnitPrice());
					orderItems.add(item);
				} catch (BackendException e) {
					throw new DatabaseException(e);
				}
			}
		} catch (SQLException e) {
			throw new DatabaseException(e);
		}
    }
    
    private void populateOrderIds(ResultSet resultSet) throws DatabaseException {
        orderIds = new LinkedList<Integer>();
        try {
            while(resultSet.next()){
                orderIds.add(resultSet.getInt("orderid"));
            }
        }
        catch(SQLException e){
            throw new DatabaseException(e);
        }
    }
    
    private void populateOrderData(ResultSet resultSet) throws DatabaseException {
    	orderData = new OrderImpl();
        try {
            if(resultSet.next()){
            	orderData.setDate(Util.localDateForString(resultSet.getString("orderdate")));            
            }
        }
        catch(SQLException e){
        	throw new DatabaseException(e);
        }     	
    	
    }    
 
    public void populateEntity(ResultSet resultSet) throws DatabaseException {
        if(queryType.equals(GET_ORDER_ITEMS)){
            populateOrderItems(resultSet);
        }
        else if(queryType.equals(GET_ORDER_IDS)){
            populateOrderIds(resultSet);
        }
        else if(queryType.equals(GET_ORDER_DATA)){
        	populateOrderData(resultSet);
        }       
    }
    
    public String getDbUrl() {
    	DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
    }
    
    public String getQuery() {
        return query;
    }
     
    public void setOrderId(Integer orderId){
        this.orderId = orderId;       
    }   
}
