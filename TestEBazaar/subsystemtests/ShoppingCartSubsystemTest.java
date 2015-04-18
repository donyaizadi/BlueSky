package subsystemtests;

import integrationtests.BrowseAndSelectTest;

import java.util.logging.Logger;

import alltests.AllTests;
import junit.framework.TestCase;

public class ShoppingCartSubsystemTest extends TestCase {
	
	static String name = "Shopping Cart Subsystem Test";
	static Logger log = Logger.getLogger(ShoppingCartSubsystemTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
}
