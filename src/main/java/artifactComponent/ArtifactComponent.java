package artifactComponent;

import java.util.Map;

import org.apache.camel.CamelContext;
import org.apache.camel.Endpoint;
import org.apache.camel.impl.DefaultComponent;

public class ArtifactComponent extends DefaultComponent {

	public void setCamelContext(CamelContext camelContext) {
		super.setCamelContext(camelContext);

	}

	public CamelContext getCamelContext() {
		// TODO Auto-generated method stub
		return super.getCamelContext();
	}

	public Endpoint createEndpoint(String uri, String remaining, Map<String, Object> parameters) throws Exception {
		//System.out.println(parameters);
		// manda o endereco e uma referencia do componente que criou (?)
		// JasonCamel.logger.info("Generating endpoint");
		ArtifactEndpoint endpoint = new ArtifactEndpoint(uri, this);
		setProperties(endpoint, parameters); // chama setters
		return endpoint;
	}
	
	public boolean useRawUri() {
		// TODO Auto-generated method stub
		return false;
	}

//	public EndpointConfiguration createConfiguration(String uri) throws Exception {
//		// TODO Auto-generated method stub
//		return null;
//	}
//
//	public ComponentConfiguration createComponentConfiguration() {
//		// TODO Auto-generated method stub
//		return null;
//	}

}
