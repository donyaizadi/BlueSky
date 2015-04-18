package business.customersubsystem;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.sun.org.apache.regexp.internal.recompile;

import middleware.creditverifcation.CreditVerificationFacade;
import middleware.exceptions.DatabaseException;
import middleware.exceptions.MiddlewareException;
import middleware.externalinterfaces.CreditVerification;
import business.exceptions.BackendException;
import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.CustomerSubsystem;
import business.externalinterfaces.DbClassAddressForTest;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderSubsystem;
import business.externalinterfaces.Rules;
import business.externalinterfaces.ShoppingCartSubsystem;
import business.ordersubsystem.OrderSubsystemFacade;
import business.shoppingcartsubsystem.DbClassShoppingCart;
import business.shoppingcartsubsystem.ShoppingCartSubsystemFacade;

public class CustomerSubsystemFacade implements CustomerSubsystem {
	ShoppingCartSubsystem shoppingCartSubsystem;
	OrderSubsystem orderSubsystem;
	List<Order> orderHistory;
	AddressImpl defaultShipAddress;
	AddressImpl defaultBillAddress;
	CreditCardImpl defaultPaymentInfo;
	CustomerProfileImpl customerProfile;
	DbClassAddress defaultDbClassAddress;
	ShoppingCartSubsystem liveCart =  ShoppingCartSubsystemFacade.INSTANCE;
	CreditVerificationFacade cvObj = new CreditVerificationFacade();  
	OrderSubsystemFacade subOrderObj = new OrderSubsystemFacade(customerProfile);
	
	/** Use for loading order history,
	 * default addresses, default payment info, 
	 * saved shopping cart,cust profile
	 * after login*/
    public void initializeCustomer(Integer id, int authorizationLevel) 
    		throws BackendException {
	    boolean isAdmin = (authorizationLevel >= 1);
		loadCustomerProfile(id, isAdmin);
		loadDefaultShipAddress();
		loadDefaultBillAddress();
		loadDefaultPaymentInfo();
		shoppingCartSubsystem = ShoppingCartSubsystemFacade.INSTANCE;
		shoppingCartSubsystem.setCustomerProfile(customerProfile);
		shoppingCartSubsystem.retrieveSavedCart();
		loadOrderData();
    }
    
    void loadCustomerProfile(int id, boolean isAdmin) throws BackendException {
    	try {
			DbClassCustomerProfile dbclass = new DbClassCustomerProfile();
			dbclass.readCustomerProfile(id);
			customerProfile = dbclass.getCustomerProfile();
			customerProfile.setIsAdmin(isAdmin);
		} catch (DatabaseException e) {
			throw new BackendException(e);
		}
    }
    void loadDefaultShipAddress() throws BackendException {
    	//implement
    }
	void loadDefaultBillAddress() throws BackendException {
		//implement
	}
	void loadDefaultPaymentInfo() throws BackendException {
		//implement
	}
	void loadOrderData() throws BackendException {

		// retrieve the order history for the customer and store here
		orderSubsystem = new OrderSubsystemFacade(customerProfile);
		//orderHistory = orderSubsystem.getOrderHistory();
		
	
	}
    /**
     * Returns true if user has admin access
     */
    public boolean isAdmin() {
    	return customerProfile.isAdmin();
    }
    
    
    
    /** 
     * Use for saving an address created by user  
     */
    public void saveNewAddress(Address addr) throws BackendException {
    	try {
			DbClassAddress dbClass = new DbClassAddress();
			dbClass.setAddress(addr);
			dbClass.saveAddress(customerProfile);
		} catch(DatabaseException e) {
			throw new BackendException(e);
		}
    }
    
    public CustomerProfile getCustomerProfile() {

		return customerProfile;
	}

	public Address getDefaultShippingAddress() {
		return defaultShipAddress;
	}

	public Address getDefaultBillingAddress() {
		return defaultBillAddress;
	}
	public CreditCard getDefaultPaymentInfo() {
		return defaultPaymentInfo;
	}
 
    
    /** 
     * Use to supply all stored addresses of a customer when he wishes to select an
	 * address in ship/bill window 
	 */
    public List<Address> getAllAddresses() throws BackendException {
    	return new ArrayList<Address>();
    	//implement
    }

	public Address runAddressRules(Address addr) throws RuleException,
			BusinessException {

		Rules transferObject = new RulesAddress(addr);
		transferObject.runRules();

		// updates are in the form of a List; 0th object is the necessary
		// Address
		AddressImpl update = (AddressImpl) transferObject.getUpdates().get(0);
		return update;
	}

	public void runPaymentRules(Address addr, CreditCard cc)
			throws RuleException, BusinessException {
		Rules transferObject = new RulesPayment(addr, cc);
		transferObject.runRules();
	}
	
	
	public static Address createAddress(String street, String city,
			String state, String zip, boolean isShip, boolean isBill) {
		return new AddressImpl(street, city, state, zip, isShip, isBill);
	}

	public static CustomerProfile createCustProfile(Integer custid,
			String firstName, String lastName, boolean isAdmin) {
		return new CustomerProfileImpl(custid, firstName, lastName, isAdmin);
	}

	public static CreditCard createCreditCard(String nameOnCard,
			String expirationDate, String cardNum, String cardType) {
		return new CreditCardImpl(nameOnCard, expirationDate, cardNum, cardType);
	}
	
    public void setShippingAddressInCart(Address addr){
    	shoppingCartSubsystem.setShippingAddress(addr);
    }
    public List<Order> getOrderHistory(){
		return orderHistory;
    }
    public void setBillingAddressInCart(Address addr){
    	shoppingCartSubsystem.setBillingAddress(addr);
    }
   
    public void saveShoppingCart() throws BackendException{
    	shoppingCartSubsystem.saveLiveCart();
    }
    public void checkCreditCard(CreditCard cc) throws BusinessException{
    	try {
    		cvObj.checkCreditCard(customerProfile, defaultBillAddress, defaultPaymentInfo, 1234);
		} catch (MiddlewareException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	//amount
    }

    public ShoppingCartSubsystem getShoppingCart()
    {
    	return liveCart;
    	
    	
    }
    public void refreshAfterSubmit() throws BackendException{
    	
    	try {
    		subOrderObj.getOrderHistory();
		} catch (BackendException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    }
    
    public void setPaymentInfoInCart(CreditCard cc){
    	liveCart.setPaymentInfo(cc);
   	
    }
	
    public void submitOrder() throws BackendException{
      	subOrderObj.submitOrder(liveCart.getLiveCart());
    }

    public DbClassAddressForTest getGenericDbClassAddress()
    {
    	return defaultDbClassAddress;
    }
    public CustomerProfile getGenericCustomerProfile()
    {
    	return customerProfile;
    }
	
}
