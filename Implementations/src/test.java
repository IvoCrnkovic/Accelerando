import java.util.Scanner;
public class test { 
	public static void main(String[] args)
	{
		Scanner in = new Scanner(System.in);
		String x = in.nextLine();
		String[] y = x.split("(\\W)+");
		for (int i = 0; i < y.length; i++)
			System.out.println(y[i]);
	}
}
