public class PrecisionSearch
{
	public static void main(String[] args)throws Exception
	{
		try
		{
			String queryStr = args[0];
			Query query = new Query(queryStr);
			float targetPrecision = Float.parseFloat(args[1]);
			String appID = args[2];
			
			Search search = new Search(query, targetPrecision, appID);
		
			float precision = search.initialSearch();
			while (precision < targetPrecision)
				precision = search.feedbackSearch();

			System.out.println("======================");
			System.out.println("FEEDBACK SUMMARY");
			System.out.println("Query: " + search.query);
			System.out.println("Precision: " + precision);
			System.out.println("Desired precision reached, done");
		}
		catch (Exception e)
		{
			System.out.println("Usage: <query> <precision> <appID>");
			return;
		}
	}
}
