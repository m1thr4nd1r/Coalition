package agents;

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
	double coalitionValue;
	int agents, number;
	boolean debug;
		
	public void register(String serviceType)
	{
		dfd = new DFAgentDescription();
	    sd = new ServiceDescription();
		dfd.setName(getAID());
		sd.setType(serviceType);		
		sd.setName(String.valueOf(number));
		dfd.addServices(sd);		
		try {  
	        DFService.register(this, dfd );  
	    }
	    catch (FIPAException fe) { 
	    	fe.printStackTrace(); 
    	}
	}
	
	public void deregister()
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
		number = Integer.valueOf(getLocalName().substring(getLocalName().length()-1));
		coalitionValue = Double.parseDouble(getArguments()[0].toString());
		register("agents");
		debug = (boolean) getArguments()[1];
		this.addBehaviour(new CoalitionBehaviour(this));
	}
	
	public void setCoalitionValue(double coalitionValue) {
		this.coalitionValue = coalitionValue;
	}
	
	public double getCoalitionValue()
	{
		return this.coalitionValue;
	}
	
	public boolean isDebugBuild()
	{
		return debug;
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