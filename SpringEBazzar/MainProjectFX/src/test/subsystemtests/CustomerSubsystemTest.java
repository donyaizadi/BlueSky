package test.subsystemtests;

import java.util.logging.Logger;

import org.mockito.internal.util.reflection.Whitebox;

import junit.framework.TestCase;
import launch.Start;
import business.exceptions.BackendException;
import business.externalinterfaces.CustomerSubsystem;
import test.DbQueries;
import test.alltests.AllTests;

public class CustomerSubsystemTest extends TestCase {

	static String name = "Customer Subsystem Testt";
	static Logger log = Logger.getLogger(CustomerSubsystemTest.class.getName());

	static {
		AllTests.initializeProperties();
	}

	public void testCustomerProfile() {
		CustomerSubsystem css = (CustomerSubsystem) Start.ctx.getBean("css");
		Whitebox.setInternalState(css, "customerProfile",
				css.getGenericCustomerProfile());
		try {
			css.initializeCustomer(css.getGenericCustomerProfile().getCustId(),
					1);
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String shipAddress = DbQueries.readShipmentAddresses(css
				.getCustomerProfile().getCustId());
		
		assertTrue(css.getDefaultShippingAddress().getStreet().equals(shipAddress));
		// String defaultAddress = css.getDefaultShippingAddress().getStreet();

	}

}
