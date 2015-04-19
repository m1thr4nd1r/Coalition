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

	private boolean done, look;
	private int accepted, anwsers;
	private String target;
	private CoalitionAgent agent;
	private ACLMessage msg, reply;
	private Random random;
	private DFAgentDescription dfd;
	private ServiceDescription sd;
	
	public CoalitionBehaviour(CoalitionAgent coalitionAgent) {
		this.myAgent = coalitionAgent;
		done = false;
		look = true;
		target = "";
		accepted = -1;
		anwsers = -1;
		this.agent = coalitionAgent;
		msg = null;
		reply = null;
		random = new Random();
	}

	@Override
	public void action() 
	{
		if (look)
		{
			reply = new ACLMessage(0);
			look = false;
			
			String temp = this.agent.getLocalName().substring(this.agent.getLocalName().length() - 1);
            int receiver;
            do {
            	receiver = random.nextInt(agent.getCoalitions());
            	target = agent.getCoalition(receiver);
            } while (target.contains(temp));
  		            
            dfd = new DFAgentDescription();
    	    sd = new ServiceDescription();
    	    sd.setName(target);
    	    dfd.addServices(sd);
            
    	    System.out.println(myAgent.getLocalName() + " target: " + target);            
    	    
			SearchConstraints ALL = new SearchConstraints();
	        ALL.setMaxResults(new Long(-1));
	        try
	        {	
	        	DFAgentDescription[] result = DFService.search(agent, dfd, ALL);
	            anwsers = result.length;
	            accepted = result.length;
	        	
	            System.out.println(myAgent.getLocalName() + " found: " + result.length);
	            
	            while (result.length != agent.getAgents())
	            	result = DFService.search(agent, agent.getDfd(), ALL);
	            	
	            for (int i=0; i<result.length; i++)
	            	reply.addReceiver(result[i].getName());
	        }
	        catch (FIPAException fe) {
	        	fe.printStackTrace(); 
        	}
			
	        reply.setConversationId("Query");
	        reply.setContent(Double.toString(agent.getCoalitionValue()));
	        myAgent.send(reply);
		}
		else
		{		
			msg = myAgent.receive();
		
			while (msg != null && msg.getConversationId() != null)
			{
				if (!msg.getConversationId().equals("Done"))
				{	
					if (msg.getConversationId().equals("Accepted") || msg.getConversationId().equals("Refused"))
					{
						anwsers--;
						System.out.println(myAgent.getLocalName() + " - " + msg.getConversationId() + " - " + msg.getSender().getLocalName());
						if (msg.getConversationId().equals("Accepted"))
						{
							accepted--;					
							if (anwsers == 0)
							{
								if (accepted == 0)
									System.out.println(this.myAgent.getName() + " Joins coalition " + target);
								else 
									look = true;
							}
						}
					}
					else
					{
						reply = msg.createReply();
						if (msg.getConversationId().equals("Query"))
						{
							look = false;
							double newValue = newCoalitionValue(Double.parseDouble(msg.getContent()));
							if (newValue > this.agent.getCoalitionValue())
								reply.setConversationId("Accepted");
							else
								reply.setConversationId("Refused");
						}
						
						this.myAgent.send(reply);
					}	
				}
				else
					this.done = true;
				
				msg = myAgent.receive();
			}
		}
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
