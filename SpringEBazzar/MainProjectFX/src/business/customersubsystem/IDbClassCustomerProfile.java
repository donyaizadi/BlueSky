package business.customersubsystem;

import middleware.exceptions.DatabaseException;

public interface IDbClassCustomerProfile {
	public void readCustomerProfile(Integer custId) throws DatabaseException;
	public CustomerProfileImpl getCustomerProfile();
}
