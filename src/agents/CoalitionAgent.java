package agents;

import java.util.ArrayList;

import behaviours.CoalitionBehaviour;
import jade.core.Agent;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;

@SuppressWarnings("serial")
public class CoalitionAgent extends Agent {
	
	DFAgentDescription dfd;
	ServiceDescription sd;
	
	ArrayList<CoalitionAgent> coalition;
	ArrayList<String> coalitions;
	double coalitionValue;
	int agents;
	
	protected String coalitionMembers()
	{
		String names = "";
		for (int i = 0; i < coalition.size(); i++)
			names += coalition.get(i).getLocalName() + " ";
		return names;
	}
	
	protected void setup()
	{
		coalitions = new ArrayList<String>();
		coalition = new ArrayList<CoalitionAgent>();
		coalition.add(this);
		coalitionValue = Double.parseDouble(getArguments()[0].toString());
		
		agents = Integer.parseInt(getArguments()[1].toString());
		for (int i = 0; i < agents; i++)
			coalitions.add(String.valueOf(i+1));
		
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("coalition");
		String temp = this.getLocalName();
		sd.setName(temp.substring(temp.length()-1));
		dfd.addServices(sd);		
		try {  
	        DFService.register(this, dfd );  
	    }
	    catch (FIPAException fe) { 
	    	fe.printStackTrace(); 
    	}
		
		//System.out.println("Agent: " + this.getLocalName() + " | Coalition Value: " + coalitionValue + " | Coalition: " + coalitionMembers());
		//System.out.println("Service Name: " + temp + "(" + temp.substring(temp.length()-1) + ")");		
		this.addBehaviour(new CoalitionBehaviour(this));
	}
		
	public DFAgentDescription getDfd() {
		return dfd;
	}

	public int getAgents() {
		return agents;
	}

	public String getCoalition(int index)
	{
		return this.coalitions.get(index);
	}
	
	public int getCoalitions()
	{
		return this.coalitions.size();
	}
	
	public double getCoalitionValue()
	{
		return this.coalitionValue;
	}
	
	protected void takeDown() 
	{
		try { DFService.deregister(this); }
		catch (Exception e) {}
	}
}