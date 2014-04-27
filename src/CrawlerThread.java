import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Connection;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;


public class CrawlerThread implements Runnable {

	private String url;
	private int depth;
	private ArrayList<Link> tobecrawled;
	private ArrayList<Link> extracted;
	private String ua = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:18.0) Gecko/20130119 Firefox/18.0";

	public CrawlerThread(){

		extracted = new ArrayList<Link>();
		tobecrawled = new ArrayList<Link>();

	}
	
	@Override
	public void run() {

		tobecrawled.addAll(Main.getThreadManager().getNext());

		if(!tobecrawled.isEmpty()) {
			
			for(Link templink : tobecrawled) {

				url = templink.getUrl();
				depth = templink.getDepth()+1;  //used to save the extracted links

				
				if(depth <= Main.getDesiredDepth()) {						
					extracted.addAll(getLinks(url));										
				}
								
			}

			if(!extracted.isEmpty()) {	
				Main.getThreadManager().add(extracted);
			}	
		}
	}


	private boolean acceptableResponse(Response res) {		
		if (res.contentType().matches("^(text/html).*$")) return true;		
		return false;
	}



	private boolean acceptableUrl(String href) {
		if(href.equals("")) return false;
		if(href.indexOf('?') > 0) return false;
		if(href.matches(".*(\\.jpg|\\.jpeg|\\.gif|\\.png|\\.pdf|\\.zip|\\.rar|\\.exe|\\.tar|\\.bmp|\\.css)\\s*$")) return false;
		//if(!href.matches("^.*(\\.html)|(\\.htm)|(\\.php)|(\\.php3)|(\\.asp)|(\\.aspx)|(\\.mspx)$")) return false;
		return true;
	}


	private ArrayList<Link> getLinks(String url) {
		ArrayList<Link> links = new ArrayList<Link>();
		Connection conn = null;
		Response res = null;
		Document doc = null;
		Elements urls = null;
		Elements media = null;		

		try {
			conn = Jsoup.connect(url).userAgent(ua);

		} catch (Exception e1) {
			Main.getThreadManager().registerError("Jsoup.connect() failed - " +url);
			//System.err.println("Jsoup.connect() failed - " +url);
		}

		try {
			res = conn.execute();
		} catch (IOException e1) {
			Main.getThreadManager().registerError("Failed to connect: " +url);
			//System.err.println("conn.execute() failed - " +url);
		}

		if(acceptableResponse(res)) {
			try {
				doc = res.parse();
			} catch (IOException e1) {
				Main.getThreadManager().registerError("res.parse() failed - " +url);
				//System.err.println("conn.execute() failed - " +url);
			}

			urls = doc.select("a[href]");
			media = doc.select("[src]");
		} else {
			System.out.println("Unacceptable response: " +res.contentType() +res.statusCode() +res.statusMessage());
		}


		if(doc!=null) {			

			for(Element e : urls){
				String href = e.attr("abs:href");
				if(acceptableUrl(href)) {
					Link link = new Link(href, depth);
					links.add(link);
				}
				

			}

			
			for(Element src : media) {
				if (src.tagName().equals("img")) {
					if((!src.attr("width").equals("") && Integer.parseInt(src.attr("width")) > 200) && (!src.attr("height").equals("") && Integer.parseInt(src.attr("height")) > 200))
					Main.getGUI().consoleOut(String.format(" * %s: <%s> %sx%s (%s)\n",
	                        src.tagName(), src.attr("abs:src"), src.attr("width"), src.attr("height"), src.attr("alt")));
				}
			}
			 
		}

		return links;
	}

	
	

}
