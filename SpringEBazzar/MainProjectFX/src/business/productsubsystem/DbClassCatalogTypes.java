package business.productsubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import business.externalinterfaces.Catalog;
import business.ordersubsystem.OrderImpl;
import business.util.Util;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;


@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
@Repository
public class DbClassCatalogTypes implements IDbClassCatalogTypes {
	
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DbClassCatalogTypes.class.getPackage().getName());
	private JdbcTemplate jdbcTemplate;
	
	private String query;
    private String queryType;
    final String GET_TYPES = "GetTypes";
    private CatalogTypesImpl types;

    public DbClassCatalogTypes(){}
    
    @Inject
    @Named("dataSourceProducts")
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }
 
    class CatalogRowMapper implements RowMapper<Catalog> {
		public Catalog mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			Catalog catalog = new CatalogImpl();
			catalog.setId(resultSet.getInt("catalogid"));
			catalog.setName(resultSet.getString("catalogname"));
			return catalog;
		}
    }

    @Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
    public CatalogTypesImpl getCatalogTypes() throws DatabaseException {
        queryType = GET_TYPES;
        buildQuery();
    	try {
    		List<Catalog> catalogs = jdbcTemplate.query(query, new CatalogRowMapper());
    		types = new CatalogTypesImpl();
    		for(Catalog catalog : catalogs) {
    			types.addCatalog(catalog.getId(), catalog.getName());
    		}
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for addCatalog with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        return types;        
    }

    
    public void buildQuery() {
        if(queryType.equals(GET_TYPES)){
            buildGetTypesQuery();
        }
    }

    void buildGetTypesQuery() {
        query = "SELECT * FROM CatalogType";       
    }

    public String getQuery() {
        return query;
    }

}
