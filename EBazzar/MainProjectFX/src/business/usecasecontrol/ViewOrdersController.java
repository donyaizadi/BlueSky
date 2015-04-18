
package business.usecasecontrol;

import java.util.List;

import business.BusinessConstants;
import business.SessionCache;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.ordersubsystem.OrderSubsystemFacade;



/**
 * @author pcorazza
 */
public enum ViewOrdersController   {
	INSTANCE;	

	CustomerSubsystem custSubsystem = (CustomerSubsystem) SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
	OrderSubsystem orderSubsystemObj = new OrderSubsystemFacade(custSubsystem.getCustomerProfile());
	
	
	public List<Order> readOrders()
	{
		try {
			return orderSubsystemObj.getOrderHistory();
		} catch (BackendException e) {
			e.printStackTrace();
		}
		return null;
	}
		
}
