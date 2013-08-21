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
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;

import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;
import ch.psi.eiger.broker.model.BrokerConfig;

@SuppressWarnings("javadoc")
public class BrokerTest {

	@Before
	public void before() {
	}

	@Test
	public void newBrokerTest() {
		BrokerImpl broker = new BrokerImpl();
		broker.shutdown();
	}

	@Test(expected = BrokerConfigurationException.class)
	public void missingAddressTest() throws BrokerConfigurationException {
		BrokerConfig config = new BrokerConfig(null, null);
		Broker broker = new BrokerImpl();
		try {
			broker.configure(config);
		} catch (BrokerConfigurationException e) {
			broker.shutdown();
			throw e;
		}
	}
	

	@Test(expected = BrokerConfigurationException.class)
	public void invalidAddressTest() throws BrokerConfigurationException {
		BrokerConfig config = new BrokerConfig("invalid");
		Broker broker = null;
		try {
			broker = new BrokerImpl();
			broker.configure(config);
		} catch (BrokerConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("address"), is(true));
			broker.shutdown();
			throw e;
		}
	}
	
	@Test(expected = BrokerConfigurationException.class)
	public void invalidHighWaterMarkTest() throws BrokerConfigurationException {
		BrokerConfig config = new BrokerConfig("tcp://*:8080");
		config.setHwm(-1);
		Broker broker = null;
		try {
			broker = new BrokerImpl();
			broker.configure(config);
		} catch (BrokerConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("high water mark"), is(true));
			broker.shutdown();
			throw e;
		}
	}
	
	@Test
	public void successfullInstantiationAndConfigurationTest() throws BrokerConfigurationException {
		BrokerConfig config = new BrokerConfig("tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(config);

		config = broker.getConfig();
		
		assertThat(broker.getForwarders().isEmpty(), is(true));
		assertThat(config.getAddress(), is("tcp://*:8080"));
		assertThat(config.getHwm(), is(notNullValue()));
		assertThat(config.getHwm(), is(4));
		
		broker.shutdown();
	}
	
	@Test
	public void overrideHighWaterMarkTest() throws BrokerConfigurationException {
		BrokerConfig config = new BrokerConfig("tcp://*:8080", 7);
		Broker broker = new BrokerImpl();
		broker.configure(config);
		assertThat(broker.getConfig().getHwm(), is(7));
		broker.shutdown();
	}
	
	
	// @Test
	// public void addAndRemoveForwarderTest() throws
	// BrokerConfigurationException, IllegalBrokerOperationException {
	// BrokerConfig config = new BrokerConfig("tcp://*:8080");
	// Broker broker = new BrokerImpl();
	// broker.configure(config);
	//
	// Forwarder fw1 = mock(Forwarder.class);
	// when(fw1.getId()).thenReturn(1);
	// Forwarder fw2 = mock(Forwarder.class);
	// when(fw2.getId()).thenReturn(2);
	//
	// broker.addForwarder(fw1);
	// assertThat(broker.getForwarders().size(), is(1));
	//
	// broker.addForwarder(fw2);
	// assertThat(broker.getForwarders().size(), is(2));
	//
	// broker.shutdownAndRemoveForwarderById(fw1.getId());
	// assertThat(broker.getForwarders().size(), is(1));
	//
	// assertThat(broker.getForwarders().values().iterator().next(), is(fw2));
	//
	// broker.shutdown();
	// }
	//
	// @Test
	// public void addAndRemoveForwarderByAddressTest() throws
	// BrokerConfigurationException, IllegalBrokerOperationException {
	// BrokerConfig config = new BrokerConfig("tcp://*:8080");
	// Broker broker = new BrokerImpl();
	// broker.configure(config);
	//
	// Forwarder fw1 = mock(Forwarder.class);
	// when(fw1.getAddress()).thenReturn("tcp://*:5000");
	// when(fw1.getId()).thenReturn(1);
	// Forwarder fw2 = mock(Forwarder.class);
	// when(fw2.getAddress()).thenReturn("tcp://*:5100");
	// when(fw2.getId()).thenReturn(2);
	//
	// broker.addForwarder(fw1);
	// assertThat(broker.getForwarders().size(), is(1));
	//
	// broker.addForwarder(fw2);
	// assertThat(broker.getForwarders().size(), is(2));
	//
	// broker.shutdownAndRemoveForwarderByAddress("tcp://*:5000");
	// assertThat(broker.getForwarders().size(), is(1));
	//
	// assertThat(broker.getForwarders().values().iterator().next(), is(fw2));
	//
	// broker.shutdown();
	// }
	//
	// @Test
	// public void isForwarderStartedAfterAddingTest() throws
	// BrokerConfigurationException, IllegalBrokerOperationException {
	// BrokerConfig config = new BrokerConfig("tcp://*:8080");
	// Broker broker = new BrokerImpl();
	// broker.configure(config);
	//
	// Forwarder fw1 = mock(Forwarder.class);
	// broker.addForwarder(fw1);
	// verify(fw1).start(any(Context.class));
	// broker.shutdown();
	// }

	@Test
	public void arbitaryPropertyModificationTest() throws BrokerConfigurationException, IllegalBrokerOperationException {
		BrokerConfig config = new BrokerConfig("tcp://*:8080");
		Broker broker = new BrokerImpl();
		broker.configure(config);
		
		config.setAddress("hack");

		assertThat(broker.getConfig().getAddress(), is("tcp://*:8080"));

		assertThat(config, is(not(broker.getConfig())));
	}
}
