package alltests;

import java.io.File;
import java.util.logging.Logger;

import business.externalinterfaces.RulesConfigProperties;

import middleware.DbConfigProperties;

import junit.framework.Test;
import junit.framework.TestSuite;


public class AllTests extends TestSuite {
    static Logger log = Logger.getLogger(AllTests.class.getName());
    //for JSF project the path is
    // <project_name>\\src\\java\\properties\\dbconfig.properites
    private static final String LOC_DB_PROPS = "/MainProjectFX/src/resources/dbconfig.properties";
    private static final String LOC_RULES_PROPS = "/MainProjectFX/src/resources/rulesconfig.properties";
    private static final String context = computeDir();
    static {
    	log.info("Looking here: " + context + LOC_DB_PROPS);
    	initializeProperties();
	}
    
    private static String computeDir() {
    	File f = new File(System.getProperty("user.dir"));
    	if(f.exists() && f.isDirectory()) {
    		System.out.println( f.getParent());
    		return f.getParent();
    	}
    	return null;
    	
    }
    
    @SuppressWarnings("unused")
	private static boolean initialized = false;
    
    public static void initializeProperties() {
    	// Need to specify full path to dbconfig.properties
		// when accessing from outside the project.
    	if (!initialized) {
    		//DbConfigProperties.readProps(context + LOC_DB_PROPS);
    		DbConfigProperties.readProps("resources/dbconfig.properties");
    		//RulesConfigProperties.readProps(context + LOC_RULES_PROPS);
    		RulesConfigProperties.readProps("resources/rulesconfig.properties");
    		initialized = true;
    	}
    }
	
	public static Test suite() 
	{
		TestSuite suite = new TestSuite();
		//$JUnit-BEGIN$ -- put fully qualified classnames of all tests here

		//$JUnit-END$
		return suite;
	}
	
	
	
	
}

