package daotests;

import integrationtests.BrowseAndSelectTest;

import java.util.logging.Logger;

import middleware.exceptions.DatabaseException;
import dbsetup.DbQueries;
import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.DbClassShoppingCartForTest;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;
import alltests.AllTests;
import junit.framework.TestCase;

public class DBClassShoppingCartTest extends TestCase {
	static String name = "DB Class Shopping Cart Test";
	static Logger log = Logger.getLogger(BrowseAndSelectTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testRetrieveSavedCart(){	
		int expectedId = DbQueries.readIdShoppingCart();
		CustomerSubsystem css = new CustomerSubsystemFacade();
		DbClassShoppingCartForTest dbClass = ShoppingCartSubsystemFacade.createTestCartDBClass();
		CustomerProfile customPro = css.getGenericCustomerProfile();
		try {
			ShoppingCart retrievedShoppingCart = dbClass.retrieveSavedCart(customPro);
			int idRetrieved = Integer.parseInt(retrievedShoppingCart.getId());
			assertTrue(expectedId == idRetrieved);
		} catch (DatabaseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
