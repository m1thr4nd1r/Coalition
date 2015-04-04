package agents;

import java.util.ArrayList;
import java.util.List;

import jade.core.Agent;

@SuppressWarnings("serial")
public class StartAgent extends Agent {

	private List<Integer> caracteristicFunction(int n, double v)
	{
		int s[] = new int[n];
		for (int i = 0; i < n; i++)
			s[i] = i+1;
		
		int[] masks = new int[n];
        for (int i = 0; i < n; i++)
            masks[i] = (1 << i);
        for (int i = 0; i < (1 << n); i++)
        {
            List<Integer> newList = new ArrayList<Integer>(n);
            for (int j = 0; j < n; j++)
                if ((masks[j] & i) != 0)
                    newList.add(s[j]);
            return newList;
        }
		return null;
	}
	
	protected void setup()
	{
		Object[] args = getArguments();
		if (args != null && args.length > 0)
		{
//			Numero de Agentes
			int agents = Integer.parseInt((String) args[0]);
			double value = 100;
			System.out.println(caracteristicFunction(agents,value));
		}
	}
	
}
