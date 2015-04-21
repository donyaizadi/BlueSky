package test.daotests;



import java.util.List;
import java.util.logging.Logger;

import test.DbQueries;
import test.alltests.AllTests;
import business.customersubsystem.CustomerSubsystemFacade;
import business.customersubsystem.IDbClassAddress;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.DbClassAddressForTest;
import junit.framework.TestCase;
import launch.Start;

public class DbClassAddressTest extends TestCase {
	
	static String name = "Db Class Address Test";
	static Logger log = Logger.getLogger(DbClassAddressTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	
	public void testReadAllAddresses() {
		List<Address> expected = DbQueries.readCustAddresses();
		
		//test real dbclass address
		CustomerSubsystem css = Start.ctx.getBean(CustomerSubsystem.class);
		IDbClassAddress dbclass = Start.ctx.getBean(IDbClassAddress.class);
		CustomerProfile custProfile = css.getGenericCustomerProfile();
		try {
			dbclass.readAllAddresses(custProfile);
			List<Address> found = dbclass.getAddressList();
			assertTrue(expected.size() == found.size());
			
		} catch(Exception e) {
			fail("Address Lists don't match");
		}
		
	}
}
