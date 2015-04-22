package business.ordersubsystem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;

import org.springframework.stereotype.Service;

import middleware.exceptions.DatabaseException;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;

@Service("orderss")
public class OrderSubsystemFacade implements OrderSubsystem {
	private static final Logger LOG = Logger
			.getLogger(OrderSubsystemFacade.class.getPackage().getName());
	CustomerProfile custProfile;

	@Inject
	private IDbclassOrder dbClass;
	
	@Inject()
	ShoppingCartSubsystem scss;

	public OrderSubsystemFacade() {
	}

	public OrderSubsystemFacade(CustomerProfile custProfile) {
		this.custProfile = custProfile;
	}

	public void setCustomerProfile(CustomerProfile custProfile) {
		this.custProfile = custProfile;
	}

	/**
	 * Used whenever an order item needs to be created from outside the order
	 * subsystem
	 */
	public static OrderItem createOrderItem(Integer prodId, String prodName,
			Integer orderId, String quantityReq, String totalPrice) {
		OrderItem orderItem = new OrderItemImpl(prodName,
				Integer.valueOf(quantityReq), Double.valueOf(totalPrice));
		orderItem.setProductId(prodId);
		orderItem.setOrderId(orderId);
		return orderItem;
	}

	/** to create an Order object from outside the subsystem */
	public static Order createOrder(Integer orderId, String orderDate,
			String totalPrice) {
		return null;
	}

	 /////////// Methods internal to the Order Subsystem -- NOT public
	List<Integer> getAllOrderIds() throws DatabaseException {
		// DbClassOrder dbClass = new DbClassOrder();
		return dbClass.getAllOrderIds(custProfile);
	}

	List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException {
		// DbClassOrder dbClass = new DbClassOrder();
		return dbClass.getOrderItems(orderId);
	}

	OrderImpl getOrderData(Integer orderId) throws DatabaseException {
		// DbClassOrder dbClass = new DbClassOrder();
		return dbClass.getOrderData(orderId);
	}

	OrderImpl createOrderData(ShoppingCart shopCart) {
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
		try {
			// DbClassOrder dbClass = new DbClassOrder();
			List<Order> orders = new ArrayList<Order>();
			List<Integer> orderIds = dbClass.getAllOrderIds(custProfile);
			for (int orderId : orderIds) {
				List<OrderItem> orderItems = dbClass.getOrderItems(orderId);
				OrderImpl order = dbClass.getOrderData(orderId);
				order.setOrderItems(orderItems);
				order.setOrderId(orderId);
				orders.add(order);
			}
			return orders;
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
	}

	@Override
	public void submitOrder(ShoppingCart shopCart) throws BackendException {
		// TODO Auto-generated method stub
		OrderImpl orderData = createOrderData(shopCart);
		// DbClassOrder db = new DbClassOrder(orderData, custProfile);
		// dbClass.setOrderandCustProfile(orderData, custProfile);
		try {
			dbClass.submitOrder(shopCart, orderData, custProfile);
			scss.clearLiveCart();

		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
		LOG.info(custProfile.getCustId()
				+ " submits his order with order id = "
				+ orderData.getOrderId() + "on" + orderData.getOrderDate());
	}
}
