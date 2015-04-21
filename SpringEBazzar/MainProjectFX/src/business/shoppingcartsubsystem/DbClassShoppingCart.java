package business.shoppingcartsubsystem;

import static business.util.StringParse.makeString;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.mysql.jdbc.Statement;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;
import business.customersubsystem.CustomerSubsystemFacade;
import business.exceptions.BackendException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CartItem;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.DbClassShoppingCartForTest;
import business.externalinterfaces.ShoppingCart;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = DatabaseException.class)
public class DbClassShoppingCart implements DbClass,
		DbClassShoppingCartForTest, IDbClassShoppingCart {
	private static final Logger LOG = Logger
			.getLogger(DbClassShoppingCart.class.getPackage().getName());
	private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();

	DataAccessSubsystem dataAccess;
	ShoppingCartImpl cartImpl;
	ShoppingCart cart;
	CartItem cartItem;
	List<CartItem> cartItemsList;
	CustomerProfile custProfile;
	Integer cartId;
	String query;
	final String GET_ID = "GetId";
	final String GET_SAVED_ITEMS = "GetSavedItems";
	final String GET_TOP_LEVEL_SAVED_CART = "GetTopLevelSavedCart";
	final String SAVE_CART = "SaveCart";
	final String SAVE_CART_ITEM = "SaveCartItem";
	final String DELETE_CART = "DeleteCart";
	final String DELETE_ALL_CART_ITEMS = "DeleteAllCartItems";
	String queryType;
	private JdbcTemplate jdbcTemplate;

	public void buildQuery() {
		if (queryType.equals(GET_ID)) {
			buildGetIdQuery();
		} else if (queryType.equals(GET_SAVED_ITEMS)) {
			buildGetSavedItemsQuery();
		} else if (queryType.equals(GET_TOP_LEVEL_SAVED_CART)) {
			this.buildGetTopLevelCartQuery();
		} else if (queryType.equals(SAVE_CART)) {
			buildSaveCartQuery();
		} else if (queryType.equals(SAVE_CART_ITEM)) {
			buildSaveCartItemQuery();
		} else if (queryType.equals(DELETE_CART)) {
			buildDeleteCartQuery();
		} else if (queryType.equals(DELETE_ALL_CART_ITEMS)) {
			buildDeleteAllCartItemsQuery();
		}
	}

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		System.out.println("jdbcTemplate is null? " + (jdbcTemplate == null));
	}

	private void buildGetIdQuery() {
		query = "SELECT shopcartid " + "FROM ShopCartTbl " + "WHERE custid = "
				+ custProfile.getCustId();

	}

	private void buildDeleteCartQuery() {
		query = "DELETE FROM shopcarttbl WHERE shopcartid = "
				+ cartId.intValue();
	}

	private void buildDeleteAllCartItemsQuery() {
		query = "DELETE FROM shopcartitem WHERE shopcartid = "
				+ cartId.intValue();
	}

	// precondition: cart and custprofile has been stored as instance variable
	private void buildSaveCartQuery() {
		query = "INSERT INTO shopcarttbl (custid,shipaddress1, "
				+ "shipaddress2, shipcity, shipstate, shipzipcode, billaddress1, "
				+ "billaddress2, billcity, billstate, billzipcode, nameoncard, "
				+ "expdate,cardtype, cardnum, totalpriceamount, totalshipmentcost, "
				+ "totaltaxamount, totalamountcharged) " + "VALUES ("
				+ custProfile.getCustId()
				+ ", '"
				+ cart.getShippingAddress().getStreet()
				+ "', '','"
				+ ""
				+ cart.getShippingAddress().getCity()
				+ "', '"
				+ cart.getShippingAddress().getState()
				+ "', '"
				+ cart.getShippingAddress().getZip()
				+ "', '"
				+ cart.getBillingAddress().getStreet()
				+ "', '"
				+ ""
				+ "', '"
				+ cart.getBillingAddress().getCity()
				+ "', '"
				+ cart.getBillingAddress().getState()
				+ "', '"
				+ cart.getBillingAddress().getZip()
				+ "', '"
				+ cart.getPaymentInfo().getNameOnCard()
				+ "', '"
				+ cart.getPaymentInfo().getExpirationDate()
				+ "', '"
				+ cart.getPaymentInfo().getCardType()
				+ "', '"
				+ cart.getPaymentInfo().getCardNum()
				+ "', '"
				+ (new Double(cart.getTotalPrice())).toString()
				+ "',"
				+ "'0.00', '0.00', '"
				+ (new Double(cart.getTotalPrice())).toString() + "')";

	}

	private void buildSaveCartItemQuery() {
		query = "INSERT INTO shopcartitem (cartitemid, shopcartid, productid, quantity, totalprice, shipmentcost, taxamount) "
				+ "VALUES (NULL, "
				+ cartItem.getCartid()
				+ ", "
				+ cartItem.getProductid()
				+ ", '"
				+ cartItem.getQuantity()
				+ "', '" + cartItem.getTotalprice() + "', '0','0')";

	}

	private void buildGetSavedItemsQuery() {
		query = "SELECT * FROM shopcartitem WHERE shopcartid = " + cartId;

	}

	private void buildGetTopLevelCartQuery() {
		query = "SELECT * FROM shopcarttbl WHERE shopcartid = " + cartId;

	}

	// Support method for saveCart -- part of another transaction
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, rollbackFor = DatabaseException.class)
	private void deleteCart(Integer cartId) throws DatabaseException {
		this.cartId = cartId;
		queryType = DELETE_CART;
		buildQuery();
		jdbcTemplate.update(query);

	}

	// Support method for saveCart -- part of another transaction
	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = false, rollbackFor = DatabaseException.class)
	private void deleteAllCartItems(Integer cartId) throws DatabaseException {
		this.cartId = cartId;
		queryType = DELETE_ALL_CART_ITEMS;
		buildQuery();
		jdbcTemplate.update(query);
	}

	public ShoppingCart retrieveSavedCart(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		// dataAccessSS.createConnection(this);
		// dataAccessSS.startTransaction();
		try {
			// First, get cartId
			Integer val = getShoppingCartId(custProfile);

			// Second, get top level cart data
			if (val != null) {
				ShoppingCartImpl cart = getTopLevelSavedCart(val);

				// Last, get cart items associated with this cart id, and insert
				// into cart
				List<CartItem> items = getSavedCartItems(val);
				cart.setCartItems(items);

				return cart;
			}

		} catch (DatabaseException e) {
			dataAccessSS.rollback();
			LOG.warning("Rolling back...");
			throw (e);
		} catch (EmptyResultDataAccessException x) {
			LOG.warning("No shop cart found");
		} finally {
			// dataAccessSS.releaseConnection(this);
			// dataAccessSS.releaseConnection();
		}
		return new ShoppingCartImpl();
	}

	// support method for retrieveSavedCart
	private Integer getShoppingCartId(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = GET_ID;
		buildQuery();
		return jdbcTemplate.queryForObject(query, new IdCartMapper());
	}

	// support method for retrieveSavedCart
	private List<CartItem> getSavedCartItems(Integer cartId)
			throws DatabaseException {
		this.cartId = cartId;
		queryType = GET_SAVED_ITEMS;
		buildQuery();
		return jdbcTemplate.query(query, new ShoppingCartItemMapper());

	}

	private class ShoppingCartMapper implements RowMapper<ShoppingCartImpl> {
		public ShoppingCartImpl mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			ShoppingCartImpl shopCrt = new ShoppingCartImpl();
			shopCrt.setCartId(rs.getString("shopcartid"));

			return shopCrt;
		}
	}

	private class ShoppingCartItemMapper implements RowMapper<CartItem> {
		public CartItem mapRow(ResultSet rs, int rowNum) throws SQLException {
			CartItemImpl shopCrt = new CartItemImpl();
			Integer cartitemid = rs.getInt("cartitemid");
			Integer productid = rs.getInt("productid");
			Integer lineitemid = rs.getInt("shopcartid");
			String quantity = rs.getString("quantity");
			String totalprice = makeString(rs.getDouble("totalprice"));
			try {
				shopCrt = new CartItemImpl(cartitemid,productid,lineitemid,quantity,totalprice,true);
			} catch (BackendException e) {
				// TODO Auto-generated catch block
				LOG.warning(e.getMessage());
			}
			return shopCrt;
		}
	}

	private class IdCartMapper implements RowMapper<Integer> {
		public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
			CartItemImpl shopCrt = new CartItemImpl();
			shopCrt.cartItemId = rs.getInt("shopcartid");
			return shopCrt.cartItemId;
		}
	}

	// support method for retrieveSavedCart
	private ShoppingCartImpl getTopLevelSavedCart(Integer cartId)
			throws DatabaseException {
		this.cartId = cartId;
		queryType = GET_TOP_LEVEL_SAVED_CART;
		buildQuery();
		return jdbcTemplate.queryForObject(query, new ShoppingCartMapper());
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW, readOnly = true, rollbackFor = DatabaseException.class)
	public void saveCart(CustomerProfile custProfile, ShoppingCart cart)
			throws DatabaseException {
		Integer cartId = null;
		this.cart = cart;
		this.custProfile = custProfile;
		List<CartItem> cartItems = cart.getCartItems();

		// Begin transaction
		Integer oldCartId = null;
		try {
			 oldCartId = getShoppingCartId(custProfile);
		} catch (EmptyResultDataAccessException e) {
			LOG.warning("No shop cart found");
		}
		try {

			// If customer has a saved cart already, get its cartId -- will
			// delete
			// this cart as part of the transaction

			//
			// First, delete old cart in two steps
			if (oldCartId != null) {
				deleteCart(oldCartId);
				deleteAllCartItems(oldCartId);
			}

			// Second, save top level of cart to be saved
			cartId = saveCartTopLevel();

			// Finally, save the associated cartitems in a loop
			// We have the cartId for these cartitems
			for (CartItem item : cartItems) {
				item.setCartId(cartId);
				saveCartItem(item);
			}

		} catch (DatabaseException e) {
			dataAccessSS.rollback();
			LOG.warning("Rolling back...");
			throw (e);
		} finally {
			// dataAccessSS.releaseConnection(this);
			// dataAccessSS.releaseConnection();
		}
	}

	public int saveCartTopLevel() throws DatabaseException {
		queryType = SAVE_CART;
		// testing
		buildQuery();
		LOG.info(query);
		jdbcTemplate.update(query);
		int shopCartId = jdbcTemplate.queryForObject( "SELECT MAX(shopcartid) as shopcartid FROM shopcarttbl", new IdCartMapper());
		return shopCartId;
	}

	/* This is part of the general saveCart transaction */
	private void saveCartItem(CartItem item) throws DatabaseException {
		this.cartItem = item;
		queryType = SAVE_CART_ITEM;
		// testing
		buildQuery();
		jdbcTemplate.update(query);
	}

	public void populateEntity(ResultSet resultSet) throws DatabaseException {
		if (queryType.equals(GET_ID)) {
			populateShopCartId(resultSet);
		} else if (queryType.equals(GET_SAVED_ITEMS)) {
			try {
				populateCartItemsList(resultSet);
			} catch (BackendException e) {
				throw new DatabaseException(e);
			}
		} else if (queryType.equals(GET_TOP_LEVEL_SAVED_CART)) {
			populateTopLevelCart(resultSet);
		}

	}

	private void populateShopCartId(ResultSet rs) {
		try {
			if (rs.next()) {
				cartId = rs.getInt("shopcartid");
			}
		} catch (SQLException e) {
			// do nothing
		}
	}

	private void populateTopLevelCart(ResultSet rs) throws DatabaseException {
		cartImpl = new ShoppingCartImpl();
		cartImpl = jdbcTemplate.queryForObject(query, new ShoppingCartMapper());
	}

	private void populateCartItemsList(ResultSet rs) throws BackendException {
		cartItemsList = new LinkedList<CartItem>();
		cartItemsList = jdbcTemplate.query(query, new ShoppingCartItemMapper());
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
	}

	public String getQuery() {
		return query;
	}

}
