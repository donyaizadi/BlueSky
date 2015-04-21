package presentation.control;

import business.exceptions.UnauthorizedException;
import business.usecasecontrol.BrowseAndSelectController;
import presentation.data.BrowseSelectData;
import presentation.data.CatalogPres;
import presentation.data.DataUtil;
import presentation.data.ManageProductsData;
import presentation.data.ProductPres;
import presentation.gui.MaintainCatalogsWindow;
import presentation.gui.MaintainProductsWindow;
import presentation.gui.MessageableWindow;
import presentation.gui.TableUtil;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.scene.text.Text;
import javafx.stage.Stage;


public enum ManageProductsUIControl {
	INSTANCE;

	private Stage primaryStage;
	private Callback startScreenCallback;

	public void setPrimaryStage(Stage ps, Callback returnMessage) {
		primaryStage = ps;
		startScreenCallback = returnMessage;
	}

	// windows managed by this class
	MaintainCatalogsWindow maintainCatalogsWindow;
	MaintainProductsWindow maintainProductsWindow;

	// Manage catalogs
	private class MaintainCatalogsHandler implements EventHandler<ActionEvent>, Callback {
		@Override
		public void handle(ActionEvent e) {
			maintainCatalogsWindow = new MaintainCatalogsWindow(primaryStage);
			
			boolean isLoggedIn = DataUtil.isLoggedIn();
			if (!isLoggedIn) {
				LoginUIControl loginControl = new LoginUIControl(maintainCatalogsWindow, primaryStage, this);
				loginControl.startLogin();
			} else {
				doUpdate();
			}
		}

		@Override
		public Text getMessageBar() {
			return startScreenCallback.getMessageBar();
		}

		@Override
		public void doUpdate() {
			try {
	    		Authorization.checkAuthorization(maintainCatalogsWindow, DataUtil.custIsAdmin());
	    	} catch(UnauthorizedException e) {   
	        	displayError(e.getMessage());
	        	return;
	        }
			
			ObservableList<CatalogPres> list = ManageProductsData.INSTANCE.getCatalogList();
			maintainCatalogsWindow.setData(list);
			maintainCatalogsWindow.show();
			primaryStage.hide();
		}
	}
	
	public MaintainCatalogsHandler getMaintainCatalogsHandler() {
		return new MaintainCatalogsHandler();
	}
	
	private class MaintainProductsHandler implements EventHandler<ActionEvent>, Callback {
		public void doUpdate() {
			try {
	    		Authorization.checkAuthorization(maintainProductsWindow, DataUtil.custIsAdmin());
	    	} catch(UnauthorizedException e) {   
	        	displayError(e.getMessage());
	        	return;
	        }			
			CatalogPres selectedCatalog = ManageProductsData.INSTANCE.getSelectedCatalog();
			if(selectedCatalog != null) {
				ObservableList<ProductPres> list = ManageProductsData.INSTANCE.getProductsList(selectedCatalog);
				maintainProductsWindow.setData(ManageProductsData.INSTANCE.getCatalogList(), list);
			}
			primaryStage.hide();
			maintainProductsWindow.show();
		}
		public Text getMessageBar() {
			return startScreenCallback.getMessageBar();
		}
		
		@Override
		public void handle(ActionEvent e) {
			maintainProductsWindow = new MaintainProductsWindow(primaryStage);
			
			boolean isLoggedIn = DataUtil.isLoggedIn();
			if (!isLoggedIn) {
				LoginUIControl loginControl = new LoginUIControl(maintainProductsWindow, primaryStage, this);
				loginControl.startLogin();
			} else {
				doUpdate();
			}
		}
	}
	public MaintainProductsHandler getMaintainProductsHandler() {
		return new MaintainProductsHandler();
	}
	
	private class BackButtonHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {		
			maintainCatalogsWindow.clearMessages();		
			maintainCatalogsWindow.hide();
			startScreenCallback.clearMessages();
			primaryStage.show();
		}
			
	}
	public BackButtonHandler getBackButtonHandler() {
		return new BackButtonHandler();
	}
	
	private class BackFromProdsButtonHandler implements EventHandler<ActionEvent> {
		public void handle(ActionEvent evt) {		
			maintainProductsWindow.clearMessages();		
			maintainProductsWindow.hide();
			startScreenCallback.clearMessages();
			primaryStage.show();
		}
			
	}
	public BackFromProdsButtonHandler getBackFromProdsButtonHandler() {
		return new BackFromProdsButtonHandler();
	}


	/*
	 * private MenuItem maintainCatalogs() { MenuItem retval = new
	 * MenuItem("Maintain Catalogs"); retval.setOnAction(evt -> {
	 * MaintainCatalogsWindow maintain = new
	 * MaintainCatalogsWindow(primaryStage); ObservableList<CatalogPres> list =
	 * FXCollections.observableList( DefaultData.CATALOG_LIST_DATA);
	 * maintain.setData(list); maintain.show(); primaryStage.hide();
	 * 
	 * }); return retval; } private MenuItem maintainProducts() { MenuItem
	 * retval = new MenuItem("Maintain Products"); retval.setOnAction(evt -> {
	 * MaintainProductsWindow maintain = new
	 * MaintainProductsWindow(primaryStage); ObservableList<Product> list =
	 * FXCollections.observableList(
	 * DefaultData.PRODUCT_LIST_DATA.get(DefaultData.BOOKS_CATALOG));
	 * maintain.setData(DefaultData.CATALOG_LIST_DATA, list); maintain.show();
	 * primaryStage.hide();
	 * 
	 * }); return retval; }
	 */
}
