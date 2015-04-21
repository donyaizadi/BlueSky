package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DbConfigKey;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.DbClassAddressForTest;
@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = DatabaseException.class)
class DbClassAddress implements DbClassAddressForTest, IDbClassAddress {
	private static final Logger LOG = Logger.getLogger(DbClassAddress.class
			.getName());
	private DataAccessSubsystem dataAccessSS = new DataAccessSubsystemFacade();
	private CustomerProfile custProfile;
	private Address address;
	private List<Address> addressList;
	private AddressImpl defaultShipAddress;
	private AddressImpl defaultBillAddress;
	private String queryType;
	private String query;

	private final String SAVE = "Save";
	private final String READ = "Read";
	private final String READ_DEFAULT_BILL = "ReadDefaultBill";
	private final String READ_DEFAULT_SHIP = "ReadDefaultShip";

	// column names
	private final String STREET = "street";
	private final String CITY = "city";
	private final String STATE = "state";
	private final String ZIP = "zip";
	private final String ISSHIP = "isship";
	private final String ISBILL = "isbill";
	private JdbcTemplate jdbcTemplate;

	public void saveAddress(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = SAVE;
		buildQuery();
		jdbcTemplate.update(query);
	}

	public void buildQuery() throws DatabaseException {
		LOG.info("Query  for " + queryType + ": " + query);
		if (queryType.equals(SAVE)) {
			buildSaveNewAddrQuery();
		} else if (queryType.equals(READ)) {
			buildReadAllAddressesQuery();
		} else if (queryType.equals(READ_DEFAULT_BILL)) {
			buildReadDefaultBillQuery();
		} else if (queryType.equals(READ_DEFAULT_SHIP)) {
			buildReadDefaultShipQuery();
		}
	}

	Address getAddress() {
		return address;
	}

	public List<Address> getAddressList() {
		return addressList;
	}

	public AddressImpl getDefaultShipAddress() {
		return this.defaultShipAddress;
	}

	public AddressImpl getDefaultBillAddress() {
		return this.defaultBillAddress;
	}

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private class DefaultShippingAddressRowMapper implements
			RowMapper<AddressImpl> {
		public AddressImpl mapRow(ResultSet rs, int rowNum) throws SQLException {
			AddressImpl shipAddress = new AddressImpl(
					rs.getString("shipaddress1"), rs.getString("shipcity"),
					rs.getString("shipstate"), rs.getString("shipzipcode"),
					true, false);
			return shipAddress;
		}
	}

	private class DefaultBillingAddressRowMapper implements
			RowMapper<AddressImpl> {
		public AddressImpl mapRow(ResultSet rs, int rowNum) throws SQLException {
			AddressImpl shipAddress = new AddressImpl(
					rs.getString("billaddress1"), rs.getString("billcity"),
					rs.getString("billstate"), rs.getString("billzipcode"),
					false, true);
			return shipAddress;
		}
	}

	private class AllAddressesRowMapper implements RowMapper<Address> {
		public AddressImpl mapRow(ResultSet rs, int rowNum) throws SQLException {
			AddressImpl allAddress = new AddressImpl(rs.getBoolean(ISSHIP),
					rs.getBoolean(ISBILL));
			String str = rs.getString(STREET);
			allAddress.setStreet(str);
			allAddress.setCity(rs.getString(CITY));
			allAddress.setState(rs.getString(STATE));
			allAddress.setZip(rs.getString(ZIP));
			return allAddress;
		}
	}

	public void readDefaultShipAddress(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = READ_DEFAULT_SHIP;
		buildQuery();
		populateEntity(null);
	}

	public void readDefaultBillAddress(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = READ_DEFAULT_BILL;
		buildQuery();
		populateEntity(null);
	}

	public void readAllAddresses(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = READ;
		buildQuery();
		populateEntity(null);
	}

	public void setAddress(Address addr) {
		address = addr;
	}

	void buildReadCustNameQuery() {
		query = "SELECT fname, lname " + "FROM Customer " + "WHERE custid = "
				+ custProfile.getCustId();
	}

	void buildSaveNewAddrQuery() throws DatabaseException {
		int shipAdd = (address.isShippingAddress()) ? 1 : 0;
		int billAdd = (address.isBillingAddress()) ? 1 : 0;

		query = "INSERT into altaddress "
				+ "(custid,street,city,state,zip, isship, isbill) " + "VALUES("
				+ custProfile.getCustId() + ",'" + address.getStreet() + "','"
				+ address.getCity() + "','" + address.getState() + "','"
				+ address.getZip() + "','" + shipAdd + "','" + billAdd + "')";

		System.out.println("test your query" + query);
	}

	void buildReadAllAddressesQuery() {
		query = "SELECT * from altaddress WHERE custid = "
				+ custProfile.getCustId();
	}

	void buildReadDefaultBillQuery() {
		query = "SELECT billaddress1, billaddress2, billcity, billstate, billzipcode "
				+ "FROM Customer "
				+ "WHERE custid = "
				+ custProfile.getCustId();
	}

	void buildReadDefaultShipQuery() {
		query = "SELECT shipaddress1, shipaddress2, shipcity, shipstate, shipzipcode "
				+ "FROM Customer "
				+ "WHERE custid = "
				+ custProfile.getCustId();
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());

	}

	public String getQuery() {
		return query;

	}

	void populateAddressList(ResultSet rs) throws DatabaseException {
		addressList = new LinkedList<Address>();
		addressList = jdbcTemplate.query(query, new AllAddressesRowMapper());
	}

	void populateDefaultShipAddress(ResultSet rs) throws DatabaseException {
		// implement by Arun

		defaultShipAddress = jdbcTemplate.queryForObject(query,
				new DefaultShippingAddressRowMapper());

	}

	void populateDefaultBillAddress(ResultSet rs) throws DatabaseException {
		// implement by Arun
				defaultBillAddress = jdbcTemplate.queryForObject(query,
						new DefaultBillingAddressRowMapper());
			
	}

	/*
	 * used only when we read from the database
	 */
	public void populateEntity(ResultSet rs) throws DatabaseException {
		if (queryType.equals(READ)) {
			populateAddressList(rs);
		} else if (queryType.equals(READ_DEFAULT_SHIP)) {
			populateDefaultShipAddress(rs);
		} else if (queryType.equals(this.READ_DEFAULT_BILL)) {
			populateDefaultBillAddress(rs);
		}
	}


}
