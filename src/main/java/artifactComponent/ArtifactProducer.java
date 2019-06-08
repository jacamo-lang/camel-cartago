package artifactComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.camel.Endpoint;
import org.apache.camel.Exchange;
import org.apache.camel.ExchangePattern;
import org.apache.camel.impl.DefaultProducer;

import jacamo.infra.JaCaMoLauncher;
import jason.infra.centralised.CentralisedAgArch;
import jason.mas2j.AgentParameters;
import jason.asSemantics.Agent;
import jason.asSemantics.Event;
import jason.asSemantics.Intention;
import jason.asSemantics.TransitionSystem;
import jason.asSyntax.Atom;
import jason.asSyntax.Literal;
import jason.asSyntax.Trigger;
import jason.asSyntax.Trigger.TEOperator;
import jason.asSyntax.Trigger.TEType;



public class ArtifactProducer extends DefaultProducer {
	private final static Logger logger = Logger.getLogger(ArtifactProducer.class.getName());
	private ArtifactEndpoint endpoint;
	
	private static int idCounter = 1;
	private static ArrayList<String> customUsedIds = new ArrayList<String>();
	
	// list containing workspaces and properties/signals observed within it
	public static HashMap<String, Collection<Literal>> observableProperties = new HashMap<String, Collection<Literal>>();
	public static HashMap<String, Collection<Literal>> signals = new HashMap<String, Collection<Literal>>();
	
	public ArtifactProducer(ArtifactEndpoint endpoint) {
		super(endpoint);
		this.endpoint = endpoint;
		String workspace = endpoint.getWorkspace();
		if(!endpoint.getIsSignal())
			ArtifactProducer.observableProperties.putIfAbsent(workspace, new ArrayList<Literal>());
		
		logger.setLevel(Level.INFO);
	}

	public void process(Exchange exchange) throws Exception {
		String artName = null;
		try {
			artName = endpoint.getEndpointUri().split("\\?")[0].split("\\/")[2];
		}catch (Exception e){
			logger.warning(e.toString());
		}
		
		String workspace = endpoint.getWorkspace();
		String content = null;
		if(endpoint.getProperty() != null)
			content = endpoint.getProperty();
		else
			content = exchange.getIn().getBody().toString();

		Literal property = Literal.parseLiteral(content);
		
		property.addAnnot(Literal.parseLiteral("artifact_name("+artName+")"));
		property.addAnnot(Literal.parseLiteral("percept_type(obs_prop)"));
		property.addAnnot(Literal.parseLiteral("workspace("+workspace+")"));
		String updateMessage = "Observable property updated "+property;
		if(!endpoint.getIsSignal()) {
			for (Literal s: ArtifactProducer.observableProperties.get(workspace)){
				if(s.getAnnot("artifact_name").equals(property.getAnnot("artifact_name")) && s.getFunctor().equals(property.getFunctor()) && s.getArity() == property.getArity()) {
					ArtifactProducer.observableProperties.get(workspace).remove(s);
					break;
				}
			}
			ArtifactProducer.observableProperties.get(workspace).add(property);
		}else {
			Event signal = new Event(new Trigger(TEOperator.add, TEType.belief, property), new Intention());
			updateMessage = "New signal "+signal.getTrigger();
			for(CamelArtArch arch: ArtifactCamel.getArchList())
				if(arch.insideWorkspace(workspace) && arch.hasFocusOn(artName))
					arch.getTS().updateEvents(signal);
		}
		logger.info(updateMessage);
	}

	public static HashMap<String, Collection<Literal>> getObservableProperties(){
		return ArtifactProducer.observableProperties;
	}
	
	public static HashMap<String, Collection<Literal>> getSignals(){
		return ArtifactProducer.signals;
	}
	
	public static void clearSignals(String workspace) {
		ArtifactProducer.signals.get(workspace).clear();
	}
	
	public void start() throws Exception {
		// TODO Auto-generated method stub

	}

	public void stop() throws Exception {
		// TODO Auto-generated method stub

	}

	public boolean isSingleton() {
		// TODO Auto-generated method stub
		return false;
	}

	public Endpoint getEndpoint() {
		return endpoint;
	}

	public Exchange createExchange() {
		return null;
	}

	public Exchange createExchange(ExchangePattern pattern) {
		// TODO Auto-generated method stub
		return null;
	}

	public Exchange createExchange(Exchange exchange) {
		// TODO Auto-generated method stub
		return null;
	}

}
