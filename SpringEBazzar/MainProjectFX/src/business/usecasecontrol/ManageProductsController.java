
package business.usecasecontrol;

import java.util.List;
import java.util.logging.Logger;

import launch.Start;
import middleware.exceptions.DatabaseException;
import business.exceptions.BackendException;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.productsubsystem.ProductSubsystemFacade;


public class ManageProductsController   {
    
    private static final Logger LOG = 
    	Logger.getLogger(ManageProductsController.class.getName());
    
    public List<Product> getProductsList(Catalog catalog) throws BackendException {
//    	ProductSubsystem pss = new ProductSubsystemFacade();
    	ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
    	return pss.getProductList(catalog);
    }
    
    
    public void deleteProduct() {
    	//implement
    } 
}
