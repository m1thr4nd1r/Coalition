package agents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import jade.core.Agent;

@SuppressWarnings("serial")
public class StartAgent extends Agent {

//	private void combinations(int n)
//	{
//		Calcula as combinações de 1..n
//		int s[] = new int[n];
//		for (int i = 0; i < n; i++)
//			s[i] = i+1;
//		
//		List<String> results = new ArrayList<String>();
//		for ( int i = 1; i <= (1<<(s.length))-1; i++ ) 
//		{
//	      StringBuilder builder = new StringBuilder();
//	      for ( int j = 0; j < s.length; j++ ) 
//	      {
//	        if ( (i & (1<<j)) != 0) 
//	        {
//	          builder.append(s[j]);
//	        }
//	      }
//	      results.add(builder.toString());
//	    }
//	    System.out.println( results );
//	    ---------------------------------------
//		Junta os resultados em conjuntos que possuem todos os elementos
//	    ArrayList<int[]> comb = new ArrayList<int[]>();
//	    for (int i = results.size()-2; i > results.size() / 2; i--)
//	    {
//	    	System.out.println(results.get(i) + " | " + results.get(results.size() - i - 2));
//	    }
//		---------------------------------------
//	}
	
	Map<String, Double> characteristicFunction = new HashMap<String, Double>();
	
	public double calculateCoallitionValue(double agentValue, double coallitionValue)
	{
		return -1;
	}
	
	public double coallitionValue(int agent, int[] coallition)
	{
		String query = Arrays.toString(coallition).replace(", ", "").replace("]","").substring(1) + Integer.toString(agent);
		if (characteristicFunction.containsKey(query))
			return characteristicFunction.get(query);
		else
		{
			double v = 0;
			
			characteristicFunction.put(query, v);
		}
		return -1;
		
	}
	
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
		
		System.out.printf("v[%d]: %.2f | x[%d]: %.2f\n", n-1, v, n-1, x[n-1]);
		System.out.printf("Total: " + sum);
		
		return x;
	}
	
	protected void setup()
	{
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
//			Numero de Agentes
			int agents = Integer.parseInt((String) args[0]);
			double value = 100;
			//caracteristicFunction(agents,value);
			double a[] = initialValues(agents, value);
			coallitionValue(3, new int[] {1,2});
		}
	}
	
}
