package daotests;

import integrationtests.BrowseAndSelectTest;

import java.util.logging.Logger;

import alltests.AllTests;
import junit.framework.TestCase;

public class DBClassShoppingCartTest extends TestCase {
	static String name = "DB Class Shopping Cart Test";
	static Logger log = Logger.getLogger(BrowseAndSelectTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testRetrieveSavedCart(){
		
	}
}
