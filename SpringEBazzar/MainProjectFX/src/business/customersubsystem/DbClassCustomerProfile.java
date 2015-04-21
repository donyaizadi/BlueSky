package business.customersubsystem;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import javax.sql.DataSource;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import business.externalinterfaces.CustomerProfile;
import middleware.DbConfigProperties;
import middleware.dataaccess.DataAccessSubsystemFacade;
import middleware.exceptions.DatabaseException;
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = DatabaseException.class)
class DbClassCustomerProfile implements IDbClassCustomerProfile {
	private static final Logger LOG = Logger
			.getLogger(DbClassCustomerProfile.class.getPackage().getName());

	private final String READ = "Read";
	private Integer custId;
	String query;
	private String queryType;
	private CustomerProfileImpl customerProfile;
	private JdbcTemplate jdbcTemplate;

	public CustomerProfileImpl getCustomerProfile() {
		return customerProfile;
	}

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	// Template
	private class CustomerRowMapper implements RowMapper<CustomerProfileImpl> {
		public CustomerProfileImpl mapRow(ResultSet rs, int rowNum)
				throws SQLException {
			CustomerProfileImpl customer = new CustomerProfileImpl(
					rs.getInt("custid"), rs.getString("fname"),
					rs.getString("lname"));

			return customer;
		}
	}

	public void readCustomerProfile(Integer custId) throws DatabaseException {
		this.custId = custId;
		queryType = READ;
		buildQuery();
		customerProfile = jdbcTemplate.queryForObject(query,
				new CustomerRowMapper());
	}

	public void buildQuery() {

		if (queryType.equals(READ)) {
			buildReadQuery();
		}
		LOG.info("Query for " + queryType + ": " + query);
	}

	void buildReadQuery() {
		query = "SELECT custid,fname,lname " + "FROM Customer "
				+ "WHERE custid = " + custId;
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());

	}

	public String getQuery() {
		return query;

	}

}
