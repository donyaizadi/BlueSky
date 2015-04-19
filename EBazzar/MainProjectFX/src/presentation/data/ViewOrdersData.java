package presentation.data;

import java.util.ArrayList;
import java.util.List;
import business.BusinessConstants;
import business.SessionCache;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderSubsystem;
import business.ordersubsystem.OrderImpl;
import business.ordersubsystem.OrderSubsystemFacade;
import business.usecasecontrol.ViewOrdersController;

import business.util.Util;

public enum ViewOrdersData {
	INSTANCE;
	private OrderPres selectedOrder;
	public OrderPres getSelectedOrder() {
		return selectedOrder;
	}
	public void setSelectedOrder(OrderPres so) {
		selectedOrder = so;
	}
	
	public List<OrderPres> getOrders() {
		List<Order> list = ViewOrdersController.INSTANCE.readOrders();		
		return Util.orderListToOrderPresList(list);
	}
	
	

	
}
