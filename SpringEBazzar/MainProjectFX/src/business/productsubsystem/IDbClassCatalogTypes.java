package business.productsubsystem;

import javax.sql.DataSource;
import middleware.exceptions.DatabaseException;

public interface IDbClassCatalogTypes {
	public void setDataSource(DataSource dataSource);
	public CatalogTypesImpl getCatalogTypes() throws DatabaseException;
	public String getQuery();
}
