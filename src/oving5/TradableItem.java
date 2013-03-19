package oving5;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class TradableItem implements Serializable{
	private static final long serialVersionUID = -141662398742511618L;
	private final double value;
	private final String name;
	private static String[] names = {"Book", "Letter", "TV", "Table", "Oven", 
		"Laptop", "Tablet", "Phone", "Mice", "Keyboard", "Playstation", "Juice",
		"Xbox"};
	
	public TradableItem(double val, String name){
		this.value = val;
		this.name = name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		long temp;
		temp = Double.doubleToLongBits(value);
		result = prime * result + (int) (temp ^ (temp >>> 32));
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TradableItem other = (TradableItem) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (Double.doubleToLongBits(value) != Double
				.doubleToLongBits(other.value))
			return false;
		return true;
	}

	public double getValue() {
		return value;
	}

	public String getName() {
		return name;
	}
	
	public String toString(){
		return "TradableItem:" + name + ":" + value;
	}
	
	public static TradableItem generateItem(){
		return new TradableItem(Math.random()*1000, 
				names[(int) (Math.random()*names.length)]);
	}
	
	public static List<TradableItem> generateItems(int amount){
		List<TradableItem> result = new ArrayList<TradableItem>(amount);
		for(int i = 0; i < amount; i++){
			result.add(generateItem());
		}
		return result;
	}
	
	public static Set<TradableItem> generateUnique(int amount){
		if(amount > names.length){
			throw new RuntimeException("Can't generate that many unique items, " +
					"requested " + amount + " of max " + names.length);
		}
		
		Set<TradableItem> result = new HashSet<TradableItem>(amount);
		while(result.size() < amount){
			result.add(generateItem());
		}
		return result;
	}
	
	public static int maxUnique(){
		return names.length;
	}

}
