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
import java.util.Collections;
import java.util.Hashtable;
import java.util.Objects;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.jeromq.ZMQ.Socket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.BrokerException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;
import ch.psi.zmq.ZMQUtil;

/**
 * This broker class allows registering forwarders they notified by any
 * activities on the specified input socket.
 * 
 * @author meyer_d2
 * 
 */
public class BrokerImpl implements Broker {

	private static final Logger log = LoggerFactory.getLogger(BrokerImpl.class);

	private static final String ADDRESS_PATTERN = "(tcp://)[a-z0-9.*-]{1,40}(:)[0-9]{4,5}";

	private ExecutorService executorService;
	private Context context;
	private Socket in;

	private Integer fwId;

	private ConcurrentSkipListMap<Integer, Forwarder> forwarders;
	private Hashtable<String, String> config;

	private boolean isRunning;

	/**
	 * @throws BrokerException
	 *             {@link BrokerException}
	 */
	public BrokerImpl() throws BrokerException {
		forwarders = new ConcurrentSkipListMap<>();
		fwId = 0;
	}

	@Override
	public void configure(Hashtable<String, String> config) throws BrokerConfigurationException {
		try {
			Objects.requireNonNull(config, "Configuration cannot be null.");
			Objects.requireNonNull(config.get("address"), "Parameter \"address\" could not be found.");
		} catch (NullPointerException e) {
			throw new BrokerConfigurationException(e.getMessage(), e);
		}

		if (!config.get("address").matches(ADDRESS_PATTERN)) {
			throw new BrokerConfigurationException("Address is not valid.");
		}

		if (config.get("hwm") != null) {
			try {
				Integer.parseInt(config.get("hwm"));

			} catch (NumberFormatException e) {
				throw new BrokerConfigurationException("The value of high water mark must be an integer.", e);
			}
		}

		this.config = new Hashtable<>();
		this.config.put("hwm", "4");
		log.debug(MessageFormat.format("Set default high water mark to {0}.", this.config.get("hwm")));

		this.config.putAll(config);
		log.debug(MessageFormat.format("Configured broker with parameters: {0}.", config));
	}

	@Override
	public void start() {
		isRunning = true;

		executorService = Executors.newFixedThreadPool(8);
		log.info("Initialized executor service.");

		context = ZMQ.context(1);
		in = ZMQUtil.connect(context, ZMQ.PULL, config.get("address"), Integer.parseInt(config.get("hwm")));
		log.info("Initialized ZMQ socket connection.");

		executorService.submit(new Runnable() {

			private long dataNo = 0;

			@Override
			public void run() {
				log.info("Broker is now online.");

				while (!Thread.currentThread().isInterrupted()) {
					byte[] data = in.recv();
					dataNo++;
					byte[] content = null;
					boolean hasMore = in.hasReceiveMore();
					for (Forwarder fw : forwarders.values()) {
						fw.send(data, hasMore, dataNo);
					}

					while (hasMore) {
						content = in.recv();
						for (Forwarder fw : forwarders.values()) {
							fw.send(content, (hasMore = in.hasReceiveMore()), dataNo);
						}
					}
				}
			}
		});
	}

	@SuppressWarnings("unchecked")
	@Override
	public TreeMap<Integer, Forwarder> getForwarders() {
		if (forwarders == null) {
			return (TreeMap<Integer, Forwarder>) Collections.EMPTY_MAP;
		}
		return new TreeMap<>(forwarders);
	}

	@Override
	public void shutdown() {
		for (Forwarder fw : forwarders.values()) {
			fw.shutdown();
		}
		forwarders.clear();

		if (isRunning) {
			in.close();
			context.term();

			try {
				executorService.shutdownNow();
			} catch (Exception e) {
				log.error("", e);
			}
		}
		isRunning = false;
	}

	@SuppressWarnings("unchecked")
	@Override
	public Hashtable<String, String> getProperties() {
		return (Hashtable<String, String>) config.clone();
	}

	@Override
	public Integer addForwarder(Forwarder forwarder) {
		forwarder.start(context);
		Integer newId = ++fwId;
		forwarders.put(newId, forwarder);
		return newId;
	}

	@Override
	public void removeForwarderByAddress(String address) throws IllegalBrokerOperationException {
		if (address == null) {
			throw new IllegalBrokerOperationException("Address is null.");
		} else if (!config.get("address").matches(ForwarderImpl.ADDRESS_PATTERN)) {
			throw new IllegalBrokerOperationException("Address is not valid.");
		}

		for (Forwarder fw : forwarders.values()) {
			if (fw.getAddress().equals(address)) {
				forwarders.values().remove(fw);
				fw.shutdown();
				return;
			}
		}

		throw new IllegalBrokerOperationException("Could not find a forwarder with specified address.");
	}

	@Override
	public void removeForwarderById(Integer fwId) throws IllegalBrokerOperationException {
		if (fwId == null) {
			throw new IllegalBrokerOperationException("Id is null.");
		}

		if (forwarders.remove(fwId) == null) {
			throw new IllegalBrokerOperationException("Could not find a forwarder with specified id.");
		}
	}
}