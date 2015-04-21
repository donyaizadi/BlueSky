package subsystemtests;

import integrationtests.BrowseAndSelectTest;

import java.util.logging.Logger;

import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import dbsetup.DbQueries;
import alltests.AllTests;
import junit.framework.TestCase;

public class ShoppingCartSubsystemTest extends TestCase {
	
	static String name = "Shopping Cart Subsystem Test";
	static Logger log = Logger.getLogger(ShoppingCartSubsystemTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testRetrieveSavedCart(){
		int idExpected = DbQueries.readIdShoppingCart();
		ShoppingCartSubsystem scs = ShoppingCartSubsystemFacade.INSTANCE;
		CustomerSubsystem css = new CustomerSubsystemFacade();
		scs.setCustomerProfile(css.getCustomerProfile());

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
