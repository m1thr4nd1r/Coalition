package behaviours;

import java.util.Random;

import agents.CoalitionAgent;
import agents.StartAgent;
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
	boolean done,start;
	StartAgent agent;
	int turn;
	private ACLMessage msg, reply;
	
	public ManagerBehaviour(StartAgent agent, int agents)
	{
		this.myAgent = agent;
		this.agent = agent;
		done = false;
		random = new Random();
		this.agents = agents;
		turn = 0;
		msg = null;
		reply = null;
		start = true;
	}
	
	public DFAgentDescription[] search()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
	    sd.setName(String.valueOf(turn));
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
		
		while (start || (msg != null && msg.getConversationId() != null))
		{
			if (start || (msg.getConversationId().equals("Turn") && !msg.getContent().isEmpty()))
			{
				start = false;
				reply = new ACLMessage(0);
				turn = random.nextInt(agents)+1;
				System.out.print(turn + " it's your turn");
				DFAgentDescription[] results = search();				
				reply.addReceiver(results[0].getName());
				reply.setConversationId("Turn");
				this.myAgent.send(reply);
				System.out.println("(" + results[0].getName().getLocalName() +")");
			}
			else if (msg.getConversationId().equals("Update"))
			{
				String[] contents = msg.getContent().split("|");
				agent.updateCoalitions(contents[0],contents[1]);
			}
			
			msg = myAgent.receive();
		}
	}

	@Override
	public boolean done() {
		return done;
	}
}
