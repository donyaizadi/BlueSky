package business.customersubsystem;

import java.util.List;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.Address;
import business.externalinterfaces.CustomerProfile;

public interface IDbClassAddress {
	public void readDefaultShipAddress(CustomerProfile custProfile) throws DatabaseException;
	public AddressImpl getDefaultShipAddress();
	public void readDefaultBillAddress(CustomerProfile custProfile) throws DatabaseException;
	public AddressImpl getDefaultBillAddress();
	public void setAddress(Address addr);
	public void saveAddress(CustomerProfile custProfile) throws DatabaseException;
	public void readAllAddresses(CustomerProfile custProfile) throws DatabaseException;
	public List<Address> getAddressList();
}
