package presentation.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import launch.Start;
import middleware.exceptions.DatabaseException;
import presentation.gui.GuiUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.ObservableMap;
import business.CartItemData;
import business.exceptions.BackendException;
import business.externalinterfaces.*;
import business.productsubsystem.ProductSubsystemFacade;

public enum ManageProductsData {
	INSTANCE;
//	ProductSubsystem productSubsystem = new ProductSubsystemFacade();
	ProductSubsystem productSubsystem = (ProductSubsystemFacade)Start.ctx.getBean("pss");
	
	private CatalogPres defaultCatalog = readDefaultCatalogFromDataSource();
	private CatalogPres readDefaultCatalogFromDataSource() {
		return readCatalogsFromDataSource().get(0);
	}
	public CatalogPres getDefaultCatalog() {
		return defaultCatalog;
	}
	
	private CatalogPres selectedCatalog = defaultCatalog;
	public void setSelectedCatalog(CatalogPres selCatalog) {
		selectedCatalog = selCatalog;
	}
	public CatalogPres getSelectedCatalog() {
		return selectedCatalog;
	}
	//////////// Products List model
	private ObservableMap<CatalogPres, List<ProductPres>> productsMap
	   = readProductsFromDataSource();
	
	/** Initializes the productsMap */
	private ObservableMap<CatalogPres, List<ProductPres>> readProductsFromDataSource() {
		Map<CatalogPres, List<ProductPres>> productsMap = new HashMap<CatalogPres, List<ProductPres>>();
		
		List<CatalogPres> catPresList = readCatalogsFromDataSource();
		for (CatalogPres catPres : catPresList) {
			List<Product> products;
			try {
				products = productSubsystem.getProductList(catPres.getCatalog());
				
				List<ProductPres> productsPres = new ArrayList<ProductPres>();
				for (Product product : products) {
					productsPres.add(new ProductPres(product));
				}
				
				productsMap.put(catPres, productsPres);
			} catch (BackendException e) {
				e.printStackTrace();
			}
		}
		
		return FXCollections.observableMap(productsMap);
	}
	
	/** Delivers the requested products list to the UI */
	public ObservableList<ProductPres> getProductsList(CatalogPres catPres) {
		return FXCollections.observableList(productsMap.get(catPres));
	}
	
	public ProductPres productPresFromData(Catalog c, String name, String date,  //MM/dd/yyyy 
			int numAvail, double price) {
		
		Product product = ProductSubsystemFacade.createProduct(c, name, 
				GuiUtils.localDateForString(date), numAvail, price);
		ProductPres prodPres = new ProductPres();
		prodPres.setProduct(product);
		return prodPres;
	}
	
	public void addToProdList(CatalogPres catPres, ProductPres prodPres) {
//		ProductSubsystemFacade productSubsystemFacade = new ProductSubsystemFacade();
		ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
		prodPres.getProduct().setCatalog(catPres.getCatalog());
		try {
			Integer productId = pss.saveNewProduct(prodPres.getProduct());
			prodPres.getProduct().setProductId(productId);
		} catch (BackendException e) {
			e.printStackTrace();
		}
		
		ObservableList<ProductPres> newProducts =
		           FXCollections.observableArrayList(prodPres);
		List<ProductPres> specifiedProds = productsMap.get(catPres);
		
		//Place the new item at the bottom of the list
		specifiedProds.addAll(newProducts);
	}
	
	/** This method looks for the 0th element of the toBeRemoved list 
	 *  and if found, removes it. In this app, removing more than one product at a time
	 *  is  not supported.
	 */
	public boolean removeFromProductList(CatalogPres cat, ObservableList<ProductPres> toBeRemoved) {
		if(toBeRemoved != null && !toBeRemoved.isEmpty()) {
//			ProductSubsystemFacade productSubsystemFacade = new ProductSubsystemFacade();
			ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
			try {
				pss.deleteProduct(toBeRemoved.get(0).getProduct());
			} catch (BackendException e) {
				e.printStackTrace();
			}
			boolean result = productsMap.get(cat).remove(toBeRemoved.get(0));
			
			return result;
		}
		return false;
	}
		
