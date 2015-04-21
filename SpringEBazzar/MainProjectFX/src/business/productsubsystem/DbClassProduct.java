package business.productsubsystem;

import static business.util.StringParse.makeString;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import presentation.gui.GuiUtils;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductFromGui;
import business.util.TwoKeyHashMap;
import business.util.Util;

@Transactional(propagation=Propagation.SUPPORTS, readOnly=true, rollbackFor=Exception.class)
@Repository
class DbClassProduct implements IDbClassProduct {
	@SuppressWarnings("unused")
	private static final Logger LOG = Logger.getLogger(DbClassProduct.class.getPackage().getName());
	private JdbcTemplate jdbcTemplate;
	/**
	 * The productTable matches product ID with Product object. It is static so
	 * that requests for "read product" based on product ID can be handled
	 * without extra db hits
	 */
	private static TwoKeyHashMap<Integer, String, Product> productTable;
	private String queryType;
	private String query;
	private Product product;
	private String description;
	private List<Product> productList;
	Catalog catalog;
	Integer productId;

	private final String LOAD_PROD_TABLE = "LoadProdTable";
	private final String READ_PRODUCT = "ReadProduct";
	private final String READ_PROD_LIST = "ReadProdList";
//	private final String SAVE_NEW_PROD = "SaveNewProd";
	private final String DELETE_PRODUCT = "DeleteProduct";

	public DbClassProduct(){
	}

