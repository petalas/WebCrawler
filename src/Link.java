
public class Link {
	
	private String url = null;
	private int depth = 0;
	
	public Link(String url, int depth) {
		this.url = url;
		this.depth = depth;
	}
	
	@Override
	public boolean equals(Object arg0) {
		if(this.url.equalsIgnoreCase(((Link) arg0).getUrl())) return true;
		return false;
	}

	public int getDepth() {
		return depth;
	}

	public String getUrl() {
		return url;
	}
	
	

}
