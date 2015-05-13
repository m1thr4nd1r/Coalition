package behaviours;

import agents.CoalitionAgent;
import jade.core.AID;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class CoalitionBehaviour extends SimpleBehaviour {

	private boolean done,start;
	private double newValue;
	private int accepted, anwsers;
	private CoalitionAgent agent;
	private ACLMessage msg, reply;
	private AID manager;
	private DFAgentDescription[] result;
	private DFAgentDescription dfd;
	private ServiceDescription sd;
	
	public CoalitionBehaviour(CoalitionAgent coalitionAgent) {
		myAgent = coalitionAgent;
		done = false;
		accepted = -1;
		anwsers = -1;
		agent = coalitionAgent;
		msg = null;
		reply = null;
		newValue = 0;
		manager = searchManager();
		start = true;
	}
	
	public AID searchManager()
	{
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
	    sd.setType("manager");
	    dfd.addServices(sd);
		
		DFAgentDescription[] result = null;
		SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        try
        {	
        	result = DFService.search(agent, dfd, ALL);
        }
        catch (FIPAException fe) {
        	fe.printStackTrace(); 
    	}
        
        return result[0].getName();
	}
	
	public DFAgentDescription[] search()
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
            anwsers = result.length;
            accepted = result.length;
        	
            if (agent.isDebugBuild())
            	System.out.println(myAgent.getLocalName() + " found: " + result.length);
        }
        catch (FIPAException fe) {
        	fe.printStackTrace(); 
    	}
        
        if (agent.isDebugBuild())
        	for (int i = 0; i < result.length; i++)
        		System.out.println("(" + result[i].getName().getLocalName() + ")");
        
        return result;
	}
	
	@Override
	public void action() 
	{
		if (start)
		{
			msg = new ACLMessage(0);
			msg.setConversationId("Start");
			msg.addReceiver(manager);
			myAgent.send(msg);
			start = false;
		}
		else
		{
			msg = myAgent.receive();
			
			while (msg != null && msg.getConversationId() != null)
			{
				if (msg.getConversationId().equals("Done"))
				{
					done = true;
					reply = msg.createReply();
					reply.setContent(String.valueOf(agent.getAgentValue()) + "|" + String.valueOf(agent.getCoalitionValue()));
					myAgent.send(reply);
				}
				else if (msg.getConversationId().equals("Coalition"))
				{
					agent.deregister();
					agent.register("coalition");
				}
				else if (msg.getConversationId().equals("Turn"))
				{
					if (agent.isDebugBuild())
						System.out.println(myAgent.getLocalName() + " it's my turn");
					reply = new ACLMessage(0);
					
					do
					{
						result = search();
					}while (result.length == 0);
		    	    
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
					if (newValue > agent.getCoalitionValue())
					{
						reply.setConversationId("Accepted");
						reply.setContent(Double.toString(newValue));
					}
					else
						reply.setConversationId("Refused");
						
					myAgent.send(reply);
				}
				else if (msg.getConversationId().equals("Update"))
				{
					agent.setCoalitionValue(Double.valueOf(msg.getContent()));
					agent.setAgentValue(agent.getAgentValue() + Double.valueOf(msg.getContent()) / search().length);
					if (agent.isDebugBuild())
						System.out.println("New Value: " + String.valueOf(agent.getAgentValue()) + " | " + String.valueOf(agent.getCoalitionValue()) + " | " + String.valueOf(agent.getNumber()));
				}
				else if (msg.getConversationId().equals("Accepted") || msg.getConversationId().equals("Refused"))
				{
					anwsers--;
					if (agent.isDebugBuild())
						System.out.println(msg.getSender().getLocalName() + " - " + msg.getConversationId() + " - me(" + myAgent.getLocalName() + ")");
					
					if (msg.getConversationId().equals("Accepted"))
					{
						accepted--;		
						if (anwsers == 0)
						{												
							if (accepted == 0)
							{
								result = search();
								for (int i = 0; i < result.length; i++)
									msg.addReceiver(result[i].getName());
								msg.setConversationId("Update");
								
								agent.deregister();
								if (agent.isDebugBuild())
									System.out.println(myAgent.getLocalName() + " joins the coalition!");
								agent.register("coalition");
								
								myAgent.send(msg);								
							}
							
							reply = new ACLMessage(0);
							reply.addReceiver(manager);
							reply.setConversationId("Turn");
							reply.setContent("Turn");
							
							myAgent.send(reply);
						}
					}
				}
				msg = myAgent.receive();
			}
		}
	}
	
	public double newCoalitionValue(double a)
	{
		return agent.getCoalitionValue() + a;
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
