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
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;

import java.util.Hashtable;

import org.junit.Before;
import org.junit.Test;

import ch.psi.eiger.broker.exception.BrokerException;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.util.TestUtil;

@SuppressWarnings("javadoc")
public class ForwarderTest {

	@Before
	public void before() {
	}

	@Test
	public void newBrokerTest() {
		Forwarder fw = new ForwarderImpl();
		fw.shutdown();
	}

	@Test(expected = ForwarderConfigurationException.class)
	public void missingAddressTest() throws ForwarderConfigurationException  {
		Hashtable<String, String> config = new Hashtable<>();
		Forwarder fw = null;
		try {
			fw = new ForwarderImpl();
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			fw.shutdown();
			throw e;
		}
	}
	

	@Test(expected = ForwarderConfigurationException.class)
	public void invalidAddressTest() throws ForwarderConfigurationException {
		Hashtable<String, String> config = new Hashtable<>();
		config.put("address", "invalid");
		Forwarder fw = null;
		try {
			fw = new ForwarderImpl();
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("address"), is(true));
			fw.shutdown();
			throw e;
		}
	}
	
	@Test(expected = ForwarderConfigurationException.class)
	public void invalidHighWaterMarkTest() throws ForwarderConfigurationException {
		Hashtable<String, String> config = new Hashtable<>();
		config.put("address", "tcp://*:8080");
		config.put("hwm", "10x");
		Forwarder fw = null;
		try {
			fw = new ForwarderImpl();
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("high water mark"), is(true));
			fw.shutdown();
			throw e;
		}
	}
	
	@Test
	public void overrideHighWaterMarkTest() throws ForwarderConfigurationException {
		Hashtable<String, String> config = new Hashtable<>();
		config.put("address", "tcp://*:8080");
		config.put("hwm", "6");
		Forwarder fw = new ForwarderImpl();
		fw.configure(config);
		assertThat(fw.getProperties().get("hwm"), is("6"));
		fw.shutdown();
	}
	
	@Test
	public void sendModeAnyTest() throws ForwarderConfigurationException, Exception {
		Hashtable<String, String> config = new Hashtable<>();
		config.put("address", "tcp://*:8080");
		config.put("hwm", "6");
		Forwarder fw = new ForwarderImpl();
		fw.configure(config);
		
		TestUtil.setField(fw, "isRunning", true);
		ZMQSocketDummy socket = new ZMQSocketDummy();
		TestUtil.setField(fw, "outSocket", socket);
		
		fw.send("a".getBytes(), false, 1);
		assertThat(ZMQSocketDummy.lastMessage, is("a"));
		
		fw.send("b".getBytes(), false, 2);
		assertThat(ZMQSocketDummy.lastMessage, is("b"));

		fw.send("c".getBytes(), false, 3);
		assertThat(ZMQSocketDummy.lastMessage, is("c"));
	}

	@Test
	public void sendModeTriggeredTest() throws ForwarderConfigurationException, Exception {
		Hashtable<String, String> config = new Hashtable<>();
		config.put("address", "tcp://*:8080");
		config.put("fwTimeInterval", "1000");
		Forwarder fw = new ForwarderImpl();
		fw.configure(config);
		
		TestUtil.setField(fw, "isRunning", true);
		ZMQSocketDummy socket = new ZMQSocketDummy();
		TestUtil.setField(fw, "outSocket", socket);
		
		fw.send("a".getBytes(), false, 1);
		assertThat(ZMQSocketDummy.lastMessage, is(nullValue()));
		
		TestUtil.setField(fw, "mode", ForwarderImpl.Mode.PassThroughNextDataPackage);
		
		fw.send("b".getBytes(), false, 2);
		assertThat(ZMQSocketDummy.lastMessage, is("b"));

		fw.send("c".getBytes(), false, 3);
		assertThat(ZMQSocketDummy.lastMessage, is("b"));
		
		TestUtil.setField(fw, "mode", ForwarderImpl.Mode.PassThroughNextDataPackage);

		fw.send("d".getBytes(), false, 4);
		assertThat(ZMQSocketDummy.lastMessage, is("d"));
	}

	@Test
	public void arbitaryPropertyModificationTest() throws BrokerException {
		Hashtable<String, String> properties = new Hashtable<>();
		properties.put("address", "tcp://*:8080");
		Forwarder fw = new ForwarderImpl();
		fw.configure(properties);

		properties.put("hack", "super");

		assertThat(fw.getProperties().containsKey("hack"), is(false));

		assertThat(properties, is(not(fw.getProperties())));
	}
}