	//////// Catalogs List model
	private ObservableList<CatalogPres> catalogList = readCatalogsFromDataSource();

	/** Initializes the catalogList */
	private ObservableList<CatalogPres> readCatalogsFromDataSource() {
		List<CatalogPres> catPres = new ArrayList<CatalogPres>();
		try {
			for (Catalog cat : productSubsystem.getCatalogList()) {
				catPres.add(new CatalogPres(cat));
			}
		} catch (BackendException e) {
			e.printStackTrace();
		}
		
		return FXCollections.observableList(catPres);
	}

	/** Delivers the already-populated catalogList to the UI */
	public ObservableList<CatalogPres> getCatalogList() {
		return catalogList;
	}

	public CatalogPres catalogPresFromData(int id, String name) {
		Catalog cat = ProductSubsystemFacade.createCatalog(id, name);
		CatalogPres catPres = new CatalogPres();
		catPres.setCatalog(cat);
		return catPres;
	}

	public void addToCatalogList(CatalogPres catPres) throws BackendException {
		ObservableList<CatalogPres> newCatalogs = FXCollections
				.observableArrayList(catPres);

		// Place the new item at the bottom of the list
		// catalogList is guaranteed to be non-null
		boolean result = catalogList.addAll(newCatalogs);
		if(result) { //must make this catalog accessible in productsMap
//			ProductSubsystemFacade productSubsystemFacade = new ProductSubsystemFacade();
			ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
			Integer catalogId = pss.saveNewCatalog(catPres.getCatalog());
			catPres.getCatalog().setId(catalogId);
			
			productsMap.put(catPres, FXCollections.observableList(new ArrayList<ProductPres>()));
		}
	}

	/**
	 * This method looks for the 0th element of the toBeRemoved list in
	 * catalogList and if found, removes it. In this app, removing more than one
	 * catalog at a time is not supported.
	 * 
	 * This method also updates the productList by removing the products that
	 * belong to the Catalog that is being removed.
	 * 
	 * Also: If the removed catalog was being stored as the selectedCatalog,
	 * the next item in the catalog list is set as "selected"
	 */
	public boolean removeFromCatalogList(ObservableList<CatalogPres> toBeRemoved) throws BackendException {
		boolean result = false;
		CatalogPres item = toBeRemoved.get(0);
		if (toBeRemoved != null && !toBeRemoved.isEmpty()) {
			result = catalogList.remove(item);
//			ProductSubsystemFacade productSubsystemFacade = new ProductSubsystemFacade();
			ProductSubsystem pss = (ProductSubsystemFacade)Start.ctx.getBean("pss");
			pss.deleteCatalog(item.getCatalog());
		}
		if(item.equals(selectedCatalog)) {
			if(!catalogList.isEmpty()) {
				selectedCatalog = catalogList.get(0);
			} else {
				selectedCatalog = null;
			}
		}
		if(result) {//update productsMap
			productsMap.remove(item);
		}
		return result;
	}
	
	//Synchronizers
	public class ManageProductsSynchronizer implements Synchronizer {
		@SuppressWarnings("rawtypes")
		@Override
		public void refresh(ObservableList list) {
			productsMap.put(selectedCatalog, list);
		}
	}
	
	public ManageProductsSynchronizer getManageProductsSynchronizer() {
		return new ManageProductsSynchronizer();
	}
	
	private class ManageCatalogsSynchronizer implements Synchronizer {
		@SuppressWarnings("rawtypes")
		@Override
		public void refresh(ObservableList list) {
			catalogList = list;
		}
	}
	
	public ManageCatalogsSynchronizer getManageCatalogsSynchronizer() {
		return new ManageCatalogsSynchronizer();
	}
}
