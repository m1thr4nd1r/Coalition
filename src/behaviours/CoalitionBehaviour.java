package behaviours;

import java.util.Random;

import agents.CoalitionAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class CoalitionBehaviour extends SimpleBehaviour {

	private boolean done;
	private double newValue;
	private String newCoalition;
	private int accepted, anwsers;
	private String target;
	private CoalitionAgent agent;
	private ACLMessage msg, reply;
	private Random random;
	private DFAgentDescription[] result;
	private DFAgentDescription dfd;
	private ServiceDescription sd;
	
	public CoalitionBehaviour(CoalitionAgent coalitionAgent) {
		this.myAgent = coalitionAgent;
		done = false;
		target = "";
		accepted = -1;
		anwsers = -1;
		this.agent = coalitionAgent;
		msg = null;
		reply = null;
		newValue = 0;
		newCoalition = "";
		random = new Random();
	}
	
	public DFAgentDescription[] searchAll()
	{
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
	    sd.setType("coalition");
	    dfd.addServices(sd);
		
		DFAgentDescription[] result = null;
		SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        try
        {	
        	result = DFService.search(agent, dfd, ALL);
            this.anwsers = result.length;
            this.accepted = result.length;
        	
            System.out.println(myAgent.getLocalName() + " found: " + result.length + "(" + result[0].getName().getLocalName() + ")");
           
        }
        catch (FIPAException fe) {
        	fe.printStackTrace(); 
    	}
        
        return result;
	}
	
	public DFAgentDescription[] search()
	{
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
	    sd.setName(target);
	    dfd.addServices(sd);
		
		DFAgentDescription[] result = null;
		SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        try
        {	
        	result = DFService.search(agent, dfd, ALL);
            this.anwsers = result.length;
            this.accepted = result.length;
        	
            System.out.println(myAgent.getLocalName() + " found: " + result.length + "(" + result[0].getName().getLocalName() + ")");
           
        }
        catch (FIPAException fe) {
        	fe.printStackTrace(); 
    	}
        
        return result;
	}
	
	@Override
	public void action() 
	{
		msg = myAgent.receive();
		
		while (msg != null && msg.getConversationId() != null)
		{
			if (msg.getConversationId().equals("Done"))
				this.done = true;
			else if (msg.getConversationId().equals("Turn"))
			{
				System.out.println(myAgent.getLocalName() + " it's my turn");
				reply = new ACLMessage(0);
				
				String temp = String.valueOf(nameIndex(this.myAgent.getLocalName()));
	            int receiver;
	            do {
	            	receiver = random.nextInt(agent.getCoalitions());
	            	target = agent.getCoalition(receiver);
	            } while (target.contains(temp));
	  		    
	            System.out.println(myAgent.getLocalName() + " target: " + target);            
	    	    
	            result = search();
	    	    
	            for (int i=0; i<result.length; i++)
	            	reply.addReceiver(result[i].getName());
				
		        reply.setConversationId("Query");
		        reply.setContent(Double.toString(agent.getCoalitionValue()));
		        myAgent.send(reply);
			}
			else if (msg.getConversationId().equals("Query"))
			{
				reply = msg.createReply();
				
				newValue = newCoalitionValue(Double.parseDouble(msg.getContent()));
				if (newValue > this.agent.getCoalitionValue())
				{
					reply.setConversationId("Accepted");
					reply.setContent(Double.toString(newValue));
				}
				else
					reply.setConversationId("Refused");
					
				this.myAgent.send(reply);
			}
			else if (msg.getConversationId().equals("Accepted") || msg.getConversationId().equals("Refused"))
			{
				anwsers--;
				System.out.println(msg.getSender().getLocalName() + " - " + msg.getConversationId() + " - me(" + myAgent.getLocalName() + ")");
				if (msg.getConversationId().equals("Accepted"))
				{
					accepted--;		
					newCoalition += nameIndex(msg.getSender().getLocalName());
					
					if (anwsers == 0)
					{												
						if (accepted == 0)
						{
							this.agent.deRegister();
							
							reply = new ACLMessage(0);
							result = searchAll();
							for (int i=0; i<result.length; i++)
				            	reply.addReceiver(result[i].getName());
							reply.addReceiver(agent.getManager().getAID());
							reply.setConversationId("Update");
							reply.setContent(agent.getCoalition() + "|" + newCoalition + "|" + msg.getContent());
							
							System.out.println(this.myAgent.getLocalName() + " Joins coalition " + target);
							this.agent.register(target);							
						}
						
//						reply = new ACLMessage(0);
//						reply.addReceiver(agent.getManager().getAID());
//						reply.setConversationId("Turn");
//						reply.setContent("");
						
						myAgent.send(reply);
					}
				}
			}
			else if (msg.getConversationId().equals("Update"))
			{
				String[] contents = msg.getContent().split("|");
				
				if (contents[1].contains(String.valueOf(agent.getNumber())) || contents[0].contains(String.valueOf(agent.getNumber())))
					agent.setCoalitionValue(Double.valueOf(contents[2]));
				
				agent.updateCoalitions(contents[0],contents[1]);
			}
			msg = myAgent.receive();
		}
	}

	public int nameIndex(String name)
	{
		return Integer.valueOf(name.substring(name.length()-1));
	}
	
	public double newCoalitionValue(double a)
	{
		return this.agent.getCoalitionValue() + a;
	}
	
	@Override
	public boolean done() {
		return done;
	}
	
	public int onEnd()
	{
		myAgent.doDelete();
		return 0;
	}
}
