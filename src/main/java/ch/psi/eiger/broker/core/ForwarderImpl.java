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

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.zmq.ZMQUtil;

/**
 * Use this class for forwarding bytes to a specific socket.
 * 
 * @author meyer_d2
 * 
 */
public class ForwarderImpl implements Forwarder {

	static final String ADDRESS_PATTERN = "(tcp://)[a-z0-9.*-]{1,40}(:)[0-9]{4,5}";

	private Logger log = LoggerFactory.getLogger(ForwarderImpl.class);

	private ZMQ.Socket outSocket;

	private Hashtable<String, String> config;

	private volatile boolean isRunning;
	
	private volatile Mode mode;
	
	private Timer timer;

	public ForwarderImpl() {
		mode = Mode.PassThroughAny;
	}

	@Override
	public void configure(Hashtable<String, String> config) throws ForwarderConfigurationException {
		try {
			Objects.requireNonNull(config, "Configuration cannot be null.");
			Objects.requireNonNull(config.get("address"), "Parameter \"address\" could not be found.");
		} catch (NullPointerException e) {
			throw new ForwarderConfigurationException(e.getMessage(), e);
		}

		if (!config.get("address").matches(ADDRESS_PATTERN)) {
			throw new ForwarderConfigurationException("Address is not valid.");
		}

		if (config.containsKey("hwm")) {
			try {
				Integer.parseInt(config.get("hwm"));

			} catch (NumberFormatException e) {
				throw new ForwarderConfigurationException("The value of high water mark must be an integer.", e);
			}
		}
		
		if (config.containsKey("fwTimeInterval")) {
			try {
				Long.parseLong(config.get("fwTimeInterval"));
				mode = Mode.Ignore;
			} catch (NumberFormatException e) {
				throw new ForwarderConfigurationException("The value for the forwarding interval must be a long.", e);
			}
		}

		log = LoggerFactory.getLogger(MessageFormat.format("{0}[{1}:{2}]", ForwarderImpl.class.getName(), ZMQ.PUSH, config.get("address")));

		this.config = new Hashtable<>();
		this.config.put("hwm", "4");
		log.debug(MessageFormat.format("Set default high water mark to {0}.", this.config.get("hwm")));

		this.config.putAll(config);
	}

	@Override
	public void start(Context context) {
		isRunning = true;
		outSocket = ZMQUtil.bind(context, ZMQ.PUSH, config.get("address"), Integer.parseInt(config.get("hwm")));
		log.info("Initialized ZMQ socket connection.");

		if (config.containsKey("fwTimeInterval")) {
			timer = new Timer();

			Long timeInMs = Long.parseLong(config.get("fwTimeInterval"));
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					log.debug("Pass through next data package.");
					mode = Mode.PassThroughNextDataPackage;
				}
			}, 0, timeInMs);
		}
		log.info("Forwarder is now online.");
	}

	@Override
	public void shutdown() {
		if (isRunning) {
			outSocket.close();
		}

		if (timer != null) {
			timer.cancel();
		}
		isRunning = false;
	}

	@Override
	public String getAddress() {
		return config.get("address");
	}

	@Override
	public void send(byte[] data, boolean hasReceiveMore, long frameNo) {
		if (isRunning && mode != Mode.Ignore) {
			// log.debug(MessageFormat.format("Send frame #{0}.", frameNo));
			outSocket.send(data, hasReceiveMore ? ZMQ.SNDMORE : 0);
			if (mode == Mode.PassThroughNextDataPackage && !hasReceiveMore) {
				mode = Mode.Ignore;
			}
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public Hashtable<String, String> getProperties() {
		return (Hashtable<String, String>) config.clone();
	}

	public static enum Mode {
		PassThroughAny,
		PassThroughNextDataPackage,
		Ignore;
	}
}