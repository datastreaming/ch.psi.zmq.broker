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

package ch.psi.eiger.broker.core;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Hashtable;

import org.jeromq.ZMQ.Context;
import org.junit.Before;
import org.junit.Test;

import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.BrokerException;

@SuppressWarnings("javadoc")
public class BrokerTest {

	@Before
	public void before() {
	}

	@Test
	public void newBrokerTest() throws BrokerException {
		BrokerImpl broker = new BrokerImpl();
		broker.shutdown();
	}

	@Test(expected = BrokerException.class)
	public void missingAddressTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		Broker broker = null;
		try {
			broker = new BrokerImpl();
			broker.configure(properties);
		} catch (BrokerException e) {
			broker.shutdown();
			throw e;
		}
	}
	

	@Test(expected = BrokerConfigurationException.class)
	public void invalidAddressTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "invalid");
		Broker broker = null;
		try {
			broker = new BrokerImpl();
			broker.configure(properties);
		} catch (BrokerConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("address"), is(true));
			broker.shutdown();
			throw e;
		}
	}
	
	@Test(expected = BrokerConfigurationException.class)
	public void invalidHighWaterMarkTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		properties.put("hwm", "10x");
		Broker broker = null;
		try {
			broker = new BrokerImpl();
			broker.configure(properties);
		} catch (BrokerConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("high water mark"), is(true));
			broker.shutdown();
			throw e;
		}
	}
	
	@Test
	public void successfullInstantiationAndConfigurationTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(properties);
		
		assertThat(broker.getForwarders().isEmpty(), is(true));
		assertThat(broker.getProperties().containsKey("address"), is(true));
		assertThat(broker.getProperties().containsKey("hwm"), is(true));
		assertThat(broker.getProperties().get("hwm"), is("4"));
		
		broker.shutdown();
	}
	
	@Test
	public void overrideHighWaterMarkTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		properties.put("hwm", "6");
		Broker broker = new BrokerImpl();
		broker.configure(properties);

		assertThat(broker.getProperties().get("hwm"), is("6"));
		
		broker.shutdown();
	}
	
	
	@Test
	public void addAndRemoveForwarderTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(properties);
		
		Forwarder fw1 = mock(Forwarder.class);
		Forwarder fw2 = mock(Forwarder.class);
		
		Integer fwId1 = broker.addForwarder(fw1);
		assertThat(broker.getForwarders().size(), is(1));
		
		broker.addForwarder(fw2);
		assertThat(broker.getForwarders().size(), is(2));
		
		broker.removeForwarderById(fwId1);
		assertThat(broker.getForwarders().size(), is(1));
		
		assertThat(broker.getForwarders().values().iterator().next(), is(fw2));
		
		broker.shutdown();
	}
	
	@Test
	public void addAndRemoveForwarderByAddressTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(properties);
		
		Forwarder fw1 = mock(Forwarder.class);
		when(fw1.getAddress()).thenReturn("tcp://*:5000");
		Forwarder fw2 = mock(Forwarder.class);
		when(fw2.getAddress()).thenReturn("tcp://*:5100");
		
		broker.addForwarder(fw1);
		assertThat(broker.getForwarders().size(), is(1));
		
		broker.addForwarder(fw2);
		assertThat(broker.getForwarders().size(), is(2));
		
		broker.removeForwarderByAddress("tcp://*:5000");
		assertThat(broker.getForwarders().size(), is(1));
		
		assertThat(broker.getForwarders().values().iterator().next(), is(fw2));
		
		broker.shutdown();
	}
	
	@Test
	public void isForwarderStartedAfterAddingTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(properties);
		
		Forwarder fw1 = mock(Forwarder.class);
		broker.addForwarder(fw1);
		verify(fw1).start(any(Context.class));		
		broker.shutdown();
	}

	@Test
	public void arbitaryPropertyModificationTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(properties);
		
		properties.put("hack", "super");

		assertThat(broker.getProperties().containsKey("hack"), is(false));

		assertThat(properties, is(not(broker.getProperties())));
	}
}
