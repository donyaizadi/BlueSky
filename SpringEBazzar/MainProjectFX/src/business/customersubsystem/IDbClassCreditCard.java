package business.customersubsystem;

import java.util.List;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.CreditCard;
import business.externalinterfaces.CustomerProfile;

public interface IDbClassCreditCard {
	public void readDefaultPaymentInfo(CustomerProfile custProfile) throws DatabaseException;
	public CreditCardImpl getDefaultPaymentInfo();
	public void readAllPayments(CustomerProfile custProfile) throws DatabaseException;
	public void savePayment(CustomerProfile custProfile) throws DatabaseException;
	public List<CreditCardImpl> getPaymentList();
}
