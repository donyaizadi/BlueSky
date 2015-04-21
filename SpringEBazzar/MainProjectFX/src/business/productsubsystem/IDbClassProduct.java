package business.productsubsystem;

import java.util.List;

import javax.sql.DataSource;

import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.util.TwoKeyHashMap;
import middleware.exceptions.DatabaseException;

public interface IDbClassProduct {
	public void setDataSource(DataSource dataSource);
	public TwoKeyHashMap<Integer, String, Product> readProductTable() throws DatabaseException;
	public TwoKeyHashMap<Integer, String, Product> refreshProductTable()throws DatabaseException;
	public List<Product> refreshProductList(Catalog cat) throws DatabaseException;
	public Product readProduct(Integer productId) throws DatabaseException;
	public Integer saveNewProduct(Product product, Catalog catalog) throws DatabaseException;
	public void deleteProduct(Product product) throws DatabaseException;
	public List<Product> readProductList(Catalog cat) throws DatabaseException;
	public String getQuery();

}
