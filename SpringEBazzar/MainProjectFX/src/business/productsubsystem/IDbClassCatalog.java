package business.productsubsystem;

import javax.sql.DataSource;

import business.externalinterfaces.Catalog;
import middleware.exceptions.DatabaseException;

public interface IDbClassCatalog {
	public void setDataSource(DataSource dataSource);
	public Integer saveNewCatalog(String name) throws DatabaseException;
	public void deleteCatalog(Catalog catalog) throws DatabaseException;
	public String getQuery();
}
