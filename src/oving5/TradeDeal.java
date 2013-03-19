package oving5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class TradeDeal implements Serializable {
	private static final long serialVersionUID = -6273972612945569502L;
	
	//Money to trade for item
	private final double tradeMoney;
	//Items to trade for item
	private final List<TradableItem> tradeItems = new ArrayList<TradableItem>();
	//Item trading for
	private final TradableItem item;
	
	private final String trader;
	private final String buyer;
	
	public TradeDeal(TradableItem item, double money, List<TradableItem> items, String seller, String buyer){
		this.item = item;
		this.tradeMoney = money;
		this.tradeItems.addAll(items);
		this.trader = seller;
		this.buyer = buyer;
	}
	
	public TradeDeal(TradableItem item, double money, String seller, String buyer){
		this(item, money, new ArrayList<TradableItem>(), seller, buyer);
	}

	public double getTradeMoney() {
		return tradeMoney;
	}

	public List<TradableItem> getTradeItems() {
		return tradeItems;
	}

	public TradableItem getItem() {
		return item;
	}

	public String getTrader() {
		return trader;
	}

	public String getBuyer() {
		return buyer;
	}
	
	public String toString(){
		return this.buyer + " wants to buy " + this.item + " from " + this.trader +
				" for " + this.tradeMoney + " and these items " + this.tradeItems;
	}
}
