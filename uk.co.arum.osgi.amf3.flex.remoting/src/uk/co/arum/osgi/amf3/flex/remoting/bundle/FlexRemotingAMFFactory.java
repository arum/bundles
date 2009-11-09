/*
  uk.co.arum.osgi.amf3.flex.remoting 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3.flex.remoting bundle.

  uk.co.arum.osgi.amf3.flex.remoting is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3.flex.remoting is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3.flex.remoting.bundle;

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.log.LogService;

import uk.co.arum.osgi.amf3.AMFFactory;
import uk.co.arum.osgi.amf3.flex.remoting.OSGiAMFConstants;
import uk.co.arum.osgi.amf3.flex.remoting.RemotingContext;
import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;
import uk.co.arum.osgi.glue.Activatable;
import uk.co.arum.osgi.glue.Contextual;
import uk.co.arum.osgi.glue.GlueableService;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;

public class FlexRemotingAMFFactory implements GlueableService, Activatable,
		Contextual, AMFFactory, ManagedService {

	private static final String AMF_SERVICE_PROPERTY_NAME = "amf.service.property.name";

	private static final String NIL_DSID = "nil";

	private static final String DSENDPOINT = "DSEndpoint";

	private static final String DSID = "DSId";

	private OSGiAMFConfig config;

	private MessagingManager messagingManager;

	private LogService logService;

	private BundleContext context;

	private AMFServicesTracker amfServicesTracker;

	private boolean active;

	public FlexRemotingAMFFactory() {
	}

	public void bind(MessagingManager mgr) {
		this.messagingManager = mgr;
	}

	public void bind(LogService logService) {
		this.logService = logService;
	}

	public String getDescription() {
		return "Flex Remoting Support for AMF for OSGi";
	}

	public Class<?> getObjectClass(Object o) {

		if (o instanceof ErrorMessage) {
			return ErrorMessage.class;
		}

		if (o instanceof AcknowledgeMessage) {
			return AcknowledgeMessage.class;
		} else if (o instanceof CommandMessage) {
			return CommandMessage.class;
		} else if (o instanceof RemotingMessage) {
			return RemotingMessage.class;
		}

		if (o instanceof AsyncMessage) {
			return AsyncMessage.class;
		}

		return null;
	}

	public String getResponseTarget(Object response) {
		if (response instanceof ErrorMessage) {
			return "onStatus";
		}

		return "onResult";
	}

	public boolean isExternaliser(String name) {
		return false;
	}

	public Class<?> loadClass(String name) {

		Class<?> clazz = config.loadClass(name);
		if (null == clazz) {
			try {
				clazz = Class.forName(name);
			} catch (ClassNotFoundException e) {
				return null;
			}
		}

		return clazz;
	}

	public Object newInstance(String name) {
		Class<?> c = loadClass(name);

		if (null != c) {
			try {
				return c.newInstance();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}

		return null;
	}

	public Object process(Object o) throws Exception {
		if (o instanceof List<?>) {
			List<?> list = (List<?>) o;
			if (list.size() > 0) {
				o = list.get(0);
			}
		}

		Object response = null;
		if (o instanceof CommandMessage) {
			response = processCommandMessage((CommandMessage) o);
		} else if (o instanceof RemotingMessage) {
			RemotingMessage message = (RemotingMessage) o;
			Object result = processRemotingMessage(message);
			Message responseMessage = new AcknowledgeMessage(message);
			responseMessage.setBody(result);
			response = responseMessage;
		} else {
			response = processPublishMessage(o);
		}

		return response;
	}

	public Object handleException(Exception ex) {
		return new ErrorMessage(ex);
	}

	public Object readExternal(String name, ObjectInput deserializer) {
		return null;
	}

	public boolean writeExternal(Object o, ObjectOutput serialiser) {
		return false;
	}

	public void activate() throws Exception {
		logService.log(LogService.LOG_INFO,
				"FlexRemotingAMFFactory - ACTIVATED");
		active = true;
		updated(null);
	}

	public void deactivate() throws Exception {
		logService.log(LogService.LOG_INFO,
				"FlexRemotingAMFFactory - DEACTIVATING");
		active = false;
		cleanup();
	}

	private void cleanup() {
		if (null != amfServicesTracker) {
			amfServicesTracker.close();
			amfServicesTracker = null;
		}

		if (null != config) {
			config.dispose();
			config = null;
		}
	}

	@SuppressWarnings("unchecked")
	public void updated(Dictionary properties) throws ConfigurationException {
		logService.log(LogService.LOG_INFO, "FlexRemotingAMFFactory - updated("
				+ properties + ")");

		cleanup();

		if (active) {
			if (null == properties) {
				properties = createDefaultConfig();
			}

			String propName = (String) properties
					.get(AMF_SERVICE_PROPERTY_NAME);

			config = new OSGiAMFConfig(context, propName);
			try {
				logService.log(LogService.LOG_INFO,
						"FlexRemotingAMFFactory - starting AMF tracker ["
								+ propName + "]");
				amfServicesTracker = new AMFServicesTracker(context,
						logService, config, propName);
			} catch (InvalidSyntaxException e) {

			}
			amfServicesTracker.open();
		}
	}

	@SuppressWarnings("unchecked")
	private Dictionary createDefaultConfig() {
		Dictionary config = new Hashtable();

		config
				.put(AMF_SERVICE_PROPERTY_NAME,
						OSGiAMFConstants.AMF_SERVICE_NAME);

		return config;
	}

	public void bindContext(BundleContext context) {
		this.context = context;
	}

	public void unbindContext(BundleContext context) {
		this.context = null;
	}

	public Dictionary<?, ?> getProperties(String serviceName) {

		if (serviceName.equals(ManagedService.class.getName())) {
			Dictionary<String, String> properties = new Hashtable<String, String>();
			properties.put(Constants.SERVICE_PID, getClass().getName());
			return properties;
		}

		return null;
	}

	public String getServiceFilter(String serviceName, String name) {
		return null;
	}

	public String[] getServiceNames() {
		return new String[] { AMFFactory.class.getName(),
				ManagedService.class.getName() };
	}

	private Object processCommandMessage(CommandMessage command) {

		AcknowledgeMessage ack = new AcknowledgeMessage(command);
		ack.setBody(System.currentTimeMillis());

		switch (command.getOperation()) {

		case CommandMessage.CLIENT_PING_OPERATION:
			// have to fix the DSId now because we need it for the body
			fixDSId(ack);
			ack.setBody(ack.getHeaders());
			break;

		case CommandMessage.SUBSCRIBE_OPERATION:

			// this client should now receive these messages...
			messagingManager.subscribe((String) command.getHeader(DSID),
					(String) command.getHeader(DSENDPOINT));

			if (null == command.getClientId()) {
				command.setClientId((String) command.getHeader(DSID));
			}

			ack.setClientId((String) command.getHeader(DSID));
			break;

		case CommandMessage.UNSUBSCRIBE_OPERATION:

			// this client should no longer receive these messages
			messagingManager.unsubscribe((String) command.getHeader(DSID),
					(String) command.getHeader(DSENDPOINT));
			break;

		case CommandMessage.POLL_OPERATION:

			// TODO handle subscription timeout
			// deliver any waiting messages using the DSId from the header
			Object[] messages = messagingManager.getMessages((String) command
					.getHeader(DSID));
			if (null != messages && messages.length > 0) {
				CommandMessage message = new CommandMessage();
				message
						.setMessageId(UUID.randomUUID().toString()
								.toUpperCase());
				message.setOperation(CommandMessage.CLIENT_SYNC_OPERATION);
				message.setBody(messages);
				message.setTimestamp(System.currentTimeMillis());
				return message;
			}

			break;

		default:
			break;
		}

		// fix the DSId if it needs it.
		fixDSId(ack);
		return ack;
	}

	private void fixDSId(AcknowledgeMessage ack) {
		if (null == ack.getHeader(DSID) || NIL_DSID.equals(ack.getHeader(DSID))) {
			ack.setHeader(DSID, UUID.randomUUID().toString().toUpperCase());
		}
	}

	private String argToString(Object arg) {

		String s = "[" + arg + "]/";

		if (arg == null) {
			s = s + "[null]";
		} else if (arg.getClass().isArray()) {
			s = s + "{";
			for (int i = 0; i < Array.getLength(arg); i++) {
				s = s + argToString(Array.get(arg, i));
			}
			s = s + "}";
		} else {
			s = s + "[" + arg.getClass().getName() + "]";
		}

		return s;
	}

	private Object convertToArray(Object arg) {
		Set<Class<?>> types = new HashSet<Class<?>>();
		Object[] array = (Object[]) arg;

		for (int i = 0; i < array.length; i++) {
			if (array != null) {
				types.add(array[i].getClass());
			}
		}

		if (types.size() == 1) {
			Object newArray = Array.newInstance(types.iterator().next(),
					array.length);

			for (int i = 0; i < array.length; i++) {
				Array.set(newArray, i, array[i]);
			}

			return newArray;
		}

		return arg;
	}

	private Object processRemotingMessage(RemotingMessage remoting)
			throws Exception {

		OSGiServiceConfig serviceConfig = config.getServiceConfig(remoting
				.getDestination());

		if (null == serviceConfig) {
			throw new RuntimeException(
					"No service configuration for destination "
							+ remoting.getDestination());
		}

		// build the operation arguments
		System.out.println("Method: " + remoting.getDestination() + "#"
				+ remoting.getOperation() + " : ");
		int index = 0;
		Object[] opargs = (Object[]) remoting.getBody();
		if (null != opargs) {
			for (index = 0; index < opargs.length; index++) {
				Object o = opargs[index];
				System.out.println(index + "\t" + argToString(o));

				if (null != o && o.getClass().isArray()) {
					// convert to a proper array
					opargs[index] = convertToArray(o);
				}

			}
		}
		System.out.println(index + " args");

		Class<?>[] opargsClasses = new Class<?>[(null == opargs ? 0
				: opargs.length)];

		if (null != opargs) {
			int i = 0;
			for (Object arg : opargs) {
				if (null != arg) {
					opargsClasses[i] = arg.getClass();
				}
				i++;
			}
		}

		Method method = null;
		for (String type : serviceConfig.getServiceTypes()) {

			try {
				Class<?> serviceClass = serviceConfig.getProvider().loadClass(
						type);

				// is there an operation on this class that matches requested
				// operation?
				if (null != serviceClass) {
					method = findMethod(serviceClass, remoting.getOperation(),
							opargsClasses, opargs);
				}

			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}

		}

		// if no operation has been found, throw an exception
		if (null == method) {
			throw new RuntimeException(new NoSuchMethodException(remoting
					.getOperation()));
		}

		// if the same operation exists on the actual service class, invoke it
		if (null == (method = findMethod(serviceConfig.getService().getClass(),
				remoting.getOperation(), opargsClasses, opargs))) {
			throw new RuntimeException(new NoSuchMethodException(remoting
					.getOperation()));
		}

		new RemotingContext(remoting);
		try {
			Object returnValue = method.invoke(serviceConfig.getService(),
					opargs);
			return config.translate(returnValue);
		} catch (InvocationTargetException e) {
			if (e.getCause() instanceof Exception) {
				throw (Exception) e.getCause();
			}
			throw e;
		}
	}

	@SuppressWarnings("unchecked")
	private boolean compareArgList(Class[] methodArgs, Class[] requiredArgs,
			Object[] args) {

		if (methodArgs.length != requiredArgs.length) {
			return false;
		}

		for (int i = 0; i < methodArgs.length; i++) {

			if (null == requiredArgs[i]
					|| methodArgs[i].isAssignableFrom(requiredArgs[i])
					|| isAssignable(methodArgs[i], requiredArgs[i], args[i])) {
				continue;
			}

			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private boolean isAssignable(Class methodArgType, Class requiredArgType,
			Object arg) {

		try {
			requiredArgType.cast(arg);
		} catch (ClassCastException e) {
			return false;
		}

		return true;
	}

	@SuppressWarnings("unchecked")
	private Method findMethod(Class c, String name, Class[] argTypes,
			Object[] args) {
		Method method = null;
		try {
			method = c.getDeclaredMethod(name, argTypes);
		} catch (NoSuchMethodException e) {
			// could happen
			for (Method xMethod : c.getDeclaredMethods()) {
				if (xMethod.getName().equals(name)) {
					if (compareArgList(xMethod.getParameterTypes(), argTypes,
							args)) {
						method = xMethod;
					}
				}
			}

		}

		return method;
	}

	private Object processPublishMessage(Object incoming) {

		if (incoming instanceof Message) {
			Message incomingMessage = (Message) incoming;
			if (null != incomingMessage.getHeader(DSENDPOINT)
					&& null != incomingMessage.getDestination()) {
				messagingManager.dispatch(new PublishedObjectEvent(
						(String) incomingMessage.getHeader(DSID),
						(String) incomingMessage.getHeader(DSENDPOINT),
						incomingMessage.getMessageId(), incomingMessage
								.getBody()));
				return new AcknowledgeMessage(incomingMessage);
			}
		}

		return null;
	}

}
