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
	
	//ArrayList<CoalitionAgent> coalition;
	ArrayList<String> coalitions;
	double coalitionValue;
	int agents, coalitionIndex,number;
	StartAgent manager;
	
//	public CoalitionAgent()
//	{
//		manager = null;
//		agents = 0;
//		coalitionValue = 0;
//		coalitionIndex = 0;
//		number = Integer.valueOf(getLocalName().substring(getLocalName().length()-1));
//		coalitions = null;
////		coalition = null;
//	}
	
//	protected String coalitionMembers()
//	{
//		String names = "";
//		for (int i = 0; i < coalition.size(); i++)
//			names += coalition.get(i).getLocalName() + " ";
//		return names;
//	}
	
	public void updateCoalitions(String coalition1, String coalition2)
	{
		coalitions.remove(coalition1);
		coalitions.remove(coalition2);
		coalitions.add(coalition1+coalition2);
		for (int i = 0; i < coalitions.size(); i++)
			if (coalitions.get(i).contains(String.valueOf(number)))
			{
				coalitionIndex = i;
				break;
			}
	}
		
	public void register(String serviceName)
	{
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType("coalition");		
		sd.setName(serviceName+serviceName);
		dfd.addServices(sd);		
		try {  
	        DFService.register(this, dfd );  
	    }
	    catch (FIPAException fe) { 
	    	fe.printStackTrace(); 
    	}
	}
	
	public void deRegister()
	{
		try {  
	        DFService.deregister(this);  
	    }
	    catch (FIPAException fe) { 
	    	fe.printStackTrace(); 
    	}
	}
	
	protected void setup()
	{
		coalitions = new ArrayList<String>();
		number = Integer.valueOf(getLocalName().substring(getLocalName().length()-1));
		coalitionValue = Double.parseDouble(getArguments()[0].toString());
		
		agents = Integer.parseInt(getArguments()[1].toString());
		for (int i = 0; i < agents; i++)
		{
			if ((i+1) == number) 
				coalitionIndex = i;
			coalitions.add(String.valueOf(i+1));
		}
		
		manager = (StartAgent) getArguments()[2];
		
		register(String.valueOf(number));
		
		this.addBehaviour(new CoalitionBehaviour(this));
	}
		
	public DFAgentDescription getDfd() {
		return dfd;
	}

	public int getAgents() {
		return agents;
	}
	
	public void setCoalitionValue(double coalitionValue) {
		this.coalitionValue = coalitionValue;
	}

	public StartAgent getManager() {
		return manager;
	}

	public String getCoalition()
	{
		return this.coalitions.get(coalitionIndex);
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
	
	public int getNumber()
	{
		return number;
	}
	
	protected void takeDown() 
	{
		try { DFService.deregister(this); }
		catch (Exception e) {}
	}
}