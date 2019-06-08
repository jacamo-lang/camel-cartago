package artifactComponent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Vector;

import cartago.WorkspaceId;
import jacamo.infra.JaCaMoAgArch;
import jason.asSemantics.ActionExec;
import jason.asSemantics.Event;
import jason.asSyntax.Literal;

public class CamelArtArch extends JaCaMoAgArch {
	private Vector<String> focusedArtifacts;
	
	@Override
	public void init() throws Exception {
		super.init();
		focusedArtifacts = new Vector<String>();
		ArtifactCamel.insertArchList(this);
	}
	
	@Override
	public Collection<Literal> perceive(){
		Collection<Literal> perceptions = super.perceive();
		if(perceptions == null)
			perceptions = new ArrayList<Literal>();
		
		for (WorkspaceId ws: this.getCartagoArch().getAllJoinedWsps()) {
			Collection<Literal> percepts = ArtifactProducer.getObservableProperties().get(ws.getName());
			if(percepts != null)
				for(Literal literal: percepts)
					if(focusedArtifacts.contains(literal.getAnnot("artifact_name").getTerm(0).toString()))
						perceptions.add(literal);
		}
		
		return perceptions;
	}
	
	@Override
	public void act(ActionExec a)  {
		String functor = a.getActionTerm().getFunctor();
		if(functor.equals("focus") && a.getActionTerm().getTerm(0)!=null) {
			focusedArtifacts.add(a.getActionTerm().getTerm(0).toString());
			a.setResult(true);
			actionExecuted(a);
		}else if(ArtifactConsumer.getConsumers().containsKey(functor)) {
			ArtifactConsumer consumer = ArtifactConsumer.getConsumers().get(functor);
			
			boolean inWorkspace = false;
			boolean hasFocus = true;
			
			String reason = "Agent is not within the workspace";
			for (WorkspaceId ws: this.getCartagoArch().getAllJoinedWsps()) {
				if(consumer.getWorkspace().equals(ws.getName())) {
					
					inWorkspace = true;
					break;
				}
			}
			if(!focusedArtifacts.contains(consumer.getArtName())) {
				hasFocus = false;
				reason = "Agent must focus the artifact "+consumer.getArtName();
			}
			
			a.setResult(inWorkspace && hasFocus);
			if(inWorkspace && hasFocus)
				consumer.operate(a.getActionTerm().getTermsArray());
			else
				a.setFailureReason(a.getActionTerm().copy(), reason);
			
			actionExecuted(a);
		}else {
			System.out.println("No operation registered: "+a.getActionTerm().getFunctor());
			super.act(a);
		}
	}
	
	public void signalize(Event event) {
		this.getTS().updateEvents(event);
	}
	
	public boolean insideWorkspace(String wsName) {
		for (WorkspaceId ws: this.getCartagoArch().getAllJoinedWsps()) {
			if(wsName.equals(ws.getName())) {
				return true;
			}
		}
		return false;
	}
	
	public boolean hasFocusOn(String artName) {
		return focusedArtifacts.contains(artName);
	}
}
