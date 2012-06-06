/**Author: Ivo Crnkovic-Rubsamen
 * Date Created: Tuesday, June 5, 2012
 * Inputs: A set of Tweet Objects with weightings and polarizations already computed.
 * Outputs: A single integer, polarization rating of the TweetSet in total.
 * 
 */

public class SetEvaluator
{
	public static float SetEvaluator(float [] weightings, float [] polarizations)
	{
		int finalPolarization = 0;
		int sumWeightings = 0;
		
		for (int i = 0; i < weightings.length; i++)
		{
			finalPolarization += weightings[i] * polarizations[i];
			sumWeightings += weightings[i];
		}
		
		finalPolarization = finalPolarization / sumWeightings;
		
		return finalPolarization;
	}
}
