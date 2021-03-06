package presentation.data;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import business.BusinessConstants;
import business.SessionCache;
import business.customersubsystem.CustomerSubsystemFacade;
import business.externalinterfaces.Address;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.usecasecontrol.BrowseAndSelectController;
import business.usecasecontrol.CheckoutController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import presentation.control.CheckoutUIControl;
import presentation.gui.GuiConstants;

public enum CheckoutData {
	INSTANCE;
	private static final Logger LOG = Logger.getLogger(CheckoutData.class
			.getPackage().getName());
	public Address createAddress(String street, String city, String state,
			String zip, boolean isShip, boolean isBill) {
		return CustomerSubsystemFacade.createAddress(street, city, state, zip, isShip, isBill);
	}
	
	public CreditCard createCreditCard(String nameOnCard,String expirationDate,
               String cardNum, String cardType) {
		return CustomerSubsystemFacade.createCreditCard(nameOnCard, expirationDate, 
				cardNum, cardType);
	}
	
	//Customer Ship Address Data
	private ObservableList<CustomerPres> shipAddresses;
	
	//Customer Bill Address Data
	private ObservableList<CustomerPres> billAddresses;
	
	private List<CustomerPres> getShipAddresses(){
		List<Address> shipAdresses = CheckoutController.INSTANCE.retrieveShippingAddresses();
		CustomerSubsystem cust = 
				(CustomerSubsystem)SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
		//transform to customerPres
		List<CustomerPres> customerPresenter = shipAdresses.stream().map(customerPres ->{
			return new CustomerPres(cust.getCustomerProfile(),customerPres); 
		}).collect(Collectors.toList());
		return customerPresenter;
	}
	private ObservableList<CustomerPres> loadShipAddresses() {	
		//load Fake data
//	    List<CustomerPres> list = DefaultData.CUSTS_ON_FILE
//						   .stream()
//						   .filter(cust -> cust.getAddress().isShippingAddress())
//						   .collect(Collectors.toList());
		//load Data from DB
		List<CustomerPres> list = getShipAddresses();
		return FXCollections.observableList(list);				   
										   
	}
	
	private List<CustomerPres> getBillAddresses(){
		List<Address> billAddresses = CheckoutController.INSTANCE.retrieveBillingAddresses();
		CustomerSubsystem cust = 
				(CustomerSubsystem)SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
		//transform to customerPres
		List<CustomerPres> customerPresenter = billAddresses.stream().map(customerPres ->{
			return new CustomerPres(cust.getCustomerProfile(),customerPres); 
		}).collect(Collectors.toList());
		return customerPresenter;
	}
	private ObservableList<CustomerPres> loadBillAddresses() {
		//load Fake data
//		List list = DefaultData.CUSTS_ON_FILE
//				   .stream()
//				   .filter(cust -> cust.getAddress().isBillingAddress())
//				   .collect(Collectors.toList());
		
		//load Data from DB
		List<CustomerPres> list = getBillAddresses();
		for(CustomerPres singleCustomer: list){
			
		}
		return FXCollections.observableList(list);
	}
	public ObservableList<CustomerPres> getCustomerShipAddresses() {
		shipAddresses = loadShipAddresses();
		return shipAddresses;
	}
	public ObservableList<CustomerPres> getCustomerBillAddresses() {
		billAddresses = loadBillAddresses();
		return billAddresses;
	}
	public List<String> getDisplayAddressFields() {
		return GuiConstants.DISPLAY_ADDRESS_FIELDS;
	}
	public List<String> getDisplayCredCardFields() {
		return GuiConstants.DISPLAY_CREDIT_CARD_FIELDS;
	}
	public List<String> getCredCardTypes() {
		return GuiConstants.CREDIT_CARD_TYPES;
	}
	public Address getDefaultShippingData() {
		//implement
//		List<String> add = DefaultData.DEFAULT_SHIP_DATA;
//		return CustomerSubsystemFacade.createAddress(add.get(0), add.get(1), 
//				add.get(2), add.get(3), true, false);
		CustomerSubsystem cust = 
				(CustomerSubsystem)SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
		return cust.getDefaultShippingAddress();
	}
	
	public Address getDefaultBillingData() {
		/*List<String> add =  DefaultData.DEFAULT_BILLING_DATA;
		return CustomerSubsystemFacade.createAddress(add.get(0), add.get(1), 
				add.get(2), add.get(3), false, true);*/
		CustomerSubsystem cust = 
				(CustomerSubsystem)SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
		return cust.getDefaultBillingAddress();
		
	}
	
	public CreditCard getDefaultPaymentInfo() {
		CustomerSubsystem cust = 
				(CustomerSubsystem)SessionCache.getInstance().get(BusinessConstants.CUSTOMER);
		return cust.getDefaultPaymentInfo();
		//return DefaultData.DEFAULT_PAYMENT_INFO;
	}
	
	
	public CustomerProfile getCustomerProfile() {
		return BrowseAndSelectController.INSTANCE.getCustomerProfile();
	}
	
		
	
	private class ShipAddressSynchronizer implements Synchronizer {
		public void refresh(ObservableList list) {
			shipAddresses = list;
		}
	}
	public ShipAddressSynchronizer getShipAddressSynchronizer() {
		return new ShipAddressSynchronizer();
	}
	
	private class BillAddressSynchronizer implements Synchronizer {
		public void refresh(ObservableList list) {
			billAddresses = list;
		}
	}
	public BillAddressSynchronizer getBillAddressSynchronizer() {
		return new BillAddressSynchronizer();
	}
	
	public static class ShipBill {
		public boolean isShipping;
		public String label;
		public Synchronizer synch;
		public ShipBill(boolean shipOrBill, String label, Synchronizer synch) {
			this.isShipping = shipOrBill;
			this.label = label;
			this.synch = synch;
		}
		
	}
	
}
