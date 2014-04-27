import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.text.DefaultCaret;


public class GUI extends Thread{
	
	private JFrame mainWindow = new JFrame("WebCrawler v1.0a");
	private JPanel panel = new JPanel();
	private JPanel northpanel = new JPanel();
	private JPanel centerpanel = new JPanel();
	
	private JTextField startingUrlTextfield = new JTextField("http://en.m.wikipedia.org/");
	private JLabel startingUrlTextfieldLabel = new JLabel("Start here: ");	
	
	private JTextField depthTextField = new JTextField("3");
	private JLabel depthTextFieldLabel = new JLabel("Depth: ");
	
	private JTextField linksPerSecondField = new JTextField("0");
	private JLabel linksPerSecondFieldLabel = new JLabel("Crawled Links/s: ");	
	
	private JTextField linkCountField = new JTextField("0");
	private JLabel linkCountFieldLabel = new JLabel("Crawled Links: ");
	
	private JTextField queueSizeField = new JTextField("0");
	private JLabel queueSizeFieldLabel = new JLabel("Queued Threads: ");
	
	private JTextField linkedSizeField = new JTextField("0");
	private JLabel linkedSizeFieldLabel = new JLabel("Extracted Links: ");
	
	private JTextField activeThreadsField = new JTextField("0");
	private JLabel activeThreadsFieldLabel = new JLabel("Active Threads: ");
	
	private JTextField workloadField = new JTextField("0");
	private JLabel workloadFieldLabel = new JLabel("Workload Per Thread: ");
	
	private JTextField lastUpdateField = new JTextField("0");
	private JLabel lastUpdateFieldLabel = new JLabel("Seconds since last update: ");
	
	private JTextField errorPercentageField = new JTextField("0.00 %");
	private JLabel errorPercentageLabel = new JLabel("Error Percentage: ");
	
	private JButton goButton = new JButton("Go.");
	private JButton stopButton = new JButton("Stop.");
	private JButton resetButton = new JButton("Reset.");
	
	boolean paused = true;
	
	private JTextArea console = new JTextArea();
	private JScrollPane scrollPanel = new JScrollPane(console);
	
	private long lastupdate = 0;
	
	public void run(){
		setPriority(MAX_PRIORITY);
		init();
		Main.getThreadManager().start();
		while(true){
			update();
			try {
				sleep(33);
			} catch (InterruptedException e) {
				System.err.println("Thread.Sleep(33) - FAILED");
			}
		}
	}
	
	public synchronized void clearConsole() {
		console.setText("");
		console.revalidate();
		console.repaint();
	}
	
	public synchronized void consoleOut(String message) {
		console.setText(console.getText() +message+"\r\n");
	}
	
