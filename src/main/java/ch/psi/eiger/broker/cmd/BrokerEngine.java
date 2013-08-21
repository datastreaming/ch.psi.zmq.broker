// $codepro.audit.disable
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
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import jline.console.ConsoleReader;
import jline.console.completer.StringsCompleter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.psi.eiger.broker.core.Broker;
import ch.psi.eiger.broker.core.BrokerImpl;
import ch.psi.eiger.broker.core.Forwarder;
import ch.psi.eiger.broker.exception.BrokerConfigurationException;
import ch.psi.eiger.broker.exception.CommandNotSupportedException;
import ch.psi.eiger.broker.exception.ForwarderConfigurationException;
import ch.psi.eiger.broker.exception.IllegalBrokerOperationException;
import ch.psi.eiger.broker.model.BrokerConfig;
import ch.psi.eiger.broker.model.ForwarderConfig;
import ch.psi.eiger.broker.server.GrizzlyServer;
import ch.psi.eiger.broker.webservice.RESTBroker;

/**
 * Use this command line-based builder for creating a broker.
 * 
 * @author meyer_d2
 * 
 */
public final class BrokerEngine {

	private final static Logger LOG = LoggerFactory.getLogger(BrokerEngine.class);

	private static BrokerEngine instance;

	private Command currentCommandState;

	private Broker broker;

	private GrizzlyServer server;

	static {
		instance = new BrokerEngine();
	}

	private enum Command {

		Start("start"),

		CreateBroker("create broker"),

		AddForwarder("add forwarder"),

		ListForwarders("list forwarders"),

		RemoveForwarder("remove forwarder"),

		EnableRESTInterface("enable rest interface"),

		DisableRESTInterface("disable rest interface"),

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

	private BrokerEngine() {
		currentCommandState = Command.Start;
	}

	/**
	 * Starts the command line reader.
	 */
	public void start() {
		try {
			processConfigFile();

			ConsoleReader console = new ConsoleReader();
			console.setPrompt("ZeroMQ Broker> ");
			console.addCompleter(new StringsCompleter(Command.EnableRESTInterface.name + " -port", Command.DisableRESTInterface.name, Command.CreateBroker.name, Command.CreateBroker.name + " -address tcp://:", Command.AddForwarder.name, Command.AddForwarder.name + " -address tcp://:",
					Command.ListForwarders.name, Command.RemoveForwarder.name, Command.RemoveForwarder.name
							+ " -address ", Command.Exit.name));

			String in = null;
			while ((in = console.readLine()) != null) {
				in = in.trim();
				if (in.equals(Command.Exit.name)) {
					break;
				} else if (in.equals(Command.Help.name)) {
					showHelpFile();
				} else {
					consume(in);
				}
			}
		} catch (Exception e) {
			LOG.error("", e);
		}

		if (broker != null) {
			broker.shutdown();
		}

		if (server != null) {
			server.stop();
		}

		System.out.println("bye");
	}

	private void processConfigFile() {
		String fileLocation = System.getProperty("configFile");
		if (fileLocation != null) {
			Path path = Paths.get(fileLocation);
			if (Files.exists(path, LinkOption.NOFOLLOW_LINKS)) {
				try {
					List<String> lines = Files.readAllLines(path, Charset.defaultCharset());
					for (String line : lines) {
						consume(line);
					}
				} catch (IOException e) {
					LOG.error("", e);
				}
			} else {
				LOG.warn(MessageFormat.format("Could not find specified config file {0}.", fileLocation));
			}
		} else {
			LOG.info("Configuration file not specified. Add -DconfigFile <path> to configure broker automatically.");
		}
	}

