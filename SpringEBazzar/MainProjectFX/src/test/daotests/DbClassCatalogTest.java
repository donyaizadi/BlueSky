package test.daotests;

import java.util.logging.Logger;

import business.productsubsystem.IDbClassCatalog;
import launch.Start;
import middleware.exceptions.DatabaseException;
import test.DbQueries;
import test.alltests.AllTests;
import junit.framework.TestCase;

public class DbClassCatalogTest extends TestCase{
	static String name = "DB Class Catalog Test";
	static Logger log = Logger.getLogger(DbClassCatalogTest.class.getName());
	
	static {
		AllTests.initializeProperties();
	}
	
	public void testsaveNewCatalog() {
		String catalogName = "testCatalog";
		IDbClassCatalog dbClass = Start.ctx.getBean(IDbClassCatalog.class);
		try {
			Integer generatedId = dbClass.saveNewCatalog(catalogName);
			String retrievedName = DbQueries.getCatalogByIdSql(generatedId);
			assertTrue(retrievedName.equals(catalogName));
		} catch (DatabaseException e) {
			fail("Saved catalog naem doesn't match with retrieved one");
		}
	}
}