	@Inject
    @Named("dataSourceProducts")
    public void setDataSource(DataSource dataSource) {
    	this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    class ProductRowMapper implements RowMapper<Product> {
		public Product mapRow(ResultSet resultSet, int rownum)
				throws SQLException {
			Product product = new ProductImpl();
			product.setProductId(resultSet.getInt("productid"));
			product.setProductName(resultSet.getString("productname"));
			product.setQuantityAvail(resultSet.getInt("totalquantity"));
			product.setUnitPrice(resultSet.getDouble("priceperunit"));
			product.setMfgDate(GuiUtils.localDateForString(resultSet.getString("mfgdate")));
			product.setCatalog(new CatalogImpl(resultSet.getInt("catalogid"), (new CatalogTypesImpl()).getCatalogName(resultSet.getInt("catalogid"))));
			product.setDescription(resultSet.getString("description"));
			return product;
		}
    }

	public void buildQuery() {
		if (queryType.equals(LOAD_PROD_TABLE)) {
			buildProdTableQuery();
		} else if (queryType.equals(READ_PRODUCT)) {
			buildReadProductQuery();
		} else if (queryType.equals(READ_PROD_LIST)) {
			buildProdListQuery();
//		} else if (queryType.equals(SAVE_NEW_PROD)) {
//			buildSaveNewQuery();
		} else if (queryType.equals(DELETE_PRODUCT)) {
			buildDeleteProductQuery();
		}
	}

	private void buildProdTableQuery() {
		query = "SELECT * FROM product";
	}

	private void buildProdListQuery() {
		query = "SELECT * FROM Product WHERE catalogid = " + catalog.getId();
	}

	private void buildReadProductQuery() {
		query = "SELECT * FROM Product WHERE productid = " + productId;
	}

//	private void buildSaveNewQuery() {
//		System.out.println("CATALOG ID:******* " + catalog.getId());
//		query = "INSERT INTO Product (productid, catalogid, productname, totalquantity, priceperunit, mfgdate, description) " +
//				"VALUES (NULL, " + catalog.getId() + ", '" + product.getProductName() + "', " + product.getQuantityAvail() + ", " + product.getUnitPrice() + ", '" + Util.localDateAsString(product.getMfgDate()) + "', '" + product.getDescription() + "')";
//	}
	
	private void buildDeleteProductQuery() {
		query = "DELETE FROM Product WHERE productid = " + productId;
	}

	@Transactional(value = "txManagerProducts", propagation=Propagation.SUPPORTS, readOnly=true)
	public TwoKeyHashMap<Integer, String, Product> readProductTable() throws DatabaseException {
		if (productTable != null) {
			return productTable.clone();
		}
		return refreshProductTable();
	}

	/**
	 * Force a database call
	 */
	@Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
	public TwoKeyHashMap<Integer, String, Product> refreshProductTable() throws DatabaseException {
		productTable = new TwoKeyHashMap<Integer, String, Product>();
		List<Product> products = new ArrayList<Product>();
		queryType = LOAD_PROD_TABLE;
		buildQuery();
		try{
        	products = jdbcTemplate.query(query, new ProductRowMapper());
        	for(Product p: products){
        		productTable.put(p.getProductId(), p.getProductName(), p);
        	}
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for refreshProductTable with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
		// Return a clone since productTable must not be corrupted
		return productTable.clone();
	}

	@Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
	public List<Product> readProductList(Catalog cat) throws DatabaseException {
		if (productList == null) {
			return refreshProductList(cat);
		}
		return Collections.unmodifiableList(productList);
	}

	@Transactional(value = "txManagerProducts", propagation=Propagation.SUPPORTS, readOnly=true)
	public List<Product> refreshProductList(Catalog cat) throws DatabaseException {
		this.catalog = cat;
		List<Product> products = new ArrayList<Product>();
		productList = new ArrayList<Product>();
		queryType = READ_PROD_LIST;
		buildQuery();
		try{
        	products = jdbcTemplate.query(query, new ProductRowMapper());
        	for(Product p: products){
        		productList.add(p);
        	}
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for refreshProductList with query = " + query);
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
		return productList;
	}

	@Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=true)
	public Product readProduct(Integer productId) throws DatabaseException {
		if (productTable != null && productTable.isAFirstKey(productId)) {
			return productTable.getValWithFirstKey(productId);
		}
		product = new ProductImpl();
		queryType = READ_PRODUCT;
		this.productId = productId;
		buildQuery();
		try{
//			product = jdbcTemplate.query(query, new ProductRowMapper());
			product = jdbcTemplate.queryForObject(query, product.getClass());
		} catch(DataAccessException e) {
			LOG.warning("Rolling back transaction for readProduct with query = " + query);
			LOG.warning("Error details:\n" + e.getMessage());
			throw new DatabaseException(e);
		}
		return product;
	}

	/**
	 * Database columns: productid, productname, totalquantity, priceperunit,
	 * mfgdate, catalogid, description
	 */
	
	@Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public Integer saveNewProduct(Product product, Catalog catalog) throws DatabaseException {
		this.product = product;
		this.catalog = catalog;
//		queryType = SAVE_NEW_PROD;

		Integer productId;
		SimpleJdbcInsert jdbcInsert = new SimpleJdbcInsert(jdbcTemplate);
		jdbcInsert.withTableName("Product").usingGeneratedKeyColumns("productid");
       
        Map<String, Object> parameters = new HashMap<String, Object>();
        parameters.put("catalogid", catalog.getId());
        parameters.put("productname", product.getProductName());
        parameters.put("totalquantity", product.getQuantityAvail());
        parameters.put("priceperunit", product.getUnitPrice());
        parameters.put("mfgdate", Util.localDateAsString(product.getMfgDate()));
        parameters.put("description", product.getDescription());
        try{
        	// execute insert
        	Number key = jdbcInsert.executeAndReturnKey(parameters);
        	productId = ((Number) key).intValue();
    	} catch(DataAccessException e) {
    		LOG.warning("Rolling back transaction for save product");
    		LOG.warning("Error details:\n" + e.getMessage());
    		throw new DatabaseException(e);
    	}
        return productId;
	}

	@Transactional(value = "txManagerProducts", propagation=Propagation.REQUIRES_NEW, readOnly=false)
	public void deleteProduct(Product product) throws DatabaseException {
		this.productId = product.getProductId();
		queryType = DELETE_PRODUCT;
    	buildQuery();
    	try{
    		jdbcTemplate.update(query);
    	} catch(DataAccessException e) {
			LOG.warning("Rolling back transaction for DELETE_PRODUCT catalog with query = " + query);
			LOG.warning("Error details:\n" + e.getMessage());
			throw new DatabaseException(e);
    	}

	}
	
//	public void populateEntity(ResultSet resultSet) throws DatabaseException {
//		if (queryType.equals(LOAD_PROD_TABLE)) {
//			populateProdTable(resultSet);
//		} else if (queryType.equals(READ_PRODUCT)) {
//			populateProduct(resultSet);
//		} else if (queryType.equals(READ_PROD_LIST)) {
//			populateProdList(resultSet);
//		}
//	}
//	
//	
//
//	private void populateProdList(ResultSet rs) throws DatabaseException {
//		productList = new LinkedList<Product>();
//		try {
//			Product product = null;
//			Integer prodId = null;
//			String productName = null;
//			Integer quantityAvail = null;
//			Double unitPrice = null;
//			String mfgDate = null;
//			Integer catalogId = null;
//			String description = null;
//			while (rs.next()) {
//				prodId = rs.getInt("productid");
//				productName = rs.getString("productname");
//				quantityAvail = rs.getInt("totalquantity");
//				unitPrice =rs.getDouble("priceperunit");
//				mfgDate = rs.getString("mfgdate");
//				catalogId = rs.getInt("catalogid");
//				description = rs.getString("description");
//				CatalogImpl catalog = new CatalogImpl(catalogId, (new CatalogTypesImpl()).getCatalogName(catalogId));
//				//Catalog c, Integer pi, String pn, int qa, 
//				//double up, LocalDate md, String d
//				product = new ProductImpl(catalog, prodId, productName, quantityAvail,
//						 unitPrice, GuiUtils.localDateForString(mfgDate),
//					    description);
//				productList.add(product); ////////////////////////////////donya
//			}
//		} catch (SQLException e) {
//			throw new DatabaseException(e);
//		}
//	}

	/**
	 * Internal method to ensure that product table is up to date.
	 */
//	private void populateProdTable(ResultSet rs) throws DatabaseException {
//		productTable = new TwoKeyHashMap<Integer, String, Product>();
//		try {
//			Product product = null;
//			Integer prodId = null;
//			String productName = null;
//			Integer quantityAvail = null;
//			Double unitPrice = null;
//			String mfgDate = null;
//			Integer catalogId = null;
//			String description = null;
//			while (rs.next()) {
//				prodId = rs.getInt("productid");
//				productName = rs.getString("productname");
//				quantityAvail = rs.getInt("totalquantity");
//				unitPrice = rs.getDouble("priceperunit");
//				mfgDate = rs.getString("mfgdate");
//				catalogId = rs.getInt("catalogid");
//				description = rs.getString("description");
//				CatalogImpl catalog = new CatalogImpl(catalogId, (new CatalogTypesImpl()).getCatalogName(catalogId));
//				product = new ProductImpl(catalog, prodId, productName, quantityAvail,
//						unitPrice, GuiUtils.localDateForString(mfgDate), description);
//				productTable.put(prodId, productName, product); 
//			}
//		} catch (SQLException e) {
//			throw new DatabaseException(e);
//		}
//	}

//	private void populateProduct(ResultSet rs) throws DatabaseException {
//		try {
//			if (rs.next()) {
//				CatalogImpl catalog = new CatalogImpl(rs.getInt("catalogid"), 
//						(new CatalogTypesImpl()).getCatalogName(rs.getInt("catalogid")));
//				
//				product = new ProductImpl(catalog, rs.getInt("productid"),
//						rs.getString("productname"),
//						rs.getInt("totalquantity"),
//						rs.getDouble("priceperunit"),
//						GuiUtils.localDateForString(rs.getString("mfgdate")),
//						rs.getString("description"));
//			}
//		} catch (SQLException e) {
//			throw new DatabaseException(e);
//		}
//	}

//	public String getDbUrl() {
//		DbConfigProperties props = new DbConfigProperties();
//		return props.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
//	}

	public String getQuery() {
		return query;
	}
}
