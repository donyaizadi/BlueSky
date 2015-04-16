package business.rulesbeans;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

import business.externalinterfaces.DynamicBean;
import business.externalinterfaces.ShoppingCart;

public class ShoppingCartBean implements DynamicBean{
	private ShoppingCart sp;
	
	public ShoppingCartBean(ShoppingCart sp) {
		// TODO Auto-generated constructor stub
		this.sp=sp;
	}
	
	
	 
	private PropertyChangeSupport pcs = 
	    	new PropertyChangeSupport(this);
	    public void addPropertyChangeListener(PropertyChangeListener pcl){
		 	pcs.addPropertyChangeListener(pcl);
		}
		public void removePropertyChangeListener(PropertyChangeListener pcl){	
	    	pcs.removePropertyChangeListener(pcl);
	    }

}
