/**
 * 
 * Copyright 2013 Paul Scherrer Institute. All rights reserved.
 * 
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 * 
 * This code is distributed in the hope that it will be useful, but without any
 * warranty; without even the implied warranty of merchantability or fitness for
 * a particular purpose. See the GNU Lesser General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this code. If not, see <http://www.gnu.org/licenses/>.
 * 
 */

package ch.psi.zmq.broker;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import sun.misc.Signal;
import sun.misc.SignalHandler;
import ch.psi.zmq.broker.model.Configuration;

@SuppressWarnings("restriction")
public class BrokerMain {
	
	private static final Logger logger = Logger.getLogger(BrokerMain.class.getName());

	public static void main(String[] args) {
		String config = args[0];
		
		// Read xml configuration file
		Configuration configuration = null;
		try{
			JAXBContext jaxbContext = JAXBContext.newInstance(Configuration.class);
			 
			Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
			configuration = (Configuration) jaxbUnmarshaller.unmarshal(new File(config));
		}
		catch(JAXBException e){
			logger.log(Level.SEVERE, "Unable to load configuration file", e);
			System.exit(-1);
		}

		logger.info(String.format("Start broker with configuation %s",config));
		final Broker broker = new Broker();
		broker.setConfiguration(configuration);
		
		logger.info("Broker started");
		
		Signal.handle(new Signal("INT"), new SignalHandler() {
			int counter = 0;
			public void handle(Signal sig) {
				if(counter>0){
					logger.info("Terminate broker by System.exit()");
					System.exit(1); // Terminate program after 2 ctrl+c 
				}
				broker.terminate();
				counter++;
			}
		});
		
	}

}
