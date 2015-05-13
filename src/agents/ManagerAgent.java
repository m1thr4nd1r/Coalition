package agents;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
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
	private int agents, executionAmount, faulty[], faultyAmount;
	private double value, a[], coalitionValue, b[];
	private Random rand;
	private PrintWriter writer;
	
	public void beginWriting()
	{
		String txt = "coalitionValue;";
		for (int i = 0; i < agents; i++)
			txt += "agent"+(i+1)+";";		
		txt += "faultyAgent(s);\n";
		
		writeToFile(txt);
	}
	
	public void writeToFile(String txt)
	{
		if (txt == "")
		{
			txt = String.valueOf(coalitionValue).replace('.',',') + ";";
			for (int i = 0; i < agents; i++)
				txt += String.valueOf(b[i]).replace('.', ',')+";";
			for (int i = 0; i < agents; i++)
				if (faulty[i] > 0)
					txt += String.valueOf(i+1) + " ";
			txt+=";\n";
		}
		
		writer.print(txt);
	}
	
	private void selectInitialValues(double v)
	{
		a = new double[agents];
		double temp = v, sum = 0;
		
		for (int i = 0; i < agents-1; i++)
		{
			do
				a[i] = rand.nextDouble() * v;
			while (a[i] > v - (agents - i - 1));
			v-=a[i];
			if (debug)
				System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", i, v, i, a[i]);
		}
		
		sum = temp - v;
		a[agents-1] = temp - (temp - v);
		sum += a[agents-1];
		
		if (debug)
		{
			System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", agents-1, v-a[agents-1], agents-1, a[agents-1]);
			System.out.printf("Total: " + sum);
		}
	}
	
	private void register()
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
	
	private void selectFaulty(boolean trusty)
	{
		if (!trusty)
		{
			int faultyleft = faultyAmount, chosen;
			while (faultyleft > 0)
			{
				chosen = rand.nextInt(agents);
				if (faulty[chosen] == 0)
				{
					faulty[chosen] = 1;
					faultyleft--;
				}
			}
		}
	}
	
	public void start(boolean trusty)
	{
		coalitionValue = 0;
		faulty = new int[agents];
		selectFaulty(trusty);
		selectInitialValues(value);
		b = a;
				
//		Criando variaveis necessarias a criacao de um novo agente (em um novo container)
		Runtime rt = Runtime.instance();
		Profile p = new ProfileImpl();
		ContainerController agentContainer = rt.createAgentContainer(p);			
//		Criando agentes
		for (int i = 0; i < agents; i++)
		{
			try {
				AgentController ac = agentContainer.createNewAgent("CoalitionAgent"+(i+1), "agents.CoalitionAgent", new Object[] { Double.valueOf(a[i]), String.valueOf(faulty[i]), debug });
				ac.start();
			} catch (StaleProxyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		addBehaviour(new ManagerBehaviour(this,agents));
	}
	
	protected void setup()
	{
		Object[] args = getArguments();
		if (args != null && args.length > 3)
		{
//			Numero de Agentes
			agents = Integer.parseInt((String) args[0]);
			value = Double.valueOf((String) args[1]);			
			executionAmount = Integer.parseInt((String) args[2]) + 1;
			faultyAmount = Integer.parseInt((String) args[3]);
			debug = (args.length > 4 && (args[4].toString().charAt(0) == 't' || args[4].toString().charAt(0) == 'T'))? true : false;
			rand = new Random();
			register();
			
			try {
				writer = new PrintWriter("Coalition.csv", "UTF-8");				
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
			beginWriting();
			start(true);
		}
	}

	public void setB(int i, double v) {
		b[i] = v;
	}

	public void setCoalitionValue(double coalitionValue) {
		this.coalitionValue = coalitionValue;
	}

	public int getExecutionAmount() {
		return executionAmount;
	}

	public void decExecutionAmount() {
		executionAmount--;
	}

	public boolean isDebugBuild()
	{
		return debug;
	}
	
	protected void takeDown() 
	{
		writer.close();
		try { DFService.deregister(this); }
		catch (Exception e) {}
	}
	
	public void printValues() {
		System.out.println("Printing Values");
		// TODO Auto-generated method stub
		for (int i = 0; i < b.length; i++)
			System.out.print(String.valueOf(b[i]) + " ");
		System.out.println(" | " + String.valueOf(coalitionValue));
	}
}