	private void showHelpFile() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(BrokerEngine.class.getClassLoader().getResourceAsStream("help.txt")))) {
			String line = null;
			while ((line = br.readLine()) != null) {
				System.out.println(line);
			}
		} catch (IOException e) {
			LOG.error("", e);
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

				setupAndGetBroker(extractAndValidateBrokerProperties(in)).start();

				break;
			case AddForwarder:
				if (broker == null) {
					System.out.println("You must create a broker first!");
					break;
				}

				broker.setupAndGetForwarder(extractAndValidateForwarderProperties(in)).start();

				break;
			case ListForwarders:
				if (isForwarderListNotEmpty()) {
					TreeMap<Integer, Forwarder> forwarders = broker.getForwarders();
					for (Integer id : forwarders.keySet()) {
						System.out.println(MessageFormat.format("{0}: {1}", id, forwarders.get(id).getConfig()));
					}
				}
				break;
			case RemoveForwarder:
				if (isForwarderListNotEmpty()) {
					Hashtable<String, String> properties = extractProperties(Command.RemoveForwarder, in);
					broker.shutdownAndRemoveForwarderByAddress(properties.get("address"));
					System.out.println("Successfully removed forwarder.");
				}
				break;
			case EnableRESTInterface:
				Hashtable<String, String> properties = extractProperties(Command.EnableRESTInterface, in);
				if (properties.containsKey("port")) {
					try {
						Integer port = Integer.parseInt(properties.get("port"));

						String webAppName = "brokerengine";
						if (properties.containsKey("appname")) {
							if (properties.get("appname").matches("[a-z]{1,}")) {
								webAppName = properties.get("appname");
							} else {
								throw new BrokerConfigurationException("Parameter appname is invalid. Make sure that the name contains only characters [a-z].");
							}
						}
						server = new GrizzlyServer("http://localhost/" + webAppName + "/", port, RESTBroker.class.getPackage());
						server.start();
					} catch (NumberFormatException e) {
						throw new BrokerConfigurationException("Parameter port must be an integer.");
					} catch (IOException e) {
						LOG.error("", e);
						if (server != null) {
							GrizzlyServer server = this.server;
							this.server = null;
							server.stop();
						}
					}
				} else {
					throw new BrokerConfigurationException("Please specifiy port.");
				}

				break;
			case DisableRESTInterface:
				if (server != null) {
					server.stop();
					server = null;
				}
				break;
			}
		} catch (CommandNotSupportedException e) {
			illegalInput(e.getMessage() + " Type help for getting a list of supported commands.");
		} catch (IllegalBrokerOperationException | ForwarderConfigurationException | BrokerConfigurationException e) {
			illegalInput(e.getMessage());
		}
	}

	private BrokerConfig extractAndValidateBrokerProperties(String in) throws BrokerConfigurationException {
		Hashtable<String, String> properties = extractProperties(Command.CreateBroker, in);
		return new BrokerConfig(properties);
	}

	private ForwarderConfig extractAndValidateForwarderProperties(String in) throws ForwarderConfigurationException {
		Hashtable<String, String> properties = extractProperties(Command.AddForwarder, in);
		return new ForwarderConfig(properties);
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
			String param = paramAndValue.substring(paramAndValue.startsWith("-") ? 1 : 0, pos).trim();
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

	/**
	 * Workaround: Register this broker in servlet context!
	 * 
	 * @return {@link BrokerEngine}
	 */
	public static BrokerEngine getInstance() {
		return instance;
	}

	/**
	 * Returns the broker.
	 * 
	 * @return {@link Broker}
	 */
	public Broker getBroker() {
		return broker;
	}

	/**
	 * Stops and removes the broker.
	 */
	public void shutdownAndRemoveBroker() {
		if (broker != null) {
			broker.shutdown();
		}
		broker = null;
	}

	/**
	 * Creates a broker and returns the instance. The broker is not started
	 * after this method invocation.
	 * 
	 * @param config
	 *            {@link BrokerConfig}
	 * @return {@link Broker}
	 * @throws BrokerConfigurationException
	 *             If the configuration is not valid.
	 */
	public Broker setupAndGetBroker(BrokerConfig config) throws BrokerConfigurationException {
		Broker newBroker = null;
		try {
			newBroker = new BrokerImpl();
			newBroker.configure(config);
			broker = newBroker;
			currentCommandState = Command.Start;
			try {
				Thread.sleep(1500);
			} catch (InterruptedException e) {
				LOG.error("", e);
			}
		} catch (BrokerConfigurationException e) {
			newBroker.shutdown();
			throw e;
		}
		return broker;
	}
}
