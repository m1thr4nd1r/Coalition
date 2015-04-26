package behaviours;

import java.util.Random;

import agents.ManagerAgent;
import jade.core.behaviours.SimpleBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.SearchConstraints;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.lang.acl.ACLMessage;

@SuppressWarnings("serial")
public class ManagerBehaviour extends SimpleBehaviour
{
	Random random;
	int agents;
	boolean done;
	ManagerAgent agent;
	int turn,start;
	private ACLMessage msg, reply;
	
	public ManagerBehaviour(ManagerAgent agent, int agents)
	{
		this.myAgent = agent;
		this.agent = agent;
		done = false;
		random = new Random();
		this.agents = agents;
		turn = 0;
		msg = null;
		reply = null;
		start = agents;
	}
	
	public DFAgentDescription[] getCoalition()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
	    sd.setType("coalition");
	    dfd.addServices(sd);
		
		DFAgentDescription[] result = null;
		SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        
        try {	
        	result = DFService.search(this.myAgent, dfd, ALL);
        }
        catch (FIPAException fe) {
        	fe.printStackTrace(); 
    	}
        
        return result;
	}
	
	public DFAgentDescription[] search()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
	    sd.setType("agents");
	    dfd.addServices(sd);
		
		DFAgentDescription[] result = null;
		SearchConstraints ALL = new SearchConstraints();
        ALL.setMaxResults(new Long(-1));
        
        try {	
        	result = DFService.search(this.myAgent, dfd, ALL);
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
		
		while (start == 0 || (msg != null && msg.getConversationId() != null))
		{
			if (start == 0 || (msg.getConversationId().equals("Turn") && !msg.getContent().isEmpty()))
			{
				start = 1;
				reply = new ACLMessage(0);
				DFAgentDescription[] results = search();				
				if (results.length == 0)
				{
					results = getCoalition();
					for (int i = 0; i < results.length; i++)
						reply.addReceiver(results[i].getName());
					reply.setConversationId("Done");
					done = true;
					if (agent.isDebugBuild())
						System.out.println("Coalition fully formed.");
				}
				else
				{
					turn = random.nextInt(results.length);
					reply.addReceiver(results[turn].getName());
					reply.setConversationId("Turn");
					if (agent.isDebugBuild())
						System.out.println(turn + " it's your turn(" + results[turn].getName().getLocalName() +")");					
				}
								
				this.myAgent.send(reply);
			}
			else if (msg.getConversationId().equals("Start"))
			{
				if (agent.isDebugBuild())
					System.out.println(msg.getSender().getLocalName() + " started.");
				start--;
				if (start == 0)
				{
					reply = new ACLMessage(0);
					DFAgentDescription[] results = search();
					Random r = new Random();			        
			        int chosen = r.nextInt(results.length);
					reply.addReceiver(results[chosen].getName());
					reply.setConversationId("Coalition");
					myAgent.send(reply);
					if (agent.isDebugBuild())
						System.out.println(results[chosen].getName().getLocalName() + " begins in the coalition.");
				}
			}	
			
			msg = myAgent.receive();
		}
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
