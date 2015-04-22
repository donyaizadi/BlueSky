package test.daotests;

import java.util.List;
import java.util.logging.Logger;

import junit.framework.TestCase;
import launch.Start;
import middleware.exceptions.DatabaseException;
import test.DbQueries;
import test.alltests.AllTests;
import business.customersubsystem.CustomerSubsystemFacade;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.ordersubsystem.IDbclassOrder;


public class DbClassOrderTest extends TestCase{
	static String name = "DB Class Order Test";
	static Logger log = Logger.getLogger(DbClassOrderTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testgetAllOrderIds(){	
		CustomerSubsystem css = new CustomerSubsystemFacade();
		CustomerProfile customPro = css.getGenericCustomerProfile();
		List<Integer> expectedOrderIds = DbQueries.readAllOrderIds(customPro);
		IDbclassOrder dbClass = Start.ctx.getBean(IDbclassOrder.class);
		try {
			List<Integer> orderIds = dbClass.getAllOrderIds(customPro);
			expectedOrderIds.removeAll(orderIds);
			assertTrue(expectedOrderIds.isEmpty());
		} catch (DatabaseException e) {
			fail("Order ids don't match");
		}
	}

}
