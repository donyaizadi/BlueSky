package business.shoppingcartsubsystem;

import java.util.HashMap;
import java.util.List;

import business.exceptions.BusinessException;
import business.exceptions.RuleException;
import business.externalinterfaces.DynamicBean;
import business.externalinterfaces.Rules;
import business.externalinterfaces.RulesConfigKey;
import business.externalinterfaces.RulesConfigProperties;
import business.externalinterfaces.RulesSubsystem;
import business.externalinterfaces.ShoppingCart;
import business.rulesbeans.ShoppingCartBean;
import business.rulesubsystem.RulesSubsystemFacade;

public class RulesShoppingCart implements Rules{
	
	private HashMap<String,DynamicBean> table;
	private DynamicBean bean;	
	private RulesConfigProperties config = new RulesConfigProperties();

	public RulesShoppingCart(ShoppingCart sc) {
		// TODO Auto-generated constructor stub
		bean = new ShoppingCartBean(sc);
	}
	@Override
	public String getModuleName() {
		// TODO Auto-generated method stub
		return config.getProperty(RulesConfigKey.SHOPCART_MODULE.getVal());
	}

	@Override
	public String getRulesFile() {
		// TODO Auto-generated method stub
		return config.getProperty(RulesConfigKey.SHOPCART_RULES_FILE.getVal());
	}

	@Override
	public void prepareData() {
		// TODO Auto-generated method stub
		table = new HashMap<String,DynamicBean>();		
		String deftemplate = config.getProperty(RulesConfigKey.SHOPCART_DEFTEMPLATE.getVal());
		table.put(deftemplate, bean);	
	}

	@Override
	public HashMap<String, DynamicBean> getTable() {
		// TODO Auto-generated method stub
		return table;
	}

	@Override
	public void runRules() throws BusinessException, RuleException {
		// TODO Auto-generated method stub
		RulesSubsystem rules = new RulesSubsystemFacade();
    	rules.runRules(this);
	}

	@Override
	public void populateEntities(List<String> updates) {
		// do nothing
		
	}

	@Override
	public List<?> getUpdates() {
		// do nothing
		return null;
	}

}
