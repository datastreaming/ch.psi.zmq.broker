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
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.jeromq.ZMQ;
import org.jeromq.ZMQ.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.zmq.ZMQUtil;

/**
 * Use this class for forwarding bytes to a specific socket.
 * 
 * @author meyer_d2
 * 
 */
public class ForwarderImpl implements Forwarder {

	private Logger LOG = LoggerFactory.getLogger(ForwarderImpl.class);

	static final String ADDRESS_PATTERN = "(tcp://)[a-zA-Z0-9.*-]{1,200}(:)[0-9]{4,5}";

	private static Integer nextId = 1;

	private ZMQ.Socket outSocket;

	private ForwarderConfig config;

	private volatile boolean isRunning;

	private volatile Mode mode;

	private Timer timer;

	private ExecutorService pool;

	private ArrayBlockingQueue<DataContainer> sendQueue;

	private Integer id;

	private Context context;

	/**
	 * Default constructor.
	 * 
	 * @param context
	 *            {@link Context}
	 */
	public ForwarderImpl(Context context) {
		this.context = context;
		mode = Mode.PassAnyFramesThrough;
		pool = Executors.newFixedThreadPool(1);
		sendQueue = new ArrayBlockingQueue<>(5, true);
		id = nextId++;
	}

	@Override
	public void configure(ForwarderConfig config) throws ForwarderConfigurationException {
		if (config == null) {
			throw new ForwarderConfigurationException("Configuration cannot be null");
		}

		if (config.getAddress() == null || !config.getAddress().matches(ADDRESS_PATTERN)) {
			throw new ForwarderConfigurationException("Address is not valid.");
		}

		if (config.getHwm() != null && config.getHwm() < 1) {
			throw new ForwarderConfigurationException("High water mark cannot be lower than one.");
		}
		
		if (config.getFwTimeInterval() != null) {
			mode = Mode.IgnoreFrame;
		}
		
		this.config = new ForwarderConfig(config);

		if (this.config.getHwm() == null) {
			this.config.setHwm(4);
			LOG.debug(MessageFormat.format("Set default high water mark to {0}.", this.config.getHwm()));
		}
		LOG.debug(MessageFormat.format("Configured forwarder with parameters: {0}.", this.config));
	}

	@Override
	public void start() {
		outSocket = ZMQUtil.bind(context, ZMQ.PUSH, config.getAddress(), config.getHwm());
		LOG.info("Initialized ZMQ socket connection.");

		if (config.getFwTimeInterval() != null) {
			timer = new Timer();

			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					LOG.debug("Pass next frame through.");
					mode = Mode.PassNextFrameThrough;
				}
			}, 0, config.getFwTimeInterval());
		}

		pool.execute(new Runnable() {

			@Override
			public void run() {
				try {
					while (!Thread.currentThread().isInterrupted()) {
						DataContainer dc = sendQueue.take();
						outSocket.send(dc.data, dc.hasReceiveMore ? ZMQ.SNDMORE : 0);
					}
				} catch (InterruptedException e) {
				}
			}
		});

		isRunning = true;

		LOG.info("Forwarder is now online.");
	}

	@Override
	public void shutdown() {
		isRunning = false;

		pool.shutdownNow();

		sendQueue.clear();

		if (isRunning) {
			outSocket.close();
		}

		if (timer != null) {
			timer.cancel();
		}
	}

	@Override
	public String getAddress() {
		return config.getAddress();
	}

	@Override
	public void send(byte[] data, boolean hasReceiveMore, long internalFrameNo) {
		try {
			if (isRunning && mode != Mode.IgnoreFrame) {
				sendQueue.put(DataContainer.holdTogether(data, hasReceiveMore, internalFrameNo));
				if (mode == Mode.PassNextFrameThrough) {
					mode = Mode.IgnoreFrame;
				}
			} else if (isRunning) {
				LOG.debug(MessageFormat.format("Ignore frame {0}.", internalFrameNo));
			}
		} catch (InterruptedException e) {
			LOG.warn("", e);
		}
	}

	/**
	 * Do not need this class! It is public only for JUnit
	 * 
	 * @author meyer_d2
	 * 
	 */
	public static class DataContainer {
		byte[] data;
		boolean hasReceiveMore;

		static DataContainer holdTogether(byte[] data, boolean hasReceiveMore, long internalFrameNo) {
			DataContainer container = new DataContainer();
			container.data = data;
			container.hasReceiveMore = hasReceiveMore;
			return container;
		}
	}

	@Override
	public ForwarderConfig getConfig() {
		return new ForwarderConfig(config);
	}

	@Override
	public Integer getId() {
		return id;
	}

	@Override
	public Mode getMode() {
		return mode;
	}
}