import java.io.IOException;




public class Main {

	/**
	 * @param args
	 */
	
	
	static GUI gui;	
	static ThreadManager manager;	
	static int desiredDepth;
	static String ua;	
	static double lps;
	
	public static int getDesiredDepth(){
		return desiredDepth;
	}


	public static synchronized GUI getGUI(){
		return gui;
	
	}
	
	
	public static ThreadManager getThreadManager(){
		return manager;		
	}
	
	
	
	public static String getUA() {
		return ua;
	}
	
	private static void init() {
		gui = new GUI();
		manager = new ThreadManager();
		desiredDepth = 3;
		ua = "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:18.0) Gecko/20130119 Firefox/18.0";
		lps = 0;
		
		gui.start();
	}
	
	
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		
		init();

	}
	
	public static void setDesiredDepth(int depth){
		desiredDepth = depth;
	}

}
