package business.productsubsystem;

import java.sql.ResultSet;
import java.util.logging.Logger;

import business.externalinterfaces.Catalog;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

public class DbClassCatalog implements DbClass {
	@SuppressWarnings("unused")
	private static final Logger LOG = 
		Logger.getLogger(DbClassCatalog.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = 
    	new DataAccessSubsystemFacade();
	
	private String catalogName;
	private String query;
    private String queryType;
    private final String SAVE = "Save";
    private final String DELETE_CATALOG = "DeleteCatalog";
    
    public Integer saveNewCatalog(String name) throws DatabaseException {
    	this.catalogName = name;
    	queryType = SAVE;
    	return dataAccessSS.saveWithinTransaction(this);  	
    }
     
    public void deleteCatalog(Catalog catalog) throws DatabaseException {
    	this.catalogName = catalog.getName();
    	queryType = DELETE_CATALOG;
    	dataAccessSS.deleteWithinTransaction(this);
    }
    
	public void buildQuery() throws DatabaseException {
		if(queryType.equals(SAVE)) {
			buildSaveQuery();
		} else if (queryType.equals(DELETE_CATALOG)) {
			buildDeleteCatalogQuery();
		}
	}
	
	void buildSaveQuery() throws DatabaseException {
		query = "INSERT into CatalogType "+
		"(catalogid,catalogname) " +
		"VALUES(NULL,'"+
				  catalogName+"')"; 
	}
	
	void buildDeleteCatalogQuery() {
		query = "DELETE FROM CatalogType WHERE catalogname = '" + this.catalogName + "'";
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();	
    	return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
	}

	public String getQuery() {
		return query;
	}

	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		// do nothing
		
	}
	
}
