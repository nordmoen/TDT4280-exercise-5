package oving5;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.lang.acl.ACLMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

public class TraderAgent extends Agent {
	private static final long serialVersionUID = 9143718912851258654L;
	private static final String REQUEST_INVENTORY = "REQUEST_INVENTORY"; 
	private static final String ACCEPT_NEGOTIATION = "ACCEPT_NEGOTIATION";
	private static final String REFUSE_NEGOTIATION = "REFUSE_NEGOTIATION";
	private static final String REJECT_PROPOSAL = "REJECT_PROPOSAL";
	private static final String IN_NEGOTIATION = "ALREADY_IN_NEGOTIATION";
	private static final String HAVE_ITEM = "HAVE_ITEM";

	//Change to disable output
	private static final boolean DEBUG = true;

	private final Set<TradableItem> wantedItems;
	private final List<TradableItem> inventory;
	private final List<TradableItem> obtained = new ArrayList<TradableItem>();
	private double money;
	private AID negotiationPartner = null;
	private TradeDeal currentDeal = null;

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
				(int)(Math.random()*100), Math.random()*1000);
	}

	private void handleRequest(ACLMessage m){
		String[] splitted = m.getContent().trim().split("=");
		if(splitted[0].equals(REQUEST_INVENTORY)){
			sendReply(m, ACLMessage.INFORM, this.inventory.toString());
		}else if(splitted[0].equals(HAVE_ITEM)){
			TradableItem item = TradableItem.parseTradeItem(splitted[1]);
			sendReply(m, ACLMessage.INFORM, HAVE_ITEM + "=" +
					this.inventory.contains(item));
		}
	}

	private void handleInform(ACLMessage m){

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
		if(DEBUG){
			if(!error){
				System.out.println(this.getLocalName() + ": " + msg);
			}else{
				System.err.println(this.getLocalName() + ": " + msg);
			}
		}
	}

	@Override
	protected void setup() {
		super.setup();
		this.addBehaviour(new CyclicBehaviour() {
			private static final long serialVersionUID = 1L;

			@Override
			public void action() {
				ACLMessage msg = receive();
				if(msg != null){
					logOutput("Got " + ACLMessage.getPerformative(msg.getPerformative()) + 
							" message with content '" + msg.getContent() + "'", false);
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
		this.logOutput(d.pPrint(), false);
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
		throw new NotImplementedException();
		// TODO Implement the timeout and return to another negotiation
	}

	protected void handleConfirm(ACLMessage msg) {
		if(this.negotiationPartner != null){
			logOutput("Got a confirm message when already in a negotiation, sending refusal", true);
			this.sendReply(msg, ACLMessage.REJECT_PROPOSAL, IN_NEGOTIATION);
		}else{
			logOutput("Entering a new negotiation with " +
					msg.getSender().getLocalName(), false);
			this.negotiationPartner = msg.getSender();
			this.sendReply(msg, ACLMessage.PROPOSE, this.currentDeal.toString());
		}
	}

	protected void handlePropose(ACLMessage msg) {
		if(this.negotiationPartner == null){
			logOutput("No trading partner at this time, entering deal", false);
			this.negotiationPartner = msg.getSender();
			this.sendReply(msg, ACLMessage.CONFIRM, ACCEPT_NEGOTIATION);
		}else{
			if(!msg.getSender().equals(this.negotiationPartner)){
				logOutput("Already got a trading partner", false);
				this.sendReply(msg, ACLMessage.REFUSE, REFUSE_NEGOTIATION);
			}else{
				//TODO: Consider proposal and decide what to do about it
				throw new NotImplementedException();
			}
		}

	}
}
