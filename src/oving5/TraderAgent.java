package oving5;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 9143718912851258654L;
	private static final String REQUEST_INVENTORY = "REQUEST_INVENTORY"; 
	private static final String ACCEPT_NEGOTIATION = "ACCEPT_NEGOTIATION";
	private static final String REFUSE_NEGOTIATION = "REFUSE_NEGOTIATION";
	private static final String IN_NEGOTIATION = "ALREADY_IN_NEGOTIATION";
	private static final String HAVE_ITEM = "HAVE_ITEM";
	private static final String TRADER = "TRADER";

	//Change to disable output
	private static final boolean DEBUG = true;

	private final Set<TradableItem> wantedItems;
	private final List<TradableItem> inventory;
	private final List<TradableItem> obtained = new ArrayList<TradableItem>();
	private final Map<AID, Set<TradableItem>> otherInventories = new HashMap<AID, Set<TradableItem>>();
	private double money;
	private AID negotiationPartner = null;
	private TradeDeal currentDeal = null;
    private int dealCount = 0;

	/**
	 * Create a new TraderAgent
	 * @param wants - Items the agent want to obtain
	 * @param has - Items the trader has
	 * @param money - The amount of money
	 */
	public TraderAgent(Set<TradableItem> wants, List<TradableItem> has,
			double money){
		super();
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
	 * Create a trader with random items to obtain and random items to sell, but
	 * with a specific amount of money
	 * @param money
	 */
	public TraderAgent(double money){
		this((int)(Math.random()*TradableItem.maxUnique()), 
				(int)(Math.random()*100), money);
	}

	/**
	 * Create an agent with all attributes random
	 */
	public TraderAgent(){
		this((int)(Math.random()*TradableItem.maxUnique()), 
				(int)(Math.random()*(TradableItem.maxUnique()/2)+1), Math.random()*10000);
	}

	private void handleRequest(ACLMessage m){
		String[] splitted = m.getContent().trim().split("=");
		if(splitted[0].equals(REQUEST_INVENTORY)){
			sendReply(m, ACLMessage.INFORM, REQUEST_INVENTORY + "=" +
					this.inventory.toString());
		}else if(splitted[0].equals(HAVE_ITEM)){
			TradableItem item = TradableItem.parseTradeItem(splitted[1]);
			sendReply(m, ACLMessage.INFORM, HAVE_ITEM + "=" + item.toString() +
					"=" + this.inventory.contains(item));
		}
	}

	private void handleInform(ACLMessage m){
		String[] splitted = m.getContent().trim().split("=");
		if(splitted[0].equals(REQUEST_INVENTORY)){
			Set<TradableItem> otherInventory = new HashSet<TradableItem>();
			String[] items = splitted[1].replaceAll("[\\[\\]]", "").split(",");
			for(String item : items){
				otherInventory.add(TradableItem.parseTradeItem(item));
			}
			this.otherInventories.put(m.getSender(), otherInventory);
		}else if(splitted[0].equals(HAVE_ITEM)){
			TradableItem item = TradableItem.parseTradeItem(splitted[1]);
			boolean agentHas = Boolean.parseBoolean(splitted[2]);
			if(!this.otherInventories.containsKey(m.getSender())){
				this.otherInventories.put(m.getSender(), new HashSet<TradableItem>());
			}
			if(agentHas){
				this.otherInventories.get(m.getSender()).add(item);
			}else{
				this.otherInventories.get(m.getSender()).remove(item);
			}
		}
	}

	/**
	 * Create a new deal which this agent want to bargin for. This method will
	 * remove items from the wanted list if no trader can be found after 4 retries.
	 * @return - A new TradeDeal if an item and a trader could be found, will
	 * return null if no more wanted items could be found
	 */
	private TradeDeal proposeDeal(){
		TradableItem max = this.maxWanted();
		if(max == null){
			return null;
		}
		AID trader = this.findTrader(max);
		if(trader == null){
			return null;
		}
		//Trader is not null here
		double val = estimatedWantedValue(max);
		return new TradeDeal(max, val, trader.getLocalName(), this.getLocalName());
	}
	
	private void negotiate(){
		TradeDeal deal = this.proposeDeal();
		if(deal == null && this.wantedItems.isEmpty()){
			this.logOutput("Tried to create a new deal, but it was null, assuming " +
					"we are done, wanted: " + this.wantedItems.toString() + "," +
							" obtained: " + this.obtained.toString() + ", " +
									"money: " + this.money, true);
			return;
		}else{
            //TODO: Finish this method so that it will act on the proposed deal
            // find proposer
            // send deal to proposer.
            // see what happens.
        }


	}

	/**
	 * Estimate the value of the wanted item
	 * @param item - The item to estimate the value of
	 * @return The estimated value
	 */
	private double estimatedWantedValue(TradableItem item){
		//TODO: Implement an actual heuristic to estimate the value
		/*
		 * This heuristic should take in to account the amount of money left,
		 * the number of items left, the number of sellable items left,
		 * possibly other things?
		 */
		return item.getValue();
	}

    private double estimatedSalesValue(TradableItem item){
        // todo create heuristic.
        return item.getValue();
    }

	/**
	 * Get the item in the wanted set with the maximum value
	 * @return the most valuable item
	 */
	private TradableItem maxWanted(){
		TradableItem result = null;
		double maxVal = Double.MIN_VALUE;
		for(TradableItem t : this.wantedItems){
			if(t.getValue() > maxVal){
				result = t;
				maxVal = t.getValue();
			}
		}
		return result;
	}

	/**
	 * Find a trader which has the item
	 * @param item - The item a trader must have
	 * @return The AID of the trader with the item, if null, no trader has the item
	 * or this agent does not know of any other agents inventories
	 */
	private AID findTrader(TradableItem item){
		if(otherInventories.isEmpty()){
			ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
			msg.setContent(REQUEST_INVENTORY);
			this.multicastMessage(msg);
			return null;
		}else{
			for(AID a : otherInventories.keySet()){
				if(otherInventories.get(a).contains(item)){
					return a;
				}
			}
		}
		return null;
	}

	/**
	 * Multicast a message to all the other trader agents
	 * @param msg - The message to broadcast, this should have been fully created, with
	 * performative and content, this method will only add recipients
	 * @return True if message sent to at least one trader, false otherwise
	 */
	private boolean multicastMessage(ACLMessage msg){
		DFAgentDescription desc = new DFAgentDescription();
		ServiceDescription s = new ServiceDescription();
		s.setType(TRADER);
		desc.addServices(s);
		DFAgentDescription[] agents = null;
		try {
			agents = DFService.search(this, desc);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		if(agents != null){
			for(DFAgentDescription dfa : agents){
				if(!dfa.getName().equals(this.getName()))
					msg.addReceiver(dfa.getName());
			}
			this.send(msg);
			return true;
		}
		return false;
	}

	/**
	 * Create and send a reply to a message, this is a convenience function
	 * which should be used instead of doing it your self to insure that
	 * the message is created properly and actually sent
	 * @param msg - The message to create a reply to
	 * @param performative - The performative of the reply
	 * @param message - The content of the reply
	 */
	private void sendReply(ACLMessage msg, int performative, String message){
		ACLMessage reply = msg.createReply();
		reply.setPerformative(performative);
		reply.setContent(message);
		this.send(reply);
	}

	/**
	 * Log a message in some form, currently just stdout
	 * @param msg
	 * @param error whether or not this message is an error or not
	 */
	private void logOutput(String msg, boolean error){
		if(!error){
			if(DEBUG)
				System.out.println(this.getLocalName() + ": " + msg);
		}else{
			System.err.println(this.getLocalName() + ": " + msg);
		}
	}

	@Override
	protected void setup() {
		super.setup();
		DFAgentDescription desc = new DFAgentDescription();
		desc.setName(this.getAID());
		ServiceDescription d = new ServiceDescription();
		d.setName(this.getLocalName());
		d.setType(TRADER);
		desc.addServices(d);
		try {
			DFService.register(this, desc);
		} catch (FIPAException e) {
			e.printStackTrace();
		}
		this.addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = receive();
				if(msg != null){
					logOutput("Got " + ACLMessage.getPerformative(msg.getPerformative()) + 
							" message with content '" + msg.getContent() + "' from " +
							msg.getSender().getLocalName(), false);
					switch (msg.getPerformative()) {
					case ACLMessage.REQUEST:
						handleRequest(msg);
						break;
					case ACLMessage.INFORM:
						handleInform(msg);
						break;
					case ACLMessage.PROPOSE:
						handlePropose(msg);
						break;
					case ACLMessage.CONFIRM:
						handleConfirm(msg);
						break;
					case ACLMessage.REFUSE:
						handleRefuse(msg);
						break;
					case ACLMessage.ACCEPT_PROPOSAL:
						handleAcceptProposal(msg);
						break;
					case ACLMessage.REJECT_PROPOSAL:
						handleRejectProposal(msg);
						break;
					case ACLMessage.AGREE:
						handleAgree(msg);
						break;
					default:
						logOutput("Got a message with an un handled performative",
								true);
						break;
					}
				}
			}
		});
		//Request the inventory of all the other traders
		ACLMessage msg = new ACLMessage(ACLMessage.REQUEST);
		msg.setContent(REQUEST_INVENTORY);
		this.multicastMessage(msg);
        this.negotiate();
	}

	/**
	 * Method to check if the sender of the message is the current trading
	 * partner
	 * @param msg - The message to check
	 * @return true if sender is ok to negotiate with
	 */
	private boolean checkSender(ACLMessage msg){
		if(msg.getSender().equals(this.negotiationPartner)){
			return true;
		}else{
			logOutput("Got a message from a sender who is not the current " +
					"trading partner", true);
			return false;
		}
	}

	protected void handleAgree(ACLMessage msg) {
		if(!checkSender(msg))
			return;
		TradeDeal d = TradeDeal.parseDeal(msg.getContent());
		this.logOutput(d.pPrint() + ". Deal just struck!", false);
		if(d.getBuyer().equals(this.getLocalName())){
			this.obtained.add(d.getItem());
			this.money -= d.getTradeMoney();
			this.inventory.removeAll(d.getTradeItems());
		}else{
			this.inventory.remove(d.getItem());
			this.money += d.getTradeMoney();
			this.inventory.addAll(d.getTradeItems());
		}
		this.currentDeal = null;
		this.negotiationPartner = null;
	}

	protected void handleRejectProposal(ACLMessage msg) {
		if(!checkSender(msg))
			return;
		this.logOutput("Negotiation unsuccessful, agent " + msg.getSender() + " did not accept", false);
		this.negotiationPartner = null;
		this.handleRefuse(msg); //This should update the state of the agent
		//to one where it will continue on
	}

	protected void handleAcceptProposal(ACLMessage msg) {
		if(!checkSender(msg))
			return;
		this.logOutput("Negotiation successful, agent " + msg.getSender() + " accepted", false);
		this.sendReply(msg, ACLMessage.AGREE, this.currentDeal.toString());
		this.handleAgree(msg);
	}

	protected void handleRefuse(ACLMessage msg) {
		this.logOutput("Agent " + msg.getSender() + " refused the negotiation, moving on", false);
		try {
			Thread.sleep((long) (1000*Math.random()));
			this.negotiate();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	protected void handleConfirm(ACLMessage msg) {
		if(this.negotiationPartner != null){
			logOutput("Got a confirm message when already in a negotiation, sending refusal", true);
			this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, IN_NEGOTIATION);
		}else{
			logOutput("Entering a new negotiation with " +
					msg.getSender().getLocalName(), false);
            this.dealCount = 5;
			this.negotiationPartner = msg.getSender();
			this.sendReply(msg, ACLMessage.PROPOSE, this.currentDeal.toString());
		}
	}

	protected void handlePropose(ACLMessage msg) {
		if(this.negotiationPartner == null){
			logOutput("No trading partner at this time, entering deal", false);
			this.negotiationPartner = msg.getSender();
			this.sendReply(msg, ACLMessage.CONFIRM, ACCEPT_NEGOTIATION);
            this.dealCount = 5;
		}else{
			if(!msg.getSender().equals(this.negotiationPartner)){
				logOutput("Already got a trading partner", false);
				this.sendReply(msg, ACLMessage.REFUSE, REFUSE_NEGOTIATION);
			}else{
                TradeDeal t = TradeDeal.parseDeal(msg.getContent());
                if( t.getBuyer().equals(this.getLocalName()) ){

                    // do not want deal.
                    if( getDealValue(t) >= this.estimatedWantedValue(t.getItem()) ){
                        // reject and send new deal.
                        this.dealCount--;
                        // exceeded maximum deal proposals.
                        if(this.dealCount <= 0){
                            this.negotiationPartner = null;
                            this.currentDeal = null;
                            this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, "");
                        }else{
                            // bad deal for us counter offer.
                            TradeDeal newDeal = counterDeal(t);
                            if( newDeal != null ){
                                this.currentDeal=newDeal;
                                this.sendReply(msg, ACLMessage.PROPOSE, currentDeal.toString());
                            }else{
                                // could not get new deal, rejecting negotiation.
                                this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, "");
                                this.negotiationPartner = null;
                                this.currentDeal = null;
                            }
                        }
                    }else {
                        // deal is cheaper then item value, want it
                        this.sendReply(msg, ACLMessage.ACCEPT_PROPOSAL, "");
                    }
                } else{
                    // is seller
                    // deal is bad, do not want.
                    if( getDealValue(t) < this.estimatedWantedValue(t.getItem()) ){
                        this.dealCount--;
                        // exceeded maximum deal proposals.
                        if(this.dealCount <= 0){
                            this.negotiationPartner = null;
                            this.currentDeal = null;
                            this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, "");
                        }else{
                            // bad deal for us counter offer.
                            TradeDeal newDeal = counterDeal(t);
                            if( newDeal != null ){
                                this.currentDeal=newDeal;
                                this.sendReply(msg, ACLMessage.PROPOSE, currentDeal.toString());
                            }else{
                                // could not get new deal, rejecting negotiation.
                                this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, "");
                                this.negotiationPartner = null;
                                this.currentDeal = null;
                            }
                        }
                    }   else{
                        // deal is good accept.
                        this.sendReply(msg, ACLMessage.ACCEPT_PROPOSAL, "");
                    }

                }

				throw new NotImplementedException();
			}
		}
	}

    protected TradeDeal counterDeal(TradeDeal deal){
        // todo
        // we buy
        if( deal.getBuyer().equals(this.getLocalName()) ){
            // sum wanted items.
            // sum > money left.
                // propose items.

            if(!deal.getTradeItems().isEmpty()){
                // set the min valued item to the first one.
                TradableItem min = deal.getTradeItems().get(0);
                double dealItemsValue = 0;
                for(TradableItem item: deal.getTradeItems()){
                    dealItemsValue += item.getValue();

                    if(min.getValue() > item.getValue()){
                        min = item;
                    }
                }

                double inventoryItemsValue = getValueOfItems(this.inventory);

                double maxItems = dealItemsValue / inventoryItemsValue;

                if(maxItems >= 0.2){
                    // do not like
                    // remove cheapest item to improve deal in our favor.
                    deal.getTradeItems().remove(min);
                }
            }

            double newMoney = this.estimatedWantedValue(deal.getItem());
            if (getDealValue(deal) >= newMoney){
                // money = estimert price  - deal items value
                return new TradeDeal(deal.getItem(), newMoney-getValueOfItems(deal.getTradeItems()), deal.getTradeItems(), deal.getTrader(), deal.getBuyer());
            }

        }else{
            // we are seller.
            return new TradeDeal(deal.getItem(), estimatedSalesValue(deal.getItem()), deal.getTradeItems(), deal.getTrader(), deal.getBuyer());
        }

        return null;
    }

    protected double getValueOfItems(Iterable<TradableItem> tradableItemList){
        double sum = 0.0;
        for(TradableItem item: tradableItemList){
            sum += item.getValue();
        }
        return sum;
    }

    protected double getDealValue(TradeDeal tradeDeal){
        // money + tot item value

        double sum = tradeDeal.getTradeMoney();
        for(TradableItem item: tradeDeal.getTradeItems()){
            if (tradeDeal.getBuyer().equals(this.getLocalName())){
            // my items
                sum += myItemValue(item);
            } else {
                sum += hisItemValue(item);
            }
        }

        return sum;
    }

    protected double myItemValue(TradableItem tradableItem){
        // todo create better heuristic.
        return ( tradableItem.getValue() );
    }
    protected double hisItemValue(TradableItem tradableItem){
        // if we want the item return full value, else we don't care.
        if (this.wantedItems.contains(tradableItem)){
            return tradableItem.getValue();
        }
        return 0.0;
    }

}
