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
	double initialCV, coalitionValue, initialAV, agentValue;
	int agents, number, faulty, executions;
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
	
	public void start()
	{
		coalitionValue = initialCV;
		agentValue = initialAV;
		addBehaviour(new CoalitionBehaviour(this));
	}
	
	protected void setup()
	{
		number = Integer.valueOf(getLocalName().substring(getLocalName().length()-1));
		initialCV = Double.parseDouble(getArguments()[0].toString());
		initialAV = initialCV;
		register("agents");
//		register("all");
//		faulty = Integer.valueOf(getArguments()[1].toString());
		executions = Integer.valueOf(getArguments()[1].toString());
		debug = (boolean) getArguments()[2];
		start();		
	}
	
	public void setCoalitionValue(double coalitionValue) {
		this.coalitionValue = coalitionValue;
	}
	
	public void setAgentValue(double aV) {
		agentValue = aV;
	}
	
	public int getFaulty() {
		return faulty;
	}

	public void setFaulty(int faulty) {
		this.faulty = faulty;
	}

	public double getCoalitionValue()
	{
		return coalitionValue;
	}
	
	public int getExecutions() {
		return executions;
	}
	
	public void decExecutions() {
		executions--;
	}

	public double getAgentValue()
	{
		return agentValue;
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