package business.customersubsystem;

import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;

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
import middleware.externalinterfaces.DataAccessSubsystem;
import middleware.externalinterfaces.DbClass;
import middleware.externalinterfaces.DbConfigKey;

@Repository
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = DatabaseException.class)
class DbClassCreditCard implements IDbClassCreditCard {
	private static final Logger LOG = Logger.getLogger(DbClassCreditCard.class
			.getPackage().getName());
	protected CustomerProfile custProfile;
	private CreditCard payment;
	private List<CreditCardImpl> paymentList;
	private CreditCardImpl defaultPaymentInfo;
	private String queryType;
	private String query;
	private JdbcTemplate jdbcTemplate;
	private final String SAVE = "Save";
	private final String READ = "Read";
	private final String READ_DEFAULT_PAYMENT = "ReadDefaultPayment";

	// column names
	private final String EXP_DATE = "expdate";
	private final String CARD_TYPE = "cardtype";
	private final String CARD_NUM = "cardnum";

	@Inject
	@Named("dataSourceAccounts")
	public void setDataSource(DataSource dataSource) {
		this.jdbcTemplate = new JdbcTemplate(dataSource);
	}

	private class PaymentRowMapper implements
			RowMapper<CreditCardImpl> {
		public CreditCardImpl mapRow(ResultSet rs, int rowNum) throws SQLException {
			
			CreditCardImpl cc= new CreditCardImpl(
					custProfile.getFirstName(),
					rs.getString(EXP_DATE), rs.getString(CARD_NUM),
					rs.getString(CARD_TYPE));
			return cc;
		}
	}

	public void savePayment(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = SAVE;
		buildQuery();
		jdbcTemplate.update(query);
	}

	public void buildQuery() {
		if (queryType.equals(SAVE)) {
			buildSaveNewPaymentQuery();
		} else if (queryType.equals(READ)) {
			buildReadAllPaymentsQuery();
		} else if (queryType.equals(READ_DEFAULT_PAYMENT)) {
			buildReadDefaultPaymentQuery();
		}
		LOG.info("query build "+query);
	}

	CreditCard getPayment() {
		return payment;
	}

	public List<CreditCardImpl> getPaymentList() {
		return paymentList;
	}

	public CreditCardImpl getDefaultPaymentInfo() {
		return this.defaultPaymentInfo;
	}

	public void readDefaultPaymentInfo(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = READ_DEFAULT_PAYMENT;
		buildQuery();
		defaultPaymentInfo = jdbcTemplate.queryForObject(query, new PaymentRowMapper());
	}

	public void readAllPayments(CustomerProfile custProfile)
			throws DatabaseException {
		this.custProfile = custProfile;
		queryType = READ;
		buildQuery();
		paymentList = jdbcTemplate.query(query, new PaymentRowMapper());
	}

	void setCreditCard(CreditCard payment) {
		this.payment = payment;
	}

	void buildSaveNewPaymentQuery() {
		query = "INSERT into altpayment " + "(paymentid,custid," + EXP_DATE
				+ "," + CARD_TYPE + "," + CARD_NUM + ") " + "VALUES(NULL,"
				+ custProfile.getCustId() + ",'" + payment.getExpirationDate()
				+ "','" + payment.getCardType() + "','" + payment.getCardNum()
				+ "')";
	}

	void buildReadAllPaymentsQuery() {
		query = "SELECT " + EXP_DATE + ", " + CARD_TYPE + ", " + CARD_NUM
				+ " from altpayment";
	}

	void buildReadDefaultPaymentQuery() {
		query = "SELECT " + EXP_DATE + ", " + CARD_TYPE + ", " + CARD_NUM
				+ " FROM customer WHERE custid = " + custProfile.getCustId();
	}

	public String getDbUrl() {
		DbConfigProperties props = new DbConfigProperties();
		return props.getProperty(DbConfigKey.ACCOUNT_DB_URL.getVal());
	}

	public String getQuery() {
		return query;
	}
}
