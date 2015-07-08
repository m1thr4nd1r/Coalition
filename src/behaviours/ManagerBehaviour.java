package behaviours;

import java.util.ArrayList;
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
	private String temp, lastConversation;
	private ManagerAgent agent;
	private long lastMessage, startTime;
	private int turn,start, chosen,aux,done;
	private ArrayList<Integer> agentsLeft;
	private ACLMessage msg, reply;
	
	public ManagerBehaviour(ManagerAgent agent, int agents, int sAgent)
	{
		myAgent = this.agent = agent;
		done = start = agents;
		chosen = sAgent;
		DFAgentDescription[] results;
		
//		Inicializando as variaveis
		turn = aux = 0;
		temp = lastConversation = "";
		random = new Random();
		agentsLeft = new ArrayList<Integer>();
		msg = new ACLMessage(0);
		reply = null;		
		for (int i = 1; i <= agents; i++)
			if (i != chosen)
				agentsLeft.add(i);
		do
		{
			results = search();
		} while (results.length != agents);
		
		if (agent.isDebugBuild())
			System.out.println("Manager Found: " + results.length + " agents");
		
		startTime = System.currentTimeMillis();
		
//		Inicializando a execucao
		for (int i = 0; i < results.length; i++)
		{
			msg.clearAllReceiver();
			msg.addReceiver(results[i].getName());
			msg.setConversationId("Start");
			aux = Integer.valueOf(agent.getNumber(results[i].getName().getLocalName()));
			msg.setContent(String.valueOf(agent.getFaulty(aux-1)));
			myAgent.send(msg);
		}
		
		lastMessage = -1;
	}
	
	private DFAgentDescription[] search()
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
		
		while ((lastConversation != "" && (System.currentTimeMillis() - lastMessage) > agent.getTimeOut()) ||
			   (msg != null && msg.getConversationId() != null))
		{
			
			if (msg != null)
			{
				lastConversation = msg.getConversationId();
				if (lastConversation.equals("Done"))
//				Mesmo com falha, agentes enviam a mensagem done, logo msg != null	
					End();
			}
			
			if (lastConversation.equals("Turn"))
				Turn();
			else if (lastConversation.equals("Start"))
				Start();
			else if (lastConversation.equals("Coalition"))
				Coalition();
						
			msg = myAgent.receive();
			lastMessage = System.currentTimeMillis();
		}
	}
	
	private void Turn()
//	Acho que da pra melhorar esse codigo
	{
		reply = new ACLMessage(0);
		DFAgentDescription[] results = search();	
		
		if (agent.isDebugBuild() && msg == null)
			System.out.println("An agent failed to take it's turn (" + String.valueOf(System.currentTimeMillis() - lastMessage) + ").");
		
		if (agentsLeft.size() == 0)
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
			if (agent.isDebugBuild())
				System.out.println("Coalition fully formed.");
		}
		else
		{
			aux = random.nextInt(agentsLeft.size());
			turn = agentsLeft.get(aux);
			agentsLeft.remove(aux);
			aux = 0;
			
			while (!agent.getNumber(results[aux].getName().getLocalName()).equals(String.valueOf(turn)))
				aux++;
			
			reply.addReceiver(results[aux].getName());
			reply.setConversationId("Turn");
			
			if (agent.isDebugBuild())
				System.out.println(aux + " it's your turn(" + results[aux].getName().getLocalName() +")[" + String.valueOf(agent.getFaulty(turn-1)) + "]");
		}

		myAgent.send(reply);
	}
	
	private DFAgentDescription[] getCoalition()
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
	
	private void Start()
	{
		if (agent.isDebugBuild())
		{
			if (msg != null)
				System.out.println(msg.getSender().getLocalName() + " started.");
			else
				System.out.println("An agent failed to start (" + String.valueOf(System.currentTimeMillis() - lastMessage) + ").");
		}
		start--;
		
		if (start == 0)
		{
			reply = new ACLMessage(0);
			reply.addReceiver(myAgent.getAID());
			reply.setConversationId("Coalition");
			myAgent.send(reply);					
		}
	}
	
	private void Coalition()
	{
		reply = new ACLMessage(0);
		
		if (msg != null && msg.getContent() != null)
		{
			if (agent.isDebugBuild())
				System.out.println(msg.getSender().getLocalName() + " begins in the coalition.");
			
			reply.addReceiver(myAgent.getAID());
			reply.setConversationId("Turn");
		}
		else
		{
			if (agent.isDebugBuild() && msg == null)
				System.out.println("An agent failed to begin in the coalition (" + String.valueOf(System.currentTimeMillis() - lastMessage) + ").");
			
			DFAgentDescription[] results = search();
			for (aux = 0; aux < results.length; aux++)
				if (Integer.valueOf(agent.getNumber(results[aux].getName().getLocalName())) == chosen)
					break;
			
			reply.addReceiver(results[aux].getName());
			reply.setConversationId("Coalition");
		}
		
		myAgent.send(reply);
	}
	
	private void End()
	{
		done--;
		int i = msg.getContent().indexOf('|');
		agent.setB(Integer.valueOf(agent.getNumber(msg.getSender().getLocalName()))-1, Double.valueOf(msg.getContent().substring(0, i)));
		agent.setCoalitionValue(Double.valueOf(msg.getContent().substring(i+1)));
	}
	
	@Override
	public boolean done() {
		return done == 0;
	}
	
	public int onEnd()
	{
		System.out.println("Run #" + String.valueOf(agent.getExecutionAmount()) + " ended.");
		
		agent.setExecTime(System.currentTimeMillis() - startTime);
		agent.writeToFile("");
		agent.decExecutionAmount();
		if (agent.getExecutionAmount() > 0)
			agent.start(false);
		else
			myAgent.doDelete();
		return 0;
	}
}