	private void init(){
		mainWindow.setContentPane(panel);
		mainWindow.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		mainWindow.setTitle("Main Window");		
		
		panel.setBackground(Color.gray);
		panel.setLayout(new BorderLayout());
		northpanel.setLayout(new FlowLayout());
		centerpanel.setLayout(new FlowLayout());
		
		mainWindow.setSize(1600, 900);
		mainWindow.setLocation((1920-1024)/2, (1080-768)/2);
		mainWindow.setLocationRelativeTo(null);
		mainWindow.setResizable(false);
		
		
		//Origin url bar
		northpanel.add(setupPanel(startingUrlTextfield, startingUrlTextfieldLabel, 768));		
		
		//Search Depth
		northpanel.add(setupPanel((depthTextField), depthTextFieldLabel, 64));		
		
		//go button
		northpanel.add(goButton);
		goButton.setFont(new java.awt.Font("Trebuchet MS", 0, 16));
		goButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				if(!Main.getThreadManager().isAlive()) Main.getThreadManager().start();
				Main.getThreadManager().resetT0();
				String url = startingUrlTextfield.getText();
				Main.setDesiredDepth(Integer.parseInt(depthTextField.getText()));
				Link link = new Link(url, 0);
				ArrayList<Link> links = new ArrayList<Link>();
				links.add(link);
				Main.getThreadManager().add(links);					
			}		
		});
		
		//stopButton
		northpanel.add(stopButton);
		stopButton.setFont(new java.awt.Font("Trebuchet MS", 0, 16));
		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("Stopping.");
				Main.getThreadManager().pause();
				System.err.println("Stopped.");
			}		
		});
		
		//stopButton
		northpanel.add(resetButton);
		resetButton.setFont(new java.awt.Font("Trebuchet MS", 0, 16));
		resetButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				System.err.println("Reset.");
				if(!Main.getThreadManager().isAlive()) Main.getThreadManager().start();
				Main.getThreadManager().reset();
			}		
		});
		
		panel.add(northpanel, BorderLayout.NORTH);
		
		//Links Per Second
		centerpanel.add(setupPanel(linksPerSecondField, linksPerSecondFieldLabel), BorderLayout.CENTER);
		
		//link count
		centerpanel.add(setupPanel(linkCountField, linkCountFieldLabel), BorderLayout.CENTER);
		
		//queue Size
		centerpanel.add(setupPanel(queueSizeField, queueSizeFieldLabel), BorderLayout.CENTER);
		
		//linked
		centerpanel.add(setupPanel(linkedSizeField, linkedSizeFieldLabel), BorderLayout.CENTER);
		
		//active threads
		centerpanel.add(setupPanel(activeThreadsField, activeThreadsFieldLabel), BorderLayout.CENTER);
		
		//workload
		centerpanel.add(setupPanel(workloadField, workloadFieldLabel), BorderLayout.CENTER);
		
		//last update
		centerpanel.add(setupPanel(lastUpdateField, lastUpdateFieldLabel, 192), BorderLayout.CENTER);
		
		//Error percentage
		centerpanel.add(setupPanel(errorPercentageField, errorPercentageLabel), BorderLayout.CENTER);
		
		panel.add(centerpanel, BorderLayout.CENTER);

		
		//console output
		scrollPanel = new JScrollPane(console, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPanel.setSize(new Dimension(1024, 512));
		scrollPanel.setFont(new java.awt.Font("Trebuchet MS", 0, 14));
		panel.add(scrollPanel, BorderLayout.SOUTH);
		scrollPanel.setAutoscrolls(true);
		
		console.setEditable(false);
		console.setPreferredSize(scrollPanel.getSize());
		DefaultCaret caret = (DefaultCaret) console.getCaret();
		caret.setUpdatePolicy(DefaultCaret.ALWAYS_UPDATE);
		console.setAutoscrolls(true);	
				
		
		mainWindow.setVisible(true);
		panel.setVisible(true);
		
		mainWindow.pack();
		
		
	}
	
	
	
	
	private JPanel setupPanel(JTextField field, JLabel label) {		
		return setupPanel(field, label, 128);
	}
	
	
	private JPanel setupPanel(JTextField field, JLabel label, int width) {
		
		JPanel internal = new JPanel();
		internal.setLayout(new FlowLayout());
		internal.add(field);
		field.setPreferredSize(new Dimension(width, 32));
		field.setFont(new java.awt.Font("Trebuchet MS", 0, 16));
		internal.setBorder(BorderFactory.createTitledBorder(label.getText()));
		
		JPanel external = new JPanel();
		external.setLayout(new FlowLayout());
		//external.add(label);
		label.setFont(new java.awt.Font("Trebuchet MS", 0, 16));
		external.add(internal);		
		
		return external;
	}
	
	public synchronized void update() {
		
		
		if(System.nanoTime() - lastupdate > 16000000 ) {
			
			java.awt.EventQueue.invokeLater(new Runnable() {

				@Override
				public void run() {
					// TODO Auto-generated method stub
					linksPerSecondField.setText(String.format("%.3f", Main.getThreadManager().getLinksPerSecond()));
					linkCountField.setText("" +Main.getThreadManager().getCrawledSize());
					queueSizeField.setText("" +Main.getThreadManager().getQueueSize());
					linkedSizeField.setText("" +Main.getThreadManager().getLinkedSize());
					activeThreadsField.setText("" +Main.getThreadManager().getActiveThreads());
					workloadField.setText("" +Main.getThreadManager().getWorkloadPerThread());
					lastUpdateField.setText(String.format("%.5f", (double)(System.nanoTime()-lastupdate)/1000000000));
					errorPercentageField.setText(String.format("%.2f %%", Main.getThreadManager().getErrorPercentage()*100));
				}
				
			});
			
			
		}
		lastupdate = System.nanoTime();
		
		
	}
	
	

}
