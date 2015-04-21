package test.subsystemtests;



import java.util.logging.Logger;

import test.DbQueries;
import test.alltests.AllTests;
import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import junit.framework.TestCase;
import launch.Start;

public class ShoppingCartSubsystemTest extends TestCase {
	
	static String name = "Shopping Cart Subsystem Test";
	static Logger log = Logger.getLogger(ShoppingCartSubsystemTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testRetrieveSavedCart(){
		int idExpected = DbQueries.readIdShoppingCart();
		ShoppingCartSubsystem scs = (ShoppingCartSubsystem) Start.ctx.getBean("scss");
		CustomerSubsystem css = (CustomerSubsystem) Start.ctx.getBean("css");
		scs.setCustomerProfile(css.getGenericCustomerProfile());
		try {
			scs.retrieveSavedCart();
			scs.makeSavedCartLive();
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		ShoppingCart liveCart = scs.getLiveCart();
		int idReturned = Integer.parseInt(liveCart.getId());
		assertTrue(idExpected == idReturned);
	}
}
