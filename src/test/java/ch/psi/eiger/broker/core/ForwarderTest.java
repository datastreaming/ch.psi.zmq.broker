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
import static org.junit.Assert.assertThat;

import java.util.concurrent.ArrayBlockingQueue;

import org.junit.Test;

import ch.psi.eiger.broker.core.ForwarderImpl.DataContainer;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.eiger.broker.util.TestUtil;

@SuppressWarnings("javadoc")
public class ForwarderTest {

	@Test(expected = ForwarderConfigurationException.class)
	public void missingAddressTest() throws ForwarderConfigurationException {
		ForwarderConfig config = new ForwarderConfig();
		Forwarder fw = new ForwarderImpl(null);
		try {
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			fw.shutdown();
			throw e;
		}
	}

	@Test(expected = ForwarderConfigurationException.class)
	public void invalidAddressTest() throws ForwarderConfigurationException {
		ForwarderConfig config = new ForwarderConfig("invalid");
		Forwarder fw = new ForwarderImpl(null);
		try {
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("address"), is(true));
			fw.shutdown();
			throw e;
		}
	}

	@Test(expected = ForwarderConfigurationException.class)
	public void invalidHighWaterMarkTest() throws ForwarderConfigurationException {
		ForwarderConfig config = new ForwarderConfig("tcp://*:8080", -3);
		Forwarder fw = new ForwarderImpl(null);
		try {
			fw.configure(config);
		} catch (ForwarderConfigurationException e) {
			assertThat(e.getMessage().toLowerCase().contains("high water mark"), is(true));
			fw.shutdown();
			throw e;
		}
	}

	@Test
	public void overrideHighWaterMarkTest() throws ForwarderConfigurationException {
		ForwarderConfig config = new ForwarderConfig("tcp://*:8080", 11);
		Forwarder fw = new ForwarderImpl(null);
		fw.configure(config);
		assertThat(fw.getConfig().getHwm(), is(11));
		fw.shutdown();
	}

	@Test
	public void sendModeAnyTest() throws ForwarderConfigurationException, Exception {
		ForwarderConfig config = new ForwarderConfig("tcp://*:8080", 11);
		Forwarder fw = new ForwarderImpl(null);
		fw.configure(config);

		TestUtil.setField(fw, "isRunning", true);

		@SuppressWarnings("unchecked")
		final ArrayBlockingQueue<DataContainer> queue = TestUtil.getField(ArrayBlockingQueue.class, fw, "sendQueue");

		fw.send("a".getBytes(), false, 1);
		assertThat(new String(queue.take().data), is("a"));

		fw.send("b".getBytes(), false, 2);
		assertThat(new String(queue.take().data), is("b"));

		fw.send("c".getBytes(), false, 3);
		assertThat(new String(queue.take().data), is("c"));

		fw.shutdown();
	}

	@Test
	public void sendModeTriggeredTest() throws ForwarderConfigurationException, Exception {
		ForwarderConfig config = new ForwarderConfig("tcp://*:8080", 7, 1000L);
		Forwarder fw = new ForwarderImpl(null);
		fw.configure(config);

		TestUtil.setField(fw, "isRunning", true);

		@SuppressWarnings("unchecked")
		final ArrayBlockingQueue<DataContainer> queue = TestUtil.getField(ArrayBlockingQueue.class, fw, "sendQueue");

		fw.send("a".getBytes(), false, 1);
		assertThat(queue.isEmpty(), is(true));

		TestUtil.setField(fw, "mode", ForwarderImpl.Mode.PassNextFrameThrough);

		fw.send("b".getBytes(), false, 2);
		assertThat(new String(queue.take().data), is("b"));

		fw.send("c".getBytes(), false, 3);
		assertThat(queue.isEmpty(), is(true));

		TestUtil.setField(fw, "mode", ForwarderImpl.Mode.PassNextFrameThrough);

		fw.send("d".getBytes(), false, 4);
		assertThat(new String(queue.take().data), is("d"));
	}

}
