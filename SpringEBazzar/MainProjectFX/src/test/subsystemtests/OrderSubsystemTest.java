package test.subsystemtests;

import java.util.List;
import java.util.logging.Logger;

import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderSubsystem;
import test.DbQueries;
import test.alltests.AllTests;
import junit.framework.TestCase;
import launch.Start;


public class OrderSubsystemTest extends TestCase{
	static String name = "Order Subsystem Test";
	static Logger log = Logger.getLogger(OrderSubsystemTest.class.getName());
	

	static {
		AllTests.initializeProperties();
	}
	
	public void testGetOrderHistory() {
		CustomerSubsystem css = new CustomerSubsystemFacade();
		CustomerProfile customPro = css.getGenericCustomerProfile();		
		List<Order> expectedOrders = DbQueries.readAllOrders(customPro);
		OrderSubsystem oss = (OrderSubsystem) Start.ctx.getBean("orderss");
		oss.setCustomerProfile(css.getGenericCustomerProfile());
		try {
			List<Order> orders = oss.getOrderHistory();
			assertTrue(expectedOrders.size() == orders.size());
		} catch (BackendException e) {
			fail("Orders don't match");
		}
		
	}
}
