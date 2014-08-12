package ch.psi.zmq.broker;

import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.media.sse.SseBroadcaster;

public class ResourceBinder extends AbstractBinder {

	private final Broker broker;
	
	public ResourceBinder(Broker broker){
		this.broker = broker;
	}
	
    @Override
    protected void configure() {
    	bind(broker).to(Broker.class);
    	bind(new SseBroadcaster()).to(SseBroadcaster.class);
    }

}