package ch.psi.eiger.broker.webservice;

import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import ch.psi.eiger.broker.cmd.BrokerEngine;
import ch.psi.eiger.broker.core.Broker;
import ch.psi.eiger.broker.core.Forwarder;
import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.model.BrokerConfig;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.eiger.broker.rest.model.BrokerDto;
import ch.psi.eiger.broker.rest.model.ForwarderDto;

@SuppressWarnings("javadoc")
@Path("broker")
public class RESTBroker {

	private BrokerEngine brokerEngine;

	@PostConstruct
	public void postConstruct() {
		brokerEngine = BrokerEngine.getInstance();
	}

	@GET
	@Produces({ MediaType.TEXT_XML })
	public Response getCurrentConfiguration() {
		Broker broker = brokerEngine.getBroker();
		if (broker == null) {
			return Response.status(Status.OK).entity(new BrokerDto()).build();
		}

		BrokerConfig config = broker.getConfig();

		BrokerDto dto = new BrokerDto(broker.getId(), config.getAddress(), config.getHwm());
		for (Forwarder fw : broker.getForwarders().values()) {
			ForwarderConfig fwConfig = fw.getConfig();
			dto.forwarders.add(new ForwarderDto(fw.getId(), fwConfig.getAddress(), fwConfig.getHwm(), fwConfig.getFwTimeInterval(), fw.getMode().toString()));
		}
		return Response.status(Status.OK).entity(dto).build();
	}

	@DELETE
	public Response stopBroker() {
		Broker broker = brokerEngine.getBroker();
		if (broker == null) {
			return Response.status(Status.NOT_FOUND).entity("You have to create a broker first.").build();
		}
		brokerEngine.shutdownAndRemoveBroker();
		return Response.status(Status.OK).build();
	}

	@POST
	@Produces({ MediaType.TEXT_XML })
	public Response setup(JAXBElement<BrokerDto> brokerWrapper) throws JAXBException {
		if (brokerEngine.getBroker() != null) {
			return Response.status(Status.NOT_ACCEPTABLE).entity("There is already a broker!").build();
		}
		BrokerDto dto = brokerWrapper.getValue();
		try {
			Broker broker = brokerEngine.setupAndGetBroker(new BrokerConfig(dto));
			broker.start();
			for (ForwarderDto fwDto : dto.forwarders) {
				broker.setupAndGetForwarder(new ForwarderConfig(fwDto)).start();
			}
		} catch (BrokerConfigurationException | ForwarderConfigurationException e) {
			brokerEngine.shutdownAndRemoveBroker();
			return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
		}
		return getCurrentConfiguration();
	}
}
