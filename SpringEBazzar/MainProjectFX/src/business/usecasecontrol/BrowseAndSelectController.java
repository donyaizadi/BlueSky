package business.usecasecontrol;


import java.util.List;

import javax.inject.Inject;

import presentation.data.DataUtil;
import launch.Start;
import middleware.exceptions.DatabaseException;
import business.RulesQuantity;
import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.Catalog;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.Product;
import business.externalinterfaces.ProductSubsystem;
import business.externalinterfaces.Rules;
import business.externalinterfaces.ShoppingCart;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.productsubsystem.DbClassCatalogTypes;
import business.productsubsystem.ProductSubsystemFacade;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;

public enum BrowseAndSelectController {
	INSTANCE;
	@Inject
	ShoppingCartSubsystem scss;
	
	public void updateShoppingCartItems(List<CartItem> cartitems) {
		 scss =(ShoppingCartSubsystem) Start.ctx.getBean("scss");
		scss.updateShoppingCartItems(cartitems);
	}
	
	public List<CartItem> getCartItems() {
		return scss.getCartItems();
	}
	
	/** Makes saved cart live in subsystem and then returns the new list of cartitems */
	public void retrieveSavedCart() {
		// Saved cart was retrieved during login
		scss =(ShoppingCartSubsystem) Start.ctx.getBean("scss");
		scss.makeSavedCartLive();	
		
		System.out.println("EPAAAA "+scss.getCartItems().size());
	}
	
	public void runQuantityRules(Product product, String quantityRequested)
			throws RuleException, BusinessException {

//		ProductSubsystem prodSS = new ProductSubsystemFacade();
		
		//find current quant avail since quantity may have changed
		//since product was first loaded into UI
		ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
		int currentQuantityAvail = pss.readQuantityAvailable(product);
		Rules transferObject = new RulesQuantity(currentQuantityAvail, quantityRequested);//
		transferObject.runRules();

	}
	
	public List<Catalog> getCatalogs() throws BackendException {
//		ProductSubsystem pss = new ProductSubsystemFacade();
		ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
		return pss.getCatalogList();
	}
	
	public List<Product> getProducts(Catalog catalog) throws BackendException {
//		ProductSubsystem pss = new ProductSubsystemFacade();
		ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
		return pss.getProductList(catalog);
	}
	public Product getProductForProductName(String name) throws BackendException {
//		ProductSubsystem pss = new ProductSubsystemFacade();
		ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
		return pss.getProductFromName(name);
	}
	
	/** Assume customer is logged in */
	public CustomerProfile getCustomerProfile() {
		CustomerSubsystem cust = DataUtil.readCustFromCache();
		return cust.getCustomerProfile();
	}

	public void saveCart() throws BackendException {
		scss.saveLiveCart();
	}
}
