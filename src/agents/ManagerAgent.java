package agents;

import java.util.Random;

import behaviours.ManagerBehaviour;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.domain.DFService;
import jade.domain.FIPAException;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAAgentManagement.ServiceDescription;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

@SuppressWarnings("serial")
public class ManagerAgent extends Agent {

	private boolean debug;
	
	private double[] initialValues(int n, double v)
	{
		Random r = new Random();
		double x[] = new double[n];
		double temp = v, sum = 0;
		
		for (int i = 0; i < n-1; i++)
		{
			do
				x[i] = r.nextDouble() * v;
			while (x[i] > v - (n - i - 1));
			v-=x[i];
			if (debug)
				System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", i, v, i, x[i]);
		}
		
		sum = temp - v;
		x[n-1] = temp - (temp - v);
		sum += x[n-1];
		
		if (debug)
		{
			System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", n-1, v-x[n-1], n-1, x[n-1]);
			System.out.printf("Total: " + sum);
		}
		
		return x;
	}
	
	public void register()
	{
		DFAgentDescription dfd = new DFAgentDescription();
		ServiceDescription sd = new ServiceDescription();
		sd.setName(getLocalName());
		sd.setType("manager");		
		dfd.addServices(sd);		
		try {  
	        DFService.register(this, dfd );  
	    }
	    catch (FIPAException fe) { 
	    	fe.printStackTrace(); 
    	}
	}
	
	protected void setup()
	{
		int agents;
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
//			Numero de Agentes
			agents = Integer.parseInt((String) args[0]);
			double value = 100;
			double a[] = initialValues(agents, value);
			this.register();
			debug = (args.length > 1 && args[1].toString().charAt(0) == 't')? true : false;
//			Criando variaveis necessarias a criacao de um novo agente (em um novo container)
			Runtime rt = Runtime.instance();
			Profile p = new ProfileImpl();
			ContainerController agentContainer = rt.createAgentContainer(p);			
//			Criando agentes
			for (int i = 0; i < agents; i++)
			{
				try {
					AgentController ac = agentContainer.createNewAgent("CoalitionAgent"+(i+1), "agents.CoalitionAgent", new Object[] { Double.valueOf(a[i]), debug });
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			this.addBehaviour(new ManagerBehaviour(this,agents));
		}
	}

	public boolean isDebugBuild()
	{
		return debug;
	}
	
	protected void takeDown() 
	{
		try { DFService.deregister(this); }
		catch (Exception e) {}
	}
}
