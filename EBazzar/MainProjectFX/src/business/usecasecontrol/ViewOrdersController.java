
package business.usecasecontrol;

import java.util.ArrayList;
import java.util.List;

import presentation.data.OrderPres;
import business.BusinessConstants;
import business.SessionCache;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.externalinterfaces.OrderSubsystem;
import business.ordersubsystem.OrderImpl;
import business.ordersubsystem.OrderSubsystemFacade;



/**
 * @author pcorazza
 */
public enum ViewOrdersController   {
	INSTANCE;	

//	CustomerSubsystem custSubsystem = (CustomerSubsystem) SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
//	OrderSubsystem orderSubsystemObj = new OrderSubsystemFacade(custSubsystem.getCustomerProfile());
//	
	
	public List<Order> readOrders()
	{
			try {
				SessionCache cache = SessionCache.getInstance();
				CustomerSubsystem customerSub = (CustomerSubsystem) cache.get(BusinessConstants.CUSTOMER);
				OrderSubsystem orderSub = new OrderSubsystemFacade(customerSub.getCustomerProfile());
				List<Order> orders = orderSub.getOrderHistory();				
				return orders;
			} catch (BackendException e) {
				e.printStackTrace();
				return null;
			}
		
	
	}
		
}
