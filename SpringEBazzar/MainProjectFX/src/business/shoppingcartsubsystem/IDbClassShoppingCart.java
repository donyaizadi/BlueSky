package business.shoppingcartsubsystem;

import middleware.exceptions.DatabaseException;
import business.externalinterfaces.CustomerProfile;
import business.externalinterfaces.ShoppingCart;

public interface IDbClassShoppingCart {
	public ShoppingCart retrieveSavedCart(CustomerProfile custProfile)
			throws DatabaseException;
	public void saveCart(CustomerProfile custProfile, ShoppingCart cart)
			throws DatabaseException;
}
