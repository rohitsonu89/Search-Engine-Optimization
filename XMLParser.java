import java.io.StringReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class XMLParser
{
	public static SearchResult[] getResults(String xmlStr)
	{
		//System.out.println(xmlStr);
		// get xml document from connection xml response
		Document xmlDoc = getXMLDoc(xmlStr);
		
		// determine number of results
		int numResults = 0;
		NodeList node = xmlDoc.getElementsByTagName("resultset_web");
		for(int i = 0; i < node.getLength(); i++)
		{
			if (node.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				Element resultSet = (Element) node.item(i);
				numResults = Integer.parseInt(resultSet.getAttribute("count"));
				break;
			}
		}
		
		// return results
		SearchResult[] results = new SearchResult[numResults];
		node = xmlDoc.getElementsByTagName("result");
		int resultNum = 0;
		for(int i = 0; i < node.getLength() && resultNum < numResults; i++)
		{
			if (node.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				Element result = (Element) node.item(i);
				String title = getElementValue(result, "title");
				String summary = getElementValue(result, "abstract");
				String date = getElementValue(result, "date");
				String dispurl = getElementValue(result, "dispurl");
				String clickurl = getElementValue(result, "url");
				int size = Integer.parseInt(getElementValue(result, "size"));
				
				results[resultNum++] = new SearchResult(title, summary, date, dispurl, clickurl, size);
			}
		}
		return results;
	}

	private static Document getXMLDoc(String xmlString)
	{
		try
		{
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		    DocumentBuilder db = dbf.newDocumentBuilder();
		    InputSource inputSource = new InputSource();
		    inputSource.setCharacterStream(new StringReader(xmlString));
		    return db.parse(inputSource);
		}
		catch (Exception e)
		{
			return null;
		}
	}
	
	private static String getElementValue(Element parent, String elementName)
	{
		NodeList node = parent.getElementsByTagName(elementName);
		for(int i = 0; i < node.getLength(); i++)
		{
			if (node.item(i).getNodeType() == Node.ELEMENT_NODE)
			{
				Element element = (Element) node.item(i);
				if (element.getFirstChild() == null)
					return "";
				String value = element.getFirstChild().getNodeValue().trim().replace("<b>", "").replace("</b>", "");
				return value;
			}
		}
	    return "";
	}
}
