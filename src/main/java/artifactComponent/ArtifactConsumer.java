package artifactComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.impl.DefaultConsumer;
import org.apache.camel.impl.DefaultExchange;

import jason.asSemantics.Message;
import jason.asSemantics.Unifier;
import jason.asSyntax.ListTerm;
import jason.asSyntax.ListTermImpl;
import jason.asSyntax.Literal;
import jason.asSyntax.ObjectTermImpl;
import jason.asSyntax.Term;
import jason.asSyntax.VarTerm;

public class ArtifactConsumer extends DefaultConsumer {
	private final ArtifactEndpoint endpoint;

	// private static HashMap<String, ArrayList<String>> operations = new HashMap<String, ArrayList<String>>();
	private static HashMap<String, ArtifactConsumer> consumers = new HashMap<String, ArtifactConsumer>();
	private final static Logger logger = Logger.getLogger(ArtifactConsumer.class.getName());

	private String artName; // "agente" que o Jason vhe
	private String opName;
	private String[] args;
	private String argsString;
	private String workspace;
//	private ArrayList<String> register;

	public ArtifactConsumer(ArtifactEndpoint endpoint, Processor processor) {
		super(endpoint, processor);
		this.endpoint = endpoint;
		this.setArtName(endpoint.getEndpointUri().split("\\?")[0].split("\\/")[2]);
		this.opName = endpoint.getOperation();
		this.setWorkspace(endpoint.getWorkspace());
		this.args = endpoint.getArgs().substring(1, endpoint.getArgs().length()-1).trim().split(",");
		this.argsString = endpoint.getArgs();
		logger.setLevel(Level.INFO);
	}

	public void start() throws Exception {
		consumers.put(opName, this);
//		register = new ArrayList<String>();
//		register.add(artName);
//		register.add(opName);
//		for (String arg: args)
//			register.add(arg);
//		operations.putIfAbsent(opName, new ArrayList<ArrayList<String>>());
//		operations.get(opName).add(register);
		super.start();
	}

	public void stop() throws Exception {
		consumers.remove(opName, this);
//		if(operations.get(opName).size() == 1)
//			operations.remove(opName);
//		else
//			operations.get(opName).remove(register);
		super.stop();

	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

//	public static HashMap<String, ArrayList<ArrayList<String>>> getOperations(){
//		return JasonConsumer.operations;
//	}
	
	public static HashMap<String, ArtifactConsumer> getConsumers(){
		return ArtifactConsumer.consumers;
	}
	
	public void operate(Term[] arguments) {
		// Checar se a aridade bate
		// criar um exchange
		// colocar no exchange os args como propriedades
		logger.info("Operation invoked - "+opName+" /"+arguments.length+" : "+argsString);
		
		if(arguments.length != args.length) {
			logger.warning("Expected number of args: " + args.length + ", got " + args.length);
			return ;
		}
		
		Exchange exchange = new DefaultExchange(endpoint);
		
		for(int index = 0; index<args.length; index++) {
			exchange.setProperty(args[index], arguments[index].toString());
		}
		
		logger.info("Exchange created for processing: "+exchange);
		try {
			getProcessor().process(exchange);
			logger.info("Processed successfully");
		} catch (Exception e) {
			logger.warning("Processed badly");
			e.printStackTrace();
		}
		
		
		
		/*
		if(endpoint.getSource() != null && !endpoint.getSource().equals(m.getSender())) {
			System.err.println("This route is not to receive this source: " + m.getSender() + ", only: " + endpoint.getSource());
			return ;
		}
		*/

		/*
		if(endpoint.getContent() != null) {
			Unifier u = new Unifier();

			// Needs to be applicable to atom and lists as well
			// from list to (atom, literal, list)
			Term contentLit = null;
			
			if(m.getPropCont() instanceof ListTerm) {
				contentLit = ListTermImpl.parseList(endpoint.getContent());
			}else {
				contentLit = Literal.parseLiteral(endpoint.getContent());				
			}
			
//			logger.info("Unifying "+contentLit.toString()+" with "+m.getPropCont().toString());
			if(u.unifies((Term)m.getPropCont(), contentLit)) {
				for(VarTerm t: u) {
					exchange.setProperty(t.toString(), u.get(t));
				}
			}else {
				logger.warning("Unification failed.");
				return;
			}
		 }
		 */
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

	public String getWorkspace() {
		return workspace;
	}

	public void setWorkspace(String workspace) {
		this.workspace = workspace;
	}

	public String getArtName() {
		return artName;
	}

	public void setArtName(String artName) {
		this.artName = artName;
	}

}
