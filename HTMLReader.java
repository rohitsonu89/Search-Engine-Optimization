/* Adapted from http://stackoverflow.com/questions/240546/removing-html-from-a-java-string
 * 
 */

import java.io.*;
import java.net.URLConnection;

import javax.swing.text.html.*;
import javax.swing.text.html.parser.*;

public class HTMLReader extends HTMLEditorKit.ParserCallback
{
	StringBuffer s;

	public void parse(Reader in) throws IOException
	{
		s = new StringBuffer();
		ParserDelegator delegator = new ParserDelegator();
		// the third parameter is TRUE to ignore charset directive
		delegator.parse(in, this, Boolean.TRUE);
	}

	public void handleText(char[] text, int pos) {
		s.append(text).append(" ");
	}

	public String getText() {
		return s.toString();
	}
	 
	public static String getHTMLText(URLConnection connection)
	{
		HTMLReader parser = new HTMLReader();
		try
		{
			String htmlString = DocParser.getDocString(connection)
		 					    .replaceAll("\\s", " ")
			 				    .replaceAll("<div class=\"references\">\\s<ol>.*?</ol>", " ");
			StringReader reader = new StringReader(htmlString);
		    parser.parse(reader);
		    reader.close();
		}
		catch (Exception e)
		{
			return "";
		}
	    return formatText(parser.getText());
	}
	public static String formatText(String text)
	{
		text = text.toLowerCase()
		   .replaceAll("&.+?;", " ")
		   .replaceAll("[^a-z0-9\\.\\-\\s]", " ")
		   .replaceAll("\\.+\\s+", " ")
		   .replaceAll("\\s[^a-z]{1}\\s", " ")
		   .replaceAll("\\s[0-9]+\\s", " ");
		
		return text;
	}
}
