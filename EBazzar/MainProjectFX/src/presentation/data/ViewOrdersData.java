package presentation.data;

import java.util.List;


import business.externalinterfaces.Order;
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
