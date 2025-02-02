package app;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JTextField;

import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import edu.cmu.sphinx.util.props.ConfigurationManager;
import edu.cmu.sphinx.util.props.PropertyException;
import edu.cmu.sphinx.util.props.PropertySheet;
import edu.cmu.sphinx.util.props.S4Component;
import inpro.apps.SimpleReco;
import inpro.apps.SimpleText;
import inpro.apps.util.RecoCommandLineParser;
import inpro.incremental.PushBuffer;
import inpro.incremental.processor.TextBasedFloorTracker;
import inpro.incremental.sink.FrameAwarePushBuffer;
import inpro.incremental.source.GoogleASR;
import inpro.incremental.source.IUDocument;
import inpro.incremental.source.SphinxASR;
import inpro.incremental.unit.EditMessage;
import inpro.incremental.unit.IU;

public class DialogueSystemGoogleASR {
	
	protected Logger logger;
	
	@S4Component(type = SphinxASR.class)
	public final static String PROP_CURRENT_HYPOTHESIS = "currentASRHypothesis";	
	
	@S4Component(type = TextBasedFloorTracker.Listener.class)
	public final static String PROP_FLOOR_MANAGER_LISTENERS = TextBasedFloorTracker.PROP_STATE_LISTENERS;
	
	@S4Component(type = TextBasedFloorTracker.class)
	public final static String PROP_FLOOR_MANAGER = "textBasedFloorTracker";	
	
	private static ConfigurationManager cm;
	private static IUDocument iuDocument;
	private static PropertySheet ps;
	private List<PushBuffer> hypListeners;
	
	static List<EditMessage<IU>> edits = new ArrayList<EditMessage<IU>>();
	static int currentFrame = 0;
	static JTextField textField;

	
	public DialogueSystemGoogleASR () {
		logger = Logger.getLogger(this.getClass());
	}

	private void turnOffLoggers() {
		List<Logger> loggers = Collections.<Logger>list(LogManager.getCurrentLoggers());
		loggers.add(LogManager.getRootLogger());
		for ( Logger logger : loggers ) {
		    logger.setLevel(Level.OFF);
		}		
	}

	public void notifyListeners(List<PushBuffer> listeners) {
		if (edits != null && !edits.isEmpty()) {
			//logger.debug("notifying about" + edits);
			currentFrame += 100;
			for (PushBuffer listener : listeners) {
				if (listener instanceof FrameAwarePushBuffer) {
					((FrameAwarePushBuffer) listener).setCurrentFrame(currentFrame);
				}
				// notify
				listener.hypChange(null, edits);
				
			}
			edits = new ArrayList<EditMessage<IU>>();
		}
	}
	
	
	
	private void run() throws SQLException, IOException, UnsupportedAudioFileException {
		
		PropertyConfigurator.configure("log4j.properties");
		
		cm = new ConfigurationManager(new File("src/config/config.xml").toURI().toURL());
		
		
		ps = cm.getPropertySheet(PROP_CURRENT_HYPOTHESIS);
		hypListeners = ps.getComponentList(SphinxASR.PROP_HYP_CHANGE_LISTENERS, PushBuffer.class);
		TextBasedFloorTracker textBasedFloorTracker = (TextBasedFloorTracker) cm.lookup(PROP_FLOOR_MANAGER);
		iuDocument = new IUDocument();
		iuDocument.setListeners(hypListeners);
		SimpleText.createAndShowGUI(hypListeners, textBasedFloorTracker);
		
        new Thread() {
        	GoogleASR webSpeech = (GoogleASR) cm.lookup("googleASR");
        	
        	// These are 5 API keys which work, but have limited numbers of times you can call them
        	String key1 = "your_api_key_here";
            RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-M", "-G", key1});
            //RecoCommandLineParser rclp = new RecoCommandLineParser(new String[] {"-F", "resources/audio-samples/TakeTheRedBall.wav","-G", key1});

            public void run() {
                
                try {
                    final SimpleReco simpleReco = new SimpleReco(cm, rclp);
                   
                while (true) {
                    try {
                        
                    	//final SimpleReco simpleReco = new SimpleReco(cm, rclp);
                        new Thread(){ 
                            public void run() {
                                try {
                                    simpleReco.recognizeOnce();
                                    //System.out.println("ASR: Latency since last reco:");
                                    //System.out.println(System.currentTimeMillis()-lastreset);
                                } 
                                catch (PropertyException e) {
                                    e.printStackTrace();
                                } 
                                
                            }
                        }.start();
                        
                        Thread.sleep(50000);
                        logger.info("ASR:SHUTTING DOWN");
                     //   lastreset = System.currentTimeMillis();
                        webSpeech.shutdown(); //TODO add this back
                        
                        
                         //simpleReco.shutdownMic();
                    }
                        
                    catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                
                
            } catch (PropertyException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (UnsupportedAudioFileException e1) {
                e1.printStackTrace();
            }
        }
        }.start();
		
	turnOffLoggers();
	
}
	
	public static void main(String[] args) {
		try {
			new DialogueSystemGoogleASR().run();
		} 
		catch (SQLException e) {
			e.printStackTrace();
		} 
		catch (IOException e) {
			e.printStackTrace();
		} 
		catch (UnsupportedAudioFileException e) {
			e.printStackTrace();
		}
	}

}
