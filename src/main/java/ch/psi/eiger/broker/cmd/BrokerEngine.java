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

package ch.psi.eiger.broker.cmd;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.eiger.broker.core.Broker;
import ch.psi.eiger.broker.core.BrokerImpl;
import ch.psi.eiger.broker.core.Forwarder;
import ch.psi.eiger.broker.core.ForwarderImpl;
import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.BrokerException;
import ch.psi.eiger.broker.exception.CommandNotSupportedException;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;

/**
 * Use this builder class for creating a broker from command line.
 * 
 * @author meyer_d2
 * 
 */
public final class BrokerEngine {

	private static Logger log = LoggerFactory.getLogger(BrokerEngine.class);

	private Command currentCommandState;

	private Broker broker;

	private enum Command {

		Start("start"),

		CreateBroker("create broker"),

		AddForwarder("add forwarder"),

		ListForwarders("list forwarders"),

		RemoveForwarder("remove forwarder"),

		Exit("exit"),

		Help("help");

		private String name;

		private Command(String name) {
			this.name = name;
		}

		@Override
		public String toString() {
			return name;
		}

		private static Command findCommand(String name) throws CommandNotSupportedException {
			for (Command cmd : Command.values()) {
				if (name.startsWith(cmd.name)) {
					return cmd;
				}
			}
			throw new CommandNotSupportedException(MessageFormat.format("Command \"{0}\" is unkown.", name));
		}
	}

	@SuppressWarnings("javadoc")
	public BrokerEngine() {
		currentCommandState = Command.Start;
		writePrompt();
	}

	/**
	 * Starts the command line
	 */
	public void start() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
			String in = null;
			while ((in = br.readLine()) != null) {
				in = in.trim();
				if (in.equals(Command.Exit.name)) {
					break;
				} else if (in.equals(Command.Help.name)) {
					showHelpFile();
					writePrompt();
				} else {
					consume(in);
					writePrompt();
				}
			}
		} catch (IOException e) {
		}

		if (broker != null) {
			broker.shutdown();
		}
		System.out.println("bye");
	}

	private void showHelpFile() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(BrokerEngine.class.getClassLoader().getResourceAsStream("help.txt")))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e1) {
		}
	}

	private void consume(String in) {
		try {
			if (in.length() == 0) {
				return;
			}

			currentCommandState = Command.findCommand(in);

			switch (currentCommandState) {
			case Start:
				break;
			case CreateBroker:
				if (broker != null) {
					System.out.println("Broker was already created!");
					break;
				}

				Hashtable<String, String> brokerProperties = extractProperties(Command.CreateBroker, in);
				Broker newBroker = null;
				try {
					newBroker = new BrokerImpl();
					newBroker.configure(brokerProperties);
					broker = newBroker;
					broker.start();
					currentCommandState = Command.Start;
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				} catch (BrokerConfigurationException e) {
					newBroker.shutdown();
					throw e;
				}
				break;
			case AddForwarder:
				if (broker == null) {
					System.out.println("You must create a broker first!");
					break;
				}

				Hashtable<String, String> fwProperties = extractProperties(Command.AddForwarder, in);

				Forwarder fw = null;
				try {
					fw = new ForwarderImpl();
					fw.configure(fwProperties);
					broker.addForwarder(fw);
					try {
						Thread.sleep(1500);
					} catch (InterruptedException e) {
					}
				} catch (ForwarderConfigurationException e) {
					fw.shutdown();
					throw e;
				}
				break;
			case ListForwarders:
				if (isForwarderListNotEmpty()) {
					TreeMap<Integer, Forwarder> forwarders = broker.getForwarders();
					for (Integer id : forwarders.keySet()) {
						System.out.println(MessageFormat.format("{0}: {1}", id, forwarders.get(id).getProperties()));
					}
				}
				break;
			case RemoveForwarder:
				if (isForwarderListNotEmpty()) {
					Hashtable<String, String> properties = extractProperties(Command.RemoveForwarder, in);
					broker.removeForwarderByAddress(properties.get("address"));
					System.out.println("Successfully removed forwarder.");
				}
			}
		} catch (CommandNotSupportedException e) {
			illegalInput(e.getMessage() + " Type help for getting a list of supported commands.");
		} catch (IllegalBrokerOperationException | ForwarderConfigurationException | BrokerConfigurationException e) {
			illegalInput(e.getMessage());
		} catch (BrokerException e) {
			log.error("", e);
		}
	}

	private Hashtable<String, String> extractProperties(Command cmd, String in) {
		Hashtable<String, String> config = new Hashtable<>();
		String parameters = in.substring(cmd.name.length()).trim();

		String[] paramArray = parameters.split("( -)");

		for (int i = 0; i < paramArray.length; i++) {
			String paramAndValue = paramArray[i];
			if (paramAndValue.length() == 0) {
				continue;
			}
			int pos = paramAndValue.indexOf(" ");
			if (pos == -1) {
				continue;
			}
			String param = paramAndValue.substring(1, pos).trim();
			String value = paramAndValue.substring(pos + 1).trim();
			config.put(param, value);
		}
		return config;
	}

	private boolean isForwarderListNotEmpty() {
		if (broker == null) {
			System.out.println("You must create a broker first!");
			return false;
		}
		if (broker.getForwarders().isEmpty()) {
			System.out.println("The forwarder list is empty.");
			return false;
		}
		return true;
	}

	private void illegalInput(String message) {
		System.out.println(message);
	}

	private void writePrompt() {
		System.out.print("ZeroMQ Broker> ");
	}
}
