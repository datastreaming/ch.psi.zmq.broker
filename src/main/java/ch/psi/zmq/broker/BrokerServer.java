package ch.psi.zmq.broker;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ws.rs.core.UriBuilder;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.glassfish.jersey.grizzly2.httpserver.GrizzlyHttpServerFactory;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.sse.SseBroadcaster;
import org.glassfish.jersey.media.sse.SseFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.grizzly.http.server.HttpServer;
import org.glassfish.grizzly.http.server.StaticHttpHandler;

import ch.psi.zmq.broker.model.Configuration;
import sun.misc.Signal;
import sun.misc.SignalHandler;

@SuppressWarnings("restriction")
public class BrokerServer {
	
	private static final Logger logger = Logger.getLogger(BrokerServer.class.getName());

	public static void main(String[] args) throws IOException, ParseException {

		// Option handling
		int port = 8080;
		String config = null;

		Options options = new Options();
		options.addOption("h", false, "Help");
		options.addOption("p", true, "Server port (default: "+port+")");
		options.addOption("c", true, "Initial configuration file");

		GnuParser parser = new GnuParser();
		CommandLine line = parser.parse(options, args);

		if (line.hasOption("p")) {
			port = Integer.parseInt(line.getOptionValue("p"));
		}
		if (line.hasOption("c")) {
			config = line.getOptionValue("c");
		}
		if (line.hasOption("h")) {
			HelpFormatter f = new HelpFormatter();
			f.printHelp("broker", options);
			return;
		}

		URI baseUri = UriBuilder.fromUri("http://" + InetAddress.getLocalHost().getHostName() + "/").port(port).build();

		
		Broker broker = createBroker(config);
		
		
		ResourceBinder binder = new ResourceBinder(broker);
		
		ResourceConfig resourceConfig = new ResourceConfig(SseFeature.class, JacksonFeature.class);
		resourceConfig.packages(BrokerServer.class.getPackage().getName()+".services"); // Services are located in services package
		resourceConfig.register(binder);
		HttpServer server = GrizzlyHttpServerFactory.createHttpServer(baseUri, resourceConfig);

		// Static content
		String home = System.getenv("BROKER_BASE");
		if (home == null) {
			home = "src/main/assembly";
		}
		home = home + "/www";
		server.getServerConfiguration().addHttpHandler(new StaticHttpHandler(home), "/static");
		

		logger.info("Broker started");
		logger.info(String.format("Management interface available at %sstatic/", baseUri));
		logger.info("Use ctrl+c to stop ...");

		// Signal handling
		final CountDownLatch latch = new CountDownLatch(1);
		Signal.handle(new Signal("INT"), new SignalHandler() {
			public void handle(Signal sig) {
				if(latch.getCount()==0){
					logger.info("Terminate broker by System.exit()");
					System.exit(1); // Terminate program after 2 ctrl+c 
				}
				latch.countDown();
			}
		});

		// Wait for termination, i.e. wait for ctrl+c
		try {
			latch.await();
		} catch (InterruptedException e) {
		}

		server.stop();
		
		broker.terminate();
		
	}
	
	/**
	 * Create Broker
	 * @param config
	 * @return
	 */
	private static Broker createBroker(String config) {
		
		final Broker broker = new Broker();
		
		if(config != null){
			// Read xml configuration file
			Configuration configuration = null;
			try {
				JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
	
				Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
				configuration = (Configuration) jaxbUnmarshaller.unmarshal(new File(config));
			} catch (JAXBException e) {
				logger.log(Level.SEVERE, "Unable to load configuration file", e);
				System.exit(-1);
			}
	
			logger.info(String.format("Start broker with configuation %s", config));
			broker.setConfiguration(configuration);
		}
		
		return broker;
	}
}