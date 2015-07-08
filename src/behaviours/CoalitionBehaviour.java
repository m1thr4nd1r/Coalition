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

	private boolean done;
	private double newValue;
	private int accepted, anwsers;
	private String lastConversation;
	private CoalitionAgent agent;
	private ACLMessage msg, reply;
	private AID manager;
	private DFAgentDescription[] result;
	private DFAgentDescription dfd;
	private ServiceDescription sd;
	private long lastMessage;
	
	public CoalitionBehaviour(CoalitionAgent coalitionAgent) {
		myAgent = coalitionAgent;
		done = false;
		lastConversation = "";
		lastMessage = -1;
		accepted = -1;
		anwsers = -1;
		agent = coalitionAgent;
		msg = null;
		reply = null;
		newValue = 0;
		manager = searchManager();
	}
	
	private AID searchManager()
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
	
	
	@Override
	public void action() 
	{
		msg = myAgent.receive();
		
		while ((lastConversation != "" && (System.currentTimeMillis() - lastMessage) > agent.getTimeout()) ||
			   (msg != null && msg.getConversationId() != null))
		{
			if (agent.getFaulty() != 1)
			{
				if (msg != null)
				{
					lastConversation = msg.getConversationId();
					
//					No momento, agentes não falham durante essas funções, logo msg != null
//					Porem, agentes que ja falharam, ignoram essas mensagens
					if (lastConversation.equals("Start"))
						Start();
					else if (lastConversation.equals("Coalition"))
						Coalition();
					else if (lastConversation.equals("Query"))
						Query();
					else if (lastConversation.equals("Update"))
						Update();
				}
				else if (agent.isDebugBuild())
					System.out.println("Coalition Agent Time: " + String.valueOf(System.currentTimeMillis() - lastMessage) + " " + agent.getLocalName());
				
				if (lastConversation.equals("Turn"))
					Turn();
				else if (lastConversation.equals("Response"))
					Response();
			}
			
			if (msg != null && msg.getConversationId().equals("Done"))
//			Mesmo com falha, agentes aceitam a mensagem done, logo msg != null.
//			No entanto lastConversation não é atualizado para agentes com falha	
				End();
			
			msg = myAgent.receive();
			lastMessage = System.currentTimeMillis();			
		}
	}
	
	private void Start()
	{
		agent.setFaulty(Integer.valueOf(msg.getContent()));
		if (agent.getFaulty() != 1)
		{
			reply = msg.createReply();
			myAgent.send(reply);
		}
	}
	
	private void Coalition()
	{
		agent.deregister();
		agent.register("coalition");
		reply = msg.createReply();
		reply.setContent("Confirmed");
		myAgent.send(reply);
	}
	
	private void Turn()
	{
		if (agent.isDebugBuild())
			System.out.println(myAgent.getLocalName() + " it's my turn");
		
		reply = new ACLMessage(0);
		
		do
		{
			if (agent.isDebugBuild())
				System.out.println("Querying...");
			result = search();
		}while (result.length == 0);
	    
        for (int i=0; i<result.length; i++)
        	reply.addReceiver(result[i].getName());
		
        reply.setConversationId("Query");
        reply.setContent(Double.toString(agent.getCoalitionValue()));
        myAgent.send(reply);
	}
	
	private DFAgentDescription[] search()
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
	
	private void Query()
	{
		newValue = agent.newCoalitionValue(Double.parseDouble(msg.getContent()));
		
		reply = msg.createReply();
		reply.setConversationId("Response");
		if (newValue > agent.getCoalitionValue())
			reply.setContent(Double.toString(newValue));
		myAgent.send(reply);
	}
	
	private void Update()
	{
		agent.setCoalitionValue(Double.valueOf(msg.getContent()));
		if (agent.isDebugBuild())
			System.out.println("New Value: " + String.valueOf(agent.getAgentValue()) + " | " + String.valueOf(agent.getCoalitionValue()) + " | " + String.valueOf(agent.getNumber()));
	}
	
	private void Response()
	{
		if (agent.isDebugBuild())
		{
			if (msg == null)
				System.out.println("Someone rejected me(" + myAgent.getLocalName() + ")");
			else
			{
				System.out.print(msg.getSender().getLocalName() + " - ");
				if (msg.getContent() == null) 
					System.out.println("Rejected me(" + myAgent.getLocalName() + ")");
				else 
					System.out.println("Accepted me(" + myAgent.getLocalName() + ")");
			}
		}
			
		if (msg != null && msg.getContent() != null)
			accepted--;
		anwsers--;
		
		if (anwsers == 0)
		{
			if (accepted == 0)
			{
				result = search();
				agent.newAgentValue();
				for (int i = 0; i < result.length; i++)
					msg.addReceiver(result[i].getName());
				msg.setConversationId("Update");
				
				agent.deregister();
				if (agent.isDebugBuild())
					System.out.println(myAgent.getLocalName() + " joins the coalition!");
				agent.register("coalition");
				
				myAgent.send(msg);								
			}
			else if (agent.isDebugBuild())
				System.out.println(myAgent.getLocalName() + " does not join the coalition!");
			
			if (agent.getFaulty() == 2)
				agent.setFaulty(1);
			else
			{
//				Impede que agentes sem falha fiquem refazendo o processamento para essa msg
				lastConversation = "";
				
				reply = new ACLMessage(0);
				reply.addReceiver(manager);
				reply.setConversationId("Turn");
				reply.setContent("Turn");
				
				myAgent.send(reply); 
			}
		}
	}	
	
	private void End()
	{
		done = true;
		reply = msg.createReply();
		reply.setContent(String.valueOf(agent.getAgentValue()) + "|" + String.valueOf(agent.getCoalitionValue()));
		myAgent.send(reply);
	}
		
	@Override
	public boolean done() {
		return done;
	}
	
	public int onEnd()
	{
		agent.decExecutions();
		agent.deregister();
		agent.register("agents");
		if (agent.isDebugBuild())
			System.out.println(agent.getLocalName() + " | Executions Left: " + agent.getExecutions());
		if (agent.getExecutions() > 0)
			agent.start();
		else
			myAgent.doDelete();
		return 0;
	}
}
