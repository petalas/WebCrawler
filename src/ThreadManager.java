import java.util.ArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class ThreadManager extends Thread{

	private int poolSize = 4;

	private ArrayList<Link> crawled = null;
	private ArrayList<Link> linked = null;

	private ThreadFactory factory = Executors.defaultThreadFactory();	
	private ThreadPoolExecutor pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);

	private int workload = 1;
	private AtomicInteger errorcount = new AtomicInteger();
	private AtomicInteger attempts = new AtomicInteger();
	private long t0 = 0;
	private double lps = 0;
	private double errorpercentage = 0.0;

	public ThreadManager() {
		crawled = new ArrayList<Link>();
		linked = new ArrayList<Link>();
		pool.setKeepAliveTime(5, TimeUnit.SECONDS);
	}
	
	public void run() {		
		setPriority(MAX_PRIORITY);
		System.out.println("Core: " +pool.getCorePoolSize() +"\tMax: " +pool.getMaximumPoolSize());
		t0 = System.nanoTime();
		while(true){
			try {
				sleep(1000);
			} catch (InterruptedException e) {
			}
			
			errorpercentage = errorcount.doubleValue()/attempts.doubleValue();
			if(errorpercentage < 0.02) {
				poolSize = Math.min(384, (int) Math.ceil(poolSize*1.05));
				pool.setCorePoolSize(poolSize);
				pool.setMaximumPoolSize(poolSize);
			}
			
			if(errorpercentage > 0.1) {
				poolSize = Math.max(2, (int) Math.floor(poolSize*0.75));
				pool.setCorePoolSize(poolSize);
				pool.setMaximumPoolSize(poolSize);
			}
		}
	}

	public synchronized void add(ArrayList<Link> links){		

		if(!links.isEmpty()) {
			for(Link link : links) {
				if(!linked.contains(link) && !crawled.contains(link)) {
					linked.add(link);
					startNewThreads(2);
					attempts.getAndIncrement();
				}
			}	
		}

	}
	
	public synchronized ArrayList<Link> getNext() {
		ArrayList<Link> temp = new ArrayList<Link>();
		Link link = null;

		if(!linked.isEmpty()) {			

			workload = (int) Math.ceil((double) linked.size() / (double) poolSize);

			for(int i=0; i < workload; i++) {
				link = linked.get(0);
				linked.remove(0);
				crawled.add(link);
				temp.add(link);				
			}			

			//System.out.println("Linked: " +linked.size() +"\tPer Thread workload: " +workload +"\tActive threads: " +pool.getActiveCount());
		}
		return temp;
	}



	public synchronized int getActiveThreads() {
		return pool.getActiveCount();
	}

	public synchronized ArrayList<Link> getCrawled() {
		return crawled;
	}


	public synchronized int getCrawledSize(){
		return crawled.size();
	}

	public synchronized ArrayList<Link> getLinked() {
		return linked;
	}

	public synchronized int getLinkedSize(){
		return linked.size();
	}

	public synchronized double getLinksPerSecond() {
		long t1 = System.nanoTime();
		long timediff = t1 - t0;
		if((timediff/1000000000)!=0) {			
			lps = (double) getCrawledSize() / ((double)timediff/1000000000);			
		}		
		return lps;
	}

	

	public synchronized int getQueueSize(){
		return pool.getQueue().size();
	}

	public synchronized long getT0() {
		return t0;
	}

	public synchronized int getWorkloadPerThread() {
		return workload;
	}
	
	public synchronized double getErrorPercentage() {
		return errorpercentage;
	}

	public void registerError(String errormessage) {
		errorcount.getAndIncrement();
		System.err.println(("Errors so far: " +errorcount +"\tError message: " +errormessage));
	}
	
	public synchronized void resetT0() {
		t0 = System.nanoTime();
	}
	
	public synchronized void pause() {
		pool.shutdown();
		try {
			pool.awaitTermination(10, TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		pool.shutdownNow();
		
	}
	
	public synchronized void reset() {
		
		pause();
		crawled.clear();
		linked.clear();
		errorcount.getAndSet(0);
		attempts.getAndSet(0);
		errorpercentage = 0.0;
		lps = 0;
		workload = 0;
		poolSize = 4;
		pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(poolSize);
	}

	private void startNewThreads(int nthreads) {		
		for(int i=0; i < nthreads; i++) {
			pool.submit(factory.newThread(new CrawlerThread()));
		}
	}

}
