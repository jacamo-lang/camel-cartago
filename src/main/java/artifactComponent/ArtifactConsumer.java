package artifactComponent;


import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultExchange;

import jason.asSyntax.Term;

public class ArtifactConsumer extends DefaultConsumer {
	private final ArtifactEndpoint endpoint;

	private static HashMap<String, ArtifactConsumer> consumers = new HashMap<String, ArtifactConsumer>();
	private final static Logger logger = Logger.getLogger(ArtifactConsumer.class.getName());

	private String artName;
	private String opName;
	private String[] args;
	private String argsString;

	public ArtifactConsumer(ArtifactEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		this.setArtName(endpoint.getEndpointUri().split("\\?")[0].split("\\/")[2]);
		this.opName = endpoint.getOperation();
		this.args = endpoint.getArgs().substring(1, endpoint.getArgs().length()-1).trim().replace(" ", "").split(",");
		this.argsString = endpoint.getArgs();
		logger.setLevel(Level.INFO);
	}

	public void start() throws Exception {
		consumers.put(opName, this);
		super.start();
	}

	public void stop() throws Exception {
		consumers.remove(opName, this);
		super.stop();

	}

	public Endpoint getEndpoint() {
		return endpoint;
	}
	
	public static HashMap<String, ArtifactConsumer> getConsumers(){
		return ArtifactConsumer.consumers;
	}
	
	public void operate(Term[] arguments) {

		logger.info("Operation invoked: "+opName+" /"+arguments.length+" - "+argsString);
		
		if(arguments.length != args.length) {
			logger.warning("Expected number of args: " + args.length + ", got " + args.length);
			return ;
		}
		
		Exchange exchange = new DefaultExchange(endpoint);
		
		for(int index = 0; index<args.length; index++) {
			exchange.setProperty(args[index], arguments[index].toString());
		}
		
		try {
			getProcessor().process(exchange);
			logger.info("Processed successfully");
		} catch (Exception e) {
			logger.warning("Processed badly");
			e.printStackTrace();
		}

	}

	public String getOpName() {
		return opName;
	}

	public void setOpName(String opName) {
		this.opName = opName;
	}

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}
	
	public String getArtName() {
		return artName;
	}

	public void setArtName(String artName) {
		this.artName = artName;
	}

}
