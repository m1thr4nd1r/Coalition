package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import behaviours.ManagerBehaviour;
import jade.core.Agent;
import jade.core.Runtime;
import jade.core.Profile;
import jade.core.ProfileImpl;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;
import jade.wrapper.StaleProxyException;

@SuppressWarnings("serial")
public class StartAgent extends Agent {

	ArrayList<String> coalitions;
	
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
			System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", i, v, i, x[i]);
		}
		
		sum = temp - v;
		x[n-1] = temp - (temp - v);
		sum += x[n-1];
		
		System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", n-1, v-x[n-1], n-1, x[n-1]);
		System.out.printf("Total: " + sum);
		
		return x;
	}
	
	protected void setup()
	{
		int agents;
		coalitions = new ArrayList<String>();
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
//			Numero de Agentes
			agents = Integer.parseInt((String) args[0]);
			double value = 100;
			//caracteristicFunction(agents,value);
			double a[] = initialValues(agents, value);
//			Criando variaveis necessarias a criacao de um novo agente (em um novo container)
			Runtime rt = Runtime.instance();
			Profile p = new ProfileImpl();
			ContainerController agentContainer = rt.createAgentContainer(p);			
//			Criando agentes
			for (int i = 0; i < agents; i++)
			{
				coalitions.add(String.valueOf(i+1));
				try {
					AgentController ac = agentContainer.createNewAgent("CoalitionAgent"+(i+1), "agents.CoalitionAgent", new Object[] { Double.valueOf(a[i]), agents, this });
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			this.addBehaviour(new ManagerBehaviour(this,agents));
		}
	}

	public void updateCoalitions(String coalition1, String coalition2)
	{
		coalitions.remove(coalition1);
		coalitions.remove(coalition2);
		coalitions.add(coalition1+coalition2);
	}
}
