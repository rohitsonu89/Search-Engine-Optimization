import java.net.URL;
import java.net.URLConnection;
import java.util.HashMap;


public class SearchResult
{
	public static HashMap<String, String> cachedPages = new HashMap<String, String>();
	
	public String title;
	public String summary;
	public String date;
	public String dispurl;
	public String clickurl;
	public URLConnection connection;
	public int size;
	public String page;
	public boolean isRelevant = false;
	
	SearchResult(String title, String summary, String date, String dispurl, String clickurl, int size)
	{
		this.title = title;
		this.summary = summary;
		this.date = date;
		this.dispurl = dispurl;
		this.clickurl = clickurl;
		this.size = size;
		
		// get connection if it exists otherwise add to cachedConnections
		this.page = cachedPages.get(clickurl);
		if (this.page == null)
		{
			// start separate thread to get page data
			new GetPageData(this);
		}
	}

	private class GetPageData extends Thread implements Runnable 
	{
		private SearchResult result;
		
		GetPageData(SearchResult result)
		{
			this.result = result;
			start();
		}
		
		public void run()
		{
			try
			{
				URL url;
				url = new URL(result.clickurl);
				URLConnection connection = url.openConnection();
				connection.setDefaultUseCaches(true);
				result.page = HTMLReader.getHTMLText(connection);//DocParser.getDocString(connection);
				SearchResult.cachedPages.put(result.clickurl, result.page);
			}catch(Exception e)
			{
				result.page = "";
				return;
			}
		}
	}
	
	public String toString()
	{
		String str = " URL: http://" + this.dispurl + "\n";
		str += " Title: " + this.title + "\n";
		str += " Summary: " + this.summary + "\n";
		
		return str;
	}
}
