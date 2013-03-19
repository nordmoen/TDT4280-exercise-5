package oving5;

import jade.core.Agent;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 9143718912851258654L;
	
	private final Set<TradableItem> wantedItems;
	private final List<TradableItem> inventory;
	private final List<TradableItem> obtained = new ArrayList<TradableItem>();
	private double money;
	
	/**
	 * Create a new TraderAgent
	 * @param wants - Items the agent want to obtain
	 * @param has - Items the trader has
	 * @param money - The amount of money
	 */
	public TraderAgent(Set<TradableItem> wants, List<TradableItem> has,
			double money){
		this.wantedItems = wants;
		this.inventory = has;
		this.money = money;
	}
	
	/**
	 * Generate a Trader with a number of random objects to have and to want
	 * @param numberWanted - The number of wanted items
	 * @param numberHave - The number of items the agent has
	 * @param money - The amount of money the agent has
	 */
	public TraderAgent(int numberWanted, int numberHave, double money){
		this(TradableItem.generateUnique(numberWanted), 
				TradableItem.generateItems(numberHave), money);
	}
	
	/**
	 * Create an agent with all attributes random
	 */
	public TraderAgent(){
		this((int)(Math.random()*TradableItem.maxUnique()), 
				(int)(Math.random()*100), Math.random()*1000);
	}

}
