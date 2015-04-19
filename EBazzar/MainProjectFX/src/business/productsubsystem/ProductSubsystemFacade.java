package business.productsubsystem;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

import middleware.exceptions.DatabaseException;
import business.exceptions.BackendException;
import business.externalinterfaces.*;
import business.util.TwoKeyHashMap;

public class ProductSubsystemFacade implements ProductSubsystem {
	public static Catalog createCatalog(int id, String name) {
		return new CatalogImpl(id, name);
	}
	public static Product createProduct(Catalog c, String name, 
			LocalDate date, int numAvail, double price) {
		return new ProductImpl(c, name, date, numAvail, price);
	}
	public static Product createProduct(Catalog c, Integer pi, String pn, int qa, 
			double up, LocalDate md, String desc) {
		return new ProductImpl(c, pi, pn, qa, up, md, desc);
	}
	
	/** obtains product for a given product name */
    public Product getProductFromName(String prodName) throws BackendException {
    	try {
			DbClassProduct dbclass = new DbClassProduct();
			return dbclass.readProduct(getProductIdFromName(prodName));
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}	
    }
    
    public Integer getProductIdFromName(String prodName) throws BackendException {
		try {
			DbClassProduct dbclass = new DbClassProduct();
			TwoKeyHashMap<Integer,String,Product> table = dbclass.readProductTable();
			return table.getFirstKey(prodName);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
	}
    public Product getProductFromId(Integer prodId) throws BackendException {
		try {
			DbClassProduct dbclass = new DbClassProduct();
			return dbclass.readProduct(prodId);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
	}
    
    public List<Catalog> getCatalogList() throws BackendException {
    	try {
			DbClassCatalogTypes dbClass = new DbClassCatalogTypes();
			return dbClass.getCatalogTypes().getCatalogs();
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
		
    }
    
    public List<Product> getProductList(Catalog catalog) throws BackendException {
    	try {
    		DbClassProduct dbclass = new DbClassProduct();
    		return dbclass.readProductList(catalog);
    	} catch(DatabaseException e) {
    		throw new BackendException(e);
    	}
    }
	
	public int readQuantityAvailable(Product product) {
		int quantity = 0;
		try {
			DbClassProduct dbclass = new DbClassProduct();
			quantity = dbclass.readProduct(product.getProductId()).getQuantityAvail();
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
			
		return quantity;
	}
	
	@Override
	public Catalog getCatalogFromName(String catName) throws BackendException {
		try {
			DbClassCatalogTypes dbClass = new DbClassCatalogTypes();
			List<Catalog> catalogs = dbClass.getCatalogTypes().getCatalogs();
			List<Catalog> resultCatalogs = catalogs.stream().filter(c -> c.getName().equals(catName)).collect(Collectors.toList());
			if (resultCatalogs.size() > 0) {
				return resultCatalogs.get(0);
			} else {
				return null;
			}
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
	}
	@Override
	
	public Integer saveNewCatalog(Catalog catalog) throws BackendException {
		Integer catalogId = -1;
		DbClassCatalog dbClass = new DbClassCatalog();
		try {
			catalogId = dbClass.saveNewCatalog(catalog.getName());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		return catalogId;
	}
	
	@Override
	public Integer saveNewProduct(Product product) throws BackendException {
		Integer productId = -1;
		
		DbClassProduct dbClass = new DbClassProduct();
		try {
			productId = dbClass.saveNewProduct(product, product.getCatalog());
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
		
		return productId;
	}
	
	@Override
	public void deleteProduct(Product product) throws BackendException {
		DbClassProduct dbClass = new DbClassProduct();
		try {
			dbClass.deleteProduct(product);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void deleteCatalog(Catalog catalog) throws BackendException {
		DbClassCatalog dbClass = new DbClassCatalog();
		try {
			dbClass.deleteCatalog(catalog);
		} catch (DatabaseException e) {
			e.printStackTrace();
		}
	}
}
