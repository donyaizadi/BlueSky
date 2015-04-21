package business.productsubsystem;

import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import business.externalinterfaces.Address;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.CreditCard;
import business.util.Util;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;


@Transactional(propagation=Propagation.SUPPORTS, readOnly=false, rollbackFor=Exception.class)
@Repository
public class DbClassCatalog implements IDbClassCatalog {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DbClassCatalog.class.getPackage().getName());
	
	private JdbcTemplate jdbcTemplate;
	private String catalogName;
	private String query;
    private String queryType;
    private final String SAVE = "Save";
    private final String DELETE_CATALOG = "DeleteCatalog";
   
	public DbClassCatalog(){
	}
    
    @Inject
    @Named("dataSourceProducts")
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
   
    @Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=false)
    public Integer saveNewCatalog(String name) throws DatabaseException {
//    	queryType = SAVE;
    	this.catalogName = name;
		Integer catalogId;
		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcInsert.withTableName("CatalogType").usingGeneratedKeyColumns("catalogid");
       
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("catalogname", this.catalogName);
        try{
        	// execute insert
        	Number key = jdbcInsert.executeAndReturnKey(parameters);
        	catalogId = ((Number) key).intValue();
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for save catalog with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        return catalogId;
    }
     
    @Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=false)
    public void deleteCatalog(Catalog catalog) throws DatabaseException {
    	this.catalogName = catalog.getName();
    	queryType = DELETE_CATALOG;
    	buildQuery();
    	try{
    		jdbcTemplate.update(query);
    	} catch(DataAccessException e) {
			LOG.warning("Rolling back transaction for DELETE_CATALOG catalog with query = " + query);
			LOG.warning("Error details:\n" + e.getMessage());
			throw new DatabaseException(e);
    	}
    	
    }
    
	public void buildQuery() throws DatabaseException {
		if(queryType.equals(DELETE_CATALOG)) {
			buildDeleteCatalogQuery();
//		} else if(queryType.equals(SAVE)) {
//			buildSaveQuery();
		}
	}
	
//	void buildSaveQuery() throws DatabaseException {
//		query = "INSERT into CatalogType "+ "(catalogid,catalogname) " + "VALUES(NULL,'"+ catalogName+"')"; 
//	}
	
	void buildDeleteCatalogQuery() {
		query = "DELETE FROM CatalogType WHERE catalogname = '" + this.catalogName + "'";
	}

//	public String getDbUrl() {
//		DbConfigProperties props = new DbConfigProperties();	
//    	return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
//	}

	public String getQuery() {
		return query;
	}
	
}
