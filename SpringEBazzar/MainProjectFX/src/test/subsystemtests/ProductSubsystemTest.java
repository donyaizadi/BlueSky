package test.subsystemtests;

import java.util.logging.Logger;
import business.exceptions.BackendException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;
import test.DbQueries;
import test.alltests.AllTests;
import junit.framework.TestCase;
import launch.Start;


public class ProductSubsystemTest extends TestCase{
	static String name = "Product Subsystem Test";
	static Logger log = Logger.getLogger(ProductSubsystemTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testsaveNewCatalog(){

		ProductSubsystem pss = (ProductSubsystem) Start.ctx.getBean("pss");
		String expectedName = "CatalogTest";
		Catalog catalog = ProductSubsystemFacade.createCatalog(1, expectedName); 
			Integer generatedId;
			try {
				generatedId = pss.saveNewCatalog(catalog);
				String retrievedName = DbQueries.getCatalogByIdSql(generatedId);
				assertTrue(retrievedName.equals(expectedName));
			} catch (BackendException e) {
				fail("Saved catalog naem doesn't match with retrieved one");
		    }			
	}

}
