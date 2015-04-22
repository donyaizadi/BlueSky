package test;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import middleware.DbConfigProperties;
import middleware.externalinterfaces.DbConfigKey;
import test.alltests.AllTests;
import business.customersubsystem.CustomerSubsystemFacade;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.Order;
import business.externalinterfaces.OrderItem;
import business.ordersubsystem.OrderImpl;
import business.ordersubsystem.OrderItemImpl;
import business.util.Util;

public class DbQueries {
	static {
		AllTests.initializeProperties();
	}
	
	static final DbConfigProperties PROPS = new DbConfigProperties();
	static Connection con = null;
	static Statement stmt = null;
	static final String USER = PROPS.getProperty(DbConfigKey.DB_USER.getVal()); 
    static final String PWD = PROPS.getProperty(DbConfigKey.DB_PASSWORD.getVal()); 
    static final String DRIVER = PROPS.getProperty(DbConfigKey.DRIVER.getVal());
    static final int MAX_CONN = Integer.parseInt(PROPS.getProperty(DbConfigKey.MAX_CONNECTIONS.getVal()));
    static final String PROD_DBURL = PROPS.getProperty(DbConfigKey.PRODUCT_DB_URL.getVal());
    static final String ACCT_DBURL = PROPS.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
	static Connection prodCon = null;
	static Connection acctCon = null;
    String insertStmt = "";
	String selectStmt = "";
	
	/* Connection setup */
	static {
		try {
			Class.forName(DRIVER);
		}
		catch(ClassNotFoundException e){
			//debug
			e.printStackTrace();
		}
		try {
			prodCon = DriverManager.getConnection(PROD_DBURL, USER, PWD);
			acctCon = DriverManager.getConnection(ACCT_DBURL, USER, PWD);
		}
		catch(SQLException e){
			System.out.println(e.getMessage());
			e.printStackTrace();
		}
	}
	// just to test this class
	public static void testing() {
		try {
			stmt = prodCon.createStatement();
			stmt.executeQuery("SELECT * FROM Product");
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	//////////////// The public methods to be used in the unit tests ////////////
	/**
	 * Returns a String[] with values:
	 * 0 - query
	 * 1 - product id
	 * 2 - product name
	 */
	public static String[] insertProductRow() {
		String[] vals = saveProductSql();
		String query = vals[0];
		try {
			stmt = prodCon.createStatement();
			
			stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next()) {
				vals[1] = (new Integer(rs.getInt(1)).toString());
			}
			stmt.close();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return vals;
	}
	
	/**
	 * Returns a String[] with values:
	 * 0 - query
	 * 1 - catalog id
	 * 2 - catalog name
	 */
	public static List<Address> readCustAddresses() {
		String query = readAddressesSql();
		List<Address> addressList = new LinkedList<Address>();
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			    
                while(rs.next()) {
                    
                    String street = rs.getString("custid");
                    String city = rs.getString("city");
                    String state = rs.getString("state");
                    String zip = rs.getString("zip");
                    
             
                    Address addr 
                      = CustomerSubsystemFacade.createAddress(street,city,state,zip,true,true);
                   
                    addressList.add(addr);
                }  
                stmt.close();
                
                    
	            
		}
		catch(SQLException e) {
			e.printStackTrace();
			
		}
		return addressList;
		
	}
	

	
	
	public static void insertCustomerObj()
	{
		String query = insertCustObjSql();
		try {
			stmt = acctCon.createStatement();
			stmt.executeUpdate(query);
			stmt.close();
		}catch(SQLException e) {
			e.printStackTrace();
			
		}
	}
	
	
	
	public static int readLatestCustId()
	{
		String query = "SELECT MAX(custId) FROM customer";
		int id = 0;
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
			{
				id = rs.getInt("MAX(custId)");
			}
			stmt.close();
                
                    
	            
		}
		catch(SQLException e) {
			e.printStackTrace();
			
		}
		return id;
		
	}
	
	
	public static String readShipmentAddresses(int id) {
		String query = readShipmentAddressesSql(id);
		String str = "";
		try 
		{
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
			{
				str = rs.getString("shipaddress1");
			}
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
			
		}
		return str;

		
		
	}
	
