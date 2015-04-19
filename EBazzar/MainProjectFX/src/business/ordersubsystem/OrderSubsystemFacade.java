package business.ordersubsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import middleware.exceptions.DatabaseException;
import business.exceptions.BackendException;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.ShoppingCart;

public class OrderSubsystemFacade implements OrderSubsystem {
	private static final Logger LOG = 
			Logger.getLogger(OrderSubsystemFacade.class.getPackage().getName());
	CustomerProfile custProfile;
	    
    public OrderSubsystemFacade(CustomerProfile custProfile){
        this.custProfile = custProfile;
    }
	
	/** Used whenever an order item needs to be created from outside the order subsystem */
    public static OrderItem createOrderItem(Integer prodId, String prodName, Integer orderId, String quantityReq, String totalPrice) {
    	OrderItem orderItem = new OrderItemImpl(prodName, Integer.valueOf(quantityReq), Double.valueOf(totalPrice));
    	orderItem.setProductId(prodId);
    	orderItem.setOrderId(orderId);
    	return orderItem;
    }
    
    /** to create an Order object from outside the subsystem */
    public static Order createOrder(Integer orderId, String orderDate, String totalPrice) {
    	return null;
    }
    
    ///////////// Methods internal to the Order Subsystem -- NOT public
    List<Integer> getAllOrderIds() throws DatabaseException {
        
        DbClassOrder dbClass = new DbClassOrder();
        return dbClass.getAllOrderIds(custProfile);
        
    }
    List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
        DbClassOrder dbClass = new DbClassOrder();
        return dbClass.getOrderItems(orderId);
    }
    
    OrderImpl getOrderData(Integer orderId) throws DatabaseException {
    	DbClassOrder dbClass = new DbClassOrder();
    	return dbClass.getOrderData(orderId);
    }
    
    OrderImpl createOrderData(ShoppingCart shopCart){
    	OrderImpl orderData = new OrderImpl();
		orderData.setDate(LocalDate.now()); // Current Date
		orderData.setBillAddress(shopCart.getBillingAddress());
		orderData.setShipAddress(shopCart.getShippingAddress());
		orderData.setPaymentInfo(shopCart.getPaymentInfo());
		return orderData;
    }

	@Override
	public List<Order> getOrderHistory() throws BackendException {
		// TODO Auto-generated method stub
		List<Order> orders = new ArrayList<Order>();
        try {
			List<Integer> orderIds = getAllOrderIds();
			for (int i=0; i<orderIds.size(); i++){
				OrderImpl order = getOrderData(orderIds.get(i));
				order.setOrderId(orderIds.get(i));
				order.setOrderItems(getOrderItems(orderIds.get(i)));
				orders.add(order);
			}
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			throw new BackendException(e);
			
		}
        LOG.info(custProfile.getCustId()+"requests Order History...");
		return orders;
	}

	@Override
	public void submitOrder(ShoppingCart shopCart) throws BackendException {
		// TODO Auto-generated method stub
		OrderImpl orderData = createOrderData(shopCart);
		DbClassOrder db = new DbClassOrder(orderData, custProfile);
		try {
			db.submitOrder(shopCart);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
		LOG.info(custProfile.getCustId()+" submits his order with order id = "+orderData.getOrderId() +"on"+ orderData.getOrderDate());
	}
}
