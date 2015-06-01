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
	private Random random;
	private String temp;
	private ManagerAgent agent;
	private long lastMessage;
	private int turn,start, chosen,aux,agents,done;
	private boolean hasFaulty;
	private ACLMessage msg, reply;
	
	public ManagerBehaviour(ManagerAgent agent, int agents, boolean trusty)
	{
		myAgent = agent;
		this.agent = agent;
		done = agents;
		random = new Random();
		this.agents = agents;
		turn = 0;
		msg = new ACLMessage(0);
		temp = "";
		aux = 0;
		reply = null;
		start = agents;
		hasFaulty = !trusty;
		
		DFAgentDescription[] results;
		do
		{
			results = search();
		} while (results.length != agents);
		
		if (agent.isDebugBuild())
			System.out.println("Manager Found: " + results.length);
		
		for (int i = 0; i < results.length; i++)
		{
			msg.clearAllReceiver();
			msg.addReceiver(results[i].getName());
			msg.setConversationId("Start");
			aux = Integer.valueOf(agent.getNumber(results[i].getName().getLocalName()));
			msg.setContent(String.valueOf(agent.getFaulty(aux-1)));
			myAgent.send(msg);
		}
		
		lastMessage = System.currentTimeMillis();
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
        	result = DFService.search(myAgent, dfd, ALL);
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
        	result = DFService.search(myAgent, dfd, ALL);
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
		
		while (/*(start == agent.getFaultyAmount() && (System.currentTimeMillis() - lastMessage) > agent.getTimeOut()) || */ (msg != null && msg.getConversationId() != null))
		{
			System.out.println("Time: " + String.valueOf(System.currentTimeMillis() - lastMessage));
			lastMessage = System.currentTimeMillis();
			
			if (msg.getConversationId().equals("Turn"))
			{
				if (agent.isDebugBuild() && start != 0)
					System.out.println("Agent " + agent.getNumber(msg.getSender().getLocalName()) + " turn ended.");
				
				reply = new ACLMessage(0);
				DFAgentDescription[] results = search();	
//				if (agent.isDebugBuild())
//				{
//					System.out.println("Manager found: " + String.valueOf(results.length) + " agents:");
//					for (int i = 0; i < results.length; i++)
//						System.out.println(results[i].getName().getLocalName());
//				}
				if (results.length == 0 || (hasFaulty && results.length == agent.getFaultyAmount()))
				{
					for (int i = 0; i < results.length; i++)
						reply.addReceiver(results[i].getName());
					
					results = getCoalition();
					temp = "";
					for (int i = 0; i < results.length; i++)
					{
						temp += agent.getNumber(results[i].getName().getLocalName());
						reply.addReceiver(results[i].getName());
					}
					agent.setCoalition(temp);
					reply.setConversationId("Done");
					//done = true;
					//agent.setCoalitionValue(Double.valueOf(msg.getContent()));
					if (agent.isDebugBuild())
						System.out.println("Coalition fully formed.");
				}
				else
				{
					do
					{
						aux = random.nextInt(results.length);
						turn = Integer.valueOf(agent.getNumber(results[aux].getName().getLocalName()));
					} while (turn == chosen || agent.getFaulty(turn-1) == 1);
					
					reply.addReceiver(results[aux].getName());
					reply.setConversationId("Turn");
					if (agent.isDebugBuild())
					{
						System.out.println(aux + " it's your turn(" + results[aux].getName().getLocalName() +")[" + String.valueOf(agent.getFaulty(turn-1)) + "]");
						for (int i = 0; i < agents; i++)
							System.out.print(agent.getFaulty(i));
						System.out.println();
					}
				}
								
				myAgent.send(reply);
			}
			else if (msg.getConversationId().equals("Start"))
			{
				if (agent.isDebugBuild())
					System.out.println(msg.getSender().getLocalName() + " started.");
				start--;
				
				if (start == agent.getFaultyAmount())
				{
					start = 0;
					reply = new ACLMessage(0);
					DFAgentDescription[] results = search();
					
					do
					{
						aux = random.nextInt(results.length);
						chosen = Integer.valueOf(agent.getNumber(results[aux].getName().getLocalName()));
					}while (agent.getFaulty(chosen-1) == 1);
					
					reply.addReceiver(results[aux].getName());
					reply.setConversationId("Coalition");
					myAgent.send(reply);
					if (agent.isDebugBuild())
						System.out.println(results[aux].getName().getLocalName() + " begins in the coalition.");	
					
					msg.clearAllReceiver();
					msg.addReceiver(myAgent.getAID());
					msg.setConversationId("Turn");
					myAgent.send(msg);
				}
			}
			else if (msg.getConversationId().equals("Done"))
			{
				done--;
				int i = msg.getContent().indexOf('|');
				agent.setB(Integer.valueOf(agent.getNumber(msg.getSender().getLocalName()))-1, Double.valueOf(msg.getContent().substring(0, i)));
				agent.setCoalitionValue(Double.valueOf(msg.getContent().substring(i+1)));
			}
						
			msg = myAgent.receive();
		}
	}

	@Override
	public boolean done() {
		return done==0 || (hasFaulty && done == agent.getFaultyAmount());
	}
	
	public int onEnd()
	{
		if (agent.isDebugBuild())
			System.out.println("Run #" + String.valueOf(agent.getExecutionAmount()) + " ended.");
		
		agent.writeToFile("");
		agent.decExecutionAmount();
		if (agent.getExecutionAmount() > 0)
			agent.start(false);
		else
			myAgent.doDelete();
		return 0;
	}
}
