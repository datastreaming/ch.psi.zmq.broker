package ch.psi.eiger.broker.webservice;

import javax.annotation.PostConstruct;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBException;

import ch.psi.eiger.broker.cmd.BrokerEngine;
import ch.psi.eiger.broker.core.Broker;
import ch.psi.eiger.broker.core.Forwarder;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.eiger.broker.rest.model.ForwarderDto;

@SuppressWarnings("javadoc")
@Path("broker/forwarders")
public class RESTForwarder {

	private BrokerEngine brokerEngine;

	@PostConstruct
	public void postConstruct() {
		brokerEngine = BrokerEngine.getInstance();
	}

	@GET
	@Path("/{id}")
	@Produces({ MediaType.TEXT_XML })
	public Response getCurrentConfiguration(@PathParam("id") Integer id) {
		Broker broker = brokerEngine.getBroker();
		if (broker == null) {
			return Response.status(Status.OK).entity(new ForwarderDto()).build();
		}

		for (Forwarder fw : broker.getForwarders().values()) {
			if (fw.getId().equals(id)) {
				ForwarderConfig conf = fw.getConfig();
				return Response.status(Status.OK).entity(new ForwarderDto(id, conf.getAddress(), conf.getHwm(), conf.getFwTimeInterval(), fw.getMode().toString())).build();
			}
		}
		return Response.status(Status.NOT_FOUND).build();
	}

	@DELETE
	@Path("/{id}")
	public Response stopForwarder(@PathParam("id") Integer id) {
		Broker broker = brokerEngine.getBroker();
		if (broker == null) {
			return Response.status(Status.NOT_FOUND).entity("You have to create a broker first.").build();
		}

		try {
			broker.shutdownAndRemoveForwarderById(id);
			return Response.status(Status.OK).build();
		} catch (IllegalBrokerOperationException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
		}
	}

	@POST
	@Produces({ MediaType.TEXT_XML })
	public Response setup(JAXBElement<ForwarderDto> fwWrapper) throws JAXBException {
		Broker broker = brokerEngine.getBroker();
		if (broker == null) {
			return Response.status(Status.NOT_FOUND).entity("You have to create a broker first.").build();
		}

		ForwarderDto dto = fwWrapper.getValue();

		try {
			Forwarder fw = broker.setupAndGetForwarder(new ForwarderConfig(dto));
			fw.start();
			ForwarderConfig config = fw.getConfig();
			return Response.status(Status.OK).entity(new ForwarderDto(fw.getId(), config.getAddress(), config.getHwm(), config.getFwTimeInterval(), fw.getMode().toString())).build();
		} catch (ForwarderConfigurationException e) {
			return Response.status(Status.NOT_ACCEPTABLE).entity(e.getMessage()).build();
		}
	}
}
