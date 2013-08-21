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
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;
import ch.psi.eiger.broker.model.BrokerConfig;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.zmq.ZMQUtil;

/**
 * This broker class allows registering forwarders they notified by any
 * activities on the specified input socket.
 * 
 * @author meyer_d2
 * 
 */
public class BrokerImpl implements Broker {

	private static final Logger LOG = LoggerFactory.getLogger(BrokerImpl.class);

	private static Integer nextId = 1;

	private final String ADDRESS_PATTERN = "(tcp://)[a-zA-Z0-9.*-]{1,200}(:)[0-9]{4,5}";

	private ExecutorService executorService;

	private Context context;

	private Socket in;

	private ConcurrentSkipListMap<Integer, Forwarder> forwarders;

	private boolean isRunning;

	private BrokerConfig config;

	private Integer id;

	/**
	 * Default constructor.
	 */
	public BrokerImpl() {
		forwarders = new ConcurrentSkipListMap<>();
		id = nextId++;
	}

	@Override
	public void configure(BrokerConfig config) throws BrokerConfigurationException {
		if (config == null) {
			throw new BrokerConfigurationException("Configuration cannot be null");
		}

		if (config.getAddress() == null || !config.getAddress().matches(ADDRESS_PATTERN)) {
			throw new BrokerConfigurationException("Address is not valid.");
		}
		
		if (config.getHwm() != null && config.getHwm() < 1) {
			throw new BrokerConfigurationException("High water mark cannot be lower than one.");
		}

		this.config = new BrokerConfig(config);

		if (this.config.getHwm() == null) {
			this.config.setHwm(4);
			LOG.debug(MessageFormat.format("Set default high water mark to {0}.", this.config.getHwm()));
		}
		LOG.debug(MessageFormat.format("Configured broker with parameters: {0}.", this.config));
	}

	@Override
	public void start() {
		initCore();
	}

	private void initCore() {
		isRunning = true;

		executorService = Executors.newFixedThreadPool(8);
		LOG.info("Initialized executor service.");

		context = ZMQ.context(1);
		in = ZMQUtil.connect(context, ZMQ.PULL, config.getAddress(), config.getHwm());
		LOG.info("Initialized ZMQ socket connection.");

		executorService.submit(new Runnable() {

			private long dataNo = 0;

			@Override
			public void run() {
				LOG.info("Broker is now online.");

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

	@Override
	public TreeMap<Integer, Forwarder> getForwarders() {
		if (forwarders == null) {
			return new TreeMap<>();
		}
		return new TreeMap<>(forwarders);
	}

	@Override
	public void shutdown() {
		shutdownCore();
	}

	private void shutdownCore() {
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
				LOG.error("", e);
			}
		}
		isRunning = false;
	}

	@Override
	public BrokerConfig getConfig() {
		return new BrokerConfig(config);
	}

	@Override
	public void shutdownAndRemoveForwarderByAddress(String address) throws IllegalBrokerOperationException {
		if (address == null) {
			throw new IllegalBrokerOperationException("Address is null.");
		} else if (!config.getAddress().matches(ForwarderImpl.ADDRESS_PATTERN)) {
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
	public void shutdownAndRemoveForwarderById(Integer fwId) throws IllegalBrokerOperationException {
		if (fwId == null) {
			throw new IllegalBrokerOperationException("Id is null.");
		}

		Forwarder fw = forwarders.remove(fwId);

		if (fw == null) {
			throw new IllegalBrokerOperationException("Could not find a forwarder with specified id.");
		}

		fw.shutdown();
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public Forwarder setupAndGetForwarder(ForwarderConfig config) throws ForwarderConfigurationException {
		Forwarder fw = null;
		try {
			fw = new ForwarderImpl(context);
			fw.configure(config);
			forwarders.put(fw.getId(), fw);
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				LOG.error("", e);
			}
		} catch (ForwarderConfigurationException e) {
			fw.shutdown();
			throw e;
		}
		return fw;
	}
}