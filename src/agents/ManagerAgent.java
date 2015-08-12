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
	private int agents, executionAmount, faulty[], faultyAmount,startAgent;
	private long timeout, execTime, startTime;
	private double value, a[], coalitionValue, b[];
	private String coalition;
	private Random rand;
	private PrintWriter writer;
	
	protected void setup()
	{
//		Numero de Agentes,valor (a ser distribuido),qntExecuções,qntFalhas,agente inicial da coalizão, isDebug(opcional)
		Object[] args = getArguments();
		if (args != null && args.length > 3)
		{
//			Lendo variaveis do input
			agents = Integer.parseInt((String) args[0]);
			value = Double.valueOf((String) args[1]);			
			executionAmount = Integer.parseInt((String) args[2]) + 1;
			faultyAmount = Integer.parseInt((String) args[3]);
			startAgent = Integer.parseInt((String) args[4]);
			debug = (args.length > 5 && (args[5].toString().charAt(0) == 't' || args[5].toString().charAt(0) == 'T'))? true : false;
			
//			Inicializando variaveis
			timeout = 120;
			faulty = new int[agents];
			rand = new Random();
			register();			
			selectInitialValues(value);
			b = a.clone();
			
//			Inicializando o arquivo
			try {
				writer = new PrintWriter("Coalition.csv", "UTF-8");				
			} catch (FileNotFoundException | UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
//			Criando variaveis necessarias a criacao de um novo agente (em um novo container)
			Runtime rt = Runtime.instance();
			Profile p = new ProfileImpl();
			ContainerController agentContainer = rt.createAgentContainer(p);			

//			Criando agentes
			for (int i = 0; i < agents; i++)
			{
				try {
					AgentController ac = agentContainer.createNewAgent("CoalitionAgent"+(i+1), "agents.CoalitionAgent", new Object[] { Double.valueOf(a[i]), executionAmount, 100, debug });
					ac.start();
				} catch (StaleProxyException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}		
			
//			Iniciando execucao
			beginWriting();
			startTime = System.currentTimeMillis();
			start(true);
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
	
	private void beginWriting()
	{
		String txt = "executionTime(ms);coalition;coalitionValue;";
		for (int i = 0; i < agents; i++)
			txt += "agent"+(i+1)+"-Begin;" + "agent"+(i+1)+"-End;";		
		txt += "faultyAgent(s);\n";
		
		writeToFile(txt);
	}
	
	public void start(boolean trusty)
	{
		coalitionValue = 0;
		selectFaulty(trusty);
		
		if (debug)
		{
			System.out.print("Trusty: ");
			for (int i = 0; i < agents; i++)
				System.out.print(faulty[i]+"("+(i+1)+") ");
			System.out.println();
		}
		
		addBehaviour(new ManagerBehaviour(this,agents,startAgent));
	}
	
	private void selectFaulty(boolean trusty)
	// 0 == sem falha
	// 1 == falha no inicio
	// 2 == falha depois de se juntar a coalizão
	{
		for (int i = 0; i < agents; i++)
			faulty[i] = 0;
		
		if (!trusty)
		{
			int faultyleft = faultyAmount, chosen;
			while (faultyleft > 0)
			{
				chosen = rand.nextInt(agents);
				if (faulty[chosen] == 0 && Integer.valueOf(startAgent) != chosen+1)
				{
					faulty[chosen] = 2;
					faultyleft--;
				}
			}
		}
	}
	
	public void writeToFile(String txt)
	{
		if (txt == "")
		{
			txt = execTime + ";" + coalition + ";";
			txt += String.valueOf(coalitionValue).replace('.',',') + ";";
			for (int i = 0; i < agents; i++)
			{
				txt += String.valueOf(a[i]).replace('.', ',')+";";
				txt += String.valueOf(b[i]).replace('.', ',')+";";
			}
			for (int i = 0; i < agents; i++)
				if (faulty[i] > 0)
					txt += String.valueOf(i+1) + " ";
			txt+=";\n";
		}
		
		writer.print(txt);
	}
	
	protected void takeDown() 
	{
		writer.close();
		System.out.println("Total Time: " + String.valueOf(System.currentTimeMillis() - startTime) + " ms (" + String.valueOf((System.currentTimeMillis() - startTime)/1000) + " s)");
		try { DFService.deregister(this); }
		catch (Exception e) {}
	}
	
	private void printB() {
		System.out.println("Printing Values");
		// TODO Auto-generated method stub
		for (int i = 0; i < b.length; i++)
			System.out.print(String.valueOf(b[i]) + " ");
		System.out.println(" | " + String.valueOf(coalitionValue));
	}

	public long getTimeOut()
	{
		return timeout;
	}
	
	public String getNumber(String s)
	{
		return s.substring(s.length()-1);
	}
	
	public void setCoalition(String coalition) {
		this.coalition = coalition;
	}

	public int getFaultyAmount() {
		return faultyAmount;
	}

	public int[] getFaulty() {
		return faulty;
	}
	
	public int getFaulty(int index) {
		return faulty[index];
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
	
	public void setExecTime(long l) {
		execTime = l;		
	}
}
