package business.ordersubsystem;

import java.util.List;

import javax.sql.DataSource;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.ShoppingCart;

public interface IDbclassOrder {
	public void setDataSource(DataSource dataSource);
	public List<Integer> getAllOrderIds(CustomerProfile custProfile) throws DatabaseException;
	public void submitOrder(ShoppingCart shopCart, OrderImpl order, CustomerProfile custProfile) throws DatabaseException;
	public OrderImpl getOrderData(Integer orderId) throws DatabaseException;
	public List<OrderItem> getOrderItems(Integer orderId) throws DatabaseException;
	public String getQuery();
}