	public static int readCustomerProfile() {
		String query = readCustProfileSql();
		int id = 0;
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next())
			{
				id = rs.getInt("custid");
			}
			stmt.close();
                
                    
	            
		}
		catch(SQLException e) {
			e.printStackTrace();
			
		}
		return id;
		
	}
	
		
	public static int readIdShoppingCart(){
		String query = readSavedShoppingCartSql();
		int id = 0;
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			
			    
                if(rs.next()) {
                 id = rs.getInt("shopcartid");   
                }  
                stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
			
		}
		return id;
	}
	
	public static List<Integer> readAllOrderIds(CustomerProfile customPro){
		List<Integer> orderIds = new ArrayList<Integer>();
		String query = readOrderIdsSql(customPro.getCustId());
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
            while(rs.next()){
            	orderIds.add(rs.getInt("orderid"));
            }
           stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return orderIds;
	}
	
	public static List<Order> readAllOrders(CustomerProfile customPro){
		List<Order> orders = new ArrayList<Order>();
		List<Integer> orderIds = readAllOrderIds(customPro);
		for (int orderId : orderIds) {
			List<OrderItem> orderItems = getOrderItems(orderId);
			Order order = getOrderData(orderId);
			order.setOrderItems(orderItems);
			order.setOrderId(orderId);
			orders.add(order);
		}
		return orders;
	}
	
	public static List<OrderItem> getOrderItems(Integer orderId){	
		List<OrderItem> orderItems = new LinkedList<OrderItem>();
		String query = readOrderItems(orderId);
    	try {
    		stmt = acctCon.createStatement();
    		ResultSet rs = stmt.executeQuery(query);
			while(rs.next()){
				//Escape getting product info from productSubsystem
				OrderItem item= new OrderItemImpl("pTestName", rs.getInt("quantity"), 10.0);
					orderItems.add(item);
			}
		} catch (SQLException e) {
			e.printStackTrace();
		}
    	return orderItems;
	}
		
	
	public static Order getOrderData(Integer orderId){
		Order orderData = new OrderImpl();
		String query = readOrderData(orderId);
		try {
			stmt = acctCon.createStatement();
			ResultSet rs = stmt.executeQuery(query);
			if(rs.next()){
				orderData.setDate(Util.localDateForString(rs.getString("orderdate")));
		}
		stmt.close();
		} catch(SQLException e) {
			e.printStackTrace();
		}
		return orderData;
	}
	
	/**
	 * Returns a String[] with values:
	 * 0 - query
	 * 1 - catalog id
	 * 2 - catalog name
	 */
	public static String[] insertCatalogRow() {
		String[] vals = saveCatalogSql();
		String query = vals[0];
		try {
			stmt = prodCon.createStatement();
			stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next()) {
				vals[1] = (new Integer(rs.getInt(1)).toString());
			}
			stmt.close();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return vals;
	}
	
	/**
	 * Returns a String[] with values:
	 * 0 - query
	 * 1 - customer id
	 * 2 - cust fname
	 * 3 - cust lname
	 */
	public static String[] insertCustomerRow() {
		String[] vals = saveCustomerSql();
		String query = vals[0];
		try {
			stmt = acctCon.createStatement();
			stmt.executeUpdate(query,Statement.RETURN_GENERATED_KEYS);
			ResultSet rs = stmt.getGeneratedKeys();
			if(rs.next()) {
				vals[1] = (new Integer(rs.getInt(1)).toString());
			}
			stmt.close();
			
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return vals;
	}
	public static void deleteCatalogRow(Integer catId) {
		try {
			stmt = prodCon.createStatement();
			stmt.executeUpdate(deleteCatalogSql(catId));
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	public static void deleteProductRow(Integer prodId) {
		try {
			stmt = prodCon.createStatement();
			stmt.executeUpdate(deleteProductSql(prodId));
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	public static String getCatalogByIdSql(Integer catalogId) {
		String name = "";
		try {
			stmt = prodCon.createStatement();
			ResultSet rs = stmt.executeQuery(readCatalogByIdSql(catalogId));
			if(rs.next())
			{
				name = rs.getString("catalogname");
			}
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
		return name;
	}
	
	
	public static void deleteCustomerRow(Integer custId) {
		try {
			stmt = acctCon.createStatement();
			stmt.executeUpdate(deleteCustomerSql(custId));
			stmt.close();
		}
		catch(SQLException e) {
			e.printStackTrace();
		}
	}
	
	
	///queries
	public static String readAddressesSql() {
		return "SELECT * from altaddress WHERE custid = 1";
	}
	public static String readCustProfileSql() {
		return "SELECT * from customer WHERE custid= 1";
	}
	
	public static String readShipmentAddressesSql(int id)
	{
		return "SELECT shipaddress1 FROM `Customer` WHERE custId = "+ id;
	}
	
	public static String insertCustObjSql()
	{
		return "INSERT INTO Customer VALUES (null,null,null,null,null,null,null,'81000 N. 4th St.',null,null,null,null,null,null,null,null,null,null,null,null,null)";
	}
	

	public static String readSavedShoppingCartSql(){
		return "SELECT * from shopcarttbl WHERE custid= 1";
	}
	
	public static String readOrderIdsSql(Integer customerId){
		return "SELECT orderid FROM Ord WHERE custid = "+customerId;
	}

    private static String readOrderData(Integer orderId) {
        return "SELECT orderdate, totalpriceamount FROM Ord WHERE orderid = " + String.valueOf(orderId);     
    }
  
    private static String readOrderItems(Integer orderId) {
        return "SELECT * FROM OrderItem WHERE orderid = " + String.valueOf(orderId); 
    }

	public static String[] saveCatalogSql() {
		String[] vals = new String[3];
		
		String name = "testcatalog";
		vals[0] =
		"INSERT into CatalogType "+
		"(catalogid,catalogname) " +
		"VALUES(NULL, '" + name+"')";	  
		vals[1] = null;
		vals[2] = name;
		return vals;
	}
	public static String[] saveProductSql() {
		String[] vals = new String[3];
		String name = "testprod";
		vals[0] =
		"INSERT into Product "+
		"(productid,productname,totalquantity,priceperunit,mfgdate,catalogid,description) " +
		"VALUES(NULL, '" +
				  name+"',1,1,'12/12/00',1,'test')";				  
		vals[1] = null;
		vals[2] = name;
		return vals;
	}
	public static String[] saveCustomerSql() {
		String[] vals = new String[4];
		String fname = "testf";
		String lname = "testl";
		vals[0] =
		"INSERT into Customer "+
		"(custid,fname,lname) " +
		"VALUES(NULL,'" +
				  fname+"','"+ lname+"')";
				  
		vals[2] = fname;
		vals[3] = lname;
		return vals;
	}
	public static String deleteProductSql(Integer prodId) {
		return "DELETE FROM Product WHERE productid = "+prodId;
	}
	
	public static String readCatalogByIdSql(Integer catalogId) {
		return "SELECT * FROM CatalogType WHERE catalogid = "+ String.valueOf(catalogId);
	}

	public static String deleteCatalogSql(Integer catId) {
		return "DELETE FROM CatalogType WHERE catalogid = "+catId;
	}
	
	public static String deleteCustomerSql(Integer custId) {
		return "DELETE FROM Customer WHERE custid = "+custId;
	}
	
	public static void main(String[] args) {
		readAddressesSql();
		//System.out.println(System.getProperty("user.dir"));
		/*
		String[] results = DbQueries.insertCustomerRow();
		System.out.println("id = " + results[1]);
		DbQueries.deleteCustomerRow(Integer.parseInt(results[1]));
		results = DbQueries.insertCatalogRow();
		System.out.println("id = " + Integer.parseInt(results[1]));
		DbQueries.deleteCatalogRow(Integer.parseInt(results[1]));*/
	}
}
