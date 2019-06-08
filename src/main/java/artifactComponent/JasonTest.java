package artifactComponent;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import jacamo.platform.DefaultPlatformImpl;

public class JasonTest extends DefaultPlatformImpl{
	@Override
	public void init(String[] args) throws Exception{
		System.out.println("Iniciando JasonTest.java");
		
//		jason.asSemantics.Message mens = new jason.asSemantics.Message("tell", "jomi", "bob", "mensagem(oi)", "33");
//		CentralisedAgArch agent = JaCaMoLauncher.getRunner().getAg("bob");
//		agent.receiveMsg(mens);
		
		CamelContext contexto = new DefaultCamelContext();
		try {
			contexto.addRoutes(new RouteBuilder() {
				@Override
				public void configure() throws Exception {
					/*
					from("timer://foo?fixedRate=false&period=100000")
                    .to("jason:bob?"
                    		+ "performative=tell"
                    		+ "&content=mensagem(10)"
                    		+ "&msgId=33");
                    */
					
                    from("jason-artifact:camel")
					.to("jason:bob?content=say(hi)");

//                    from("jason:bob?receiver=interruptor")
//                    // .transform(mj=vl(X), mi=X)
//                    .to("interruptor:Int1");
				}
			});

			contexto.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
