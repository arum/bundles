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
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.osgi.service.log.LogService;

import uk.co.arum.osgi.amf3.AMFFactory;
import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;
import flex.messaging.messages.AcknowledgeMessage;
import flex.messaging.messages.AsyncMessage;
import flex.messaging.messages.CommandMessage;
import flex.messaging.messages.ErrorMessage;
import flex.messaging.messages.Message;
import flex.messaging.messages.RemotingMessage;

public class FlexRemotingAMFFactory implements AMFFactory {

	private static final String NIL_DSID = "nil";

	private static final String DSENDPOINT = "DSEndpoint";

	private static final String DSID = "DSId";

	private final OSGiAMFConfig config;

	private final MessagingManager messagingManager;

	private LogService logService;

	public FlexRemotingAMFFactory(OSGiAMFConfig config,
			MessagingManager messagingManager) {
		this.config = config;
		this.messagingManager = messagingManager;
	}

	public void setLogService(LogService logService) {
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

	public Object process(Object o) {

		try {
			return doProcess(o);
		} catch (RuntimeException e) {
			Throwable t = e;

			if (t.getCause() instanceof InvocationTargetException
					&& null != t.getCause().getCause()) {
				t = t.getCause().getCause();
			}

			logService
					.log(LogService.LOG_WARNING, "An exception was thrown", t);

			if (o instanceof Message) {
				return new ErrorMessage((Message) o, t);
			} else {
				return new ErrorMessage(t);
			}
		}

	}

	private Object doProcess(Object o) {
		if (o instanceof ArrayList) {
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

	private Object processRemotingMessage(RemotingMessage remoting) {

		OSGiServiceConfig serviceConfig = config.getServiceConfig(remoting
				.getDestination());

		if (null == serviceConfig) {
			throw new RuntimeException(
					"No service configuration for destination "
							+ remoting.getDestination());
		}

		// build the operation arguments
		Object[] opargs = (Object[]) remoting.getBody();
		Class<?>[] opargsClasses = new Class<?>[(null == opargs ? 0
				: opargs.length)];

		if (null != opargs) {
			int i = 0;
			for (Object arg : opargs) {
				opargsClasses[i++] = arg.getClass();
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
					try {
						method = serviceClass.getMethod(
								remoting.getOperation(), opargsClasses);
					} catch (NoSuchMethodException e) {
						// could happen
					}
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

		// TODO add security hooks in case a bundle wants to prevent access to
		// this service for some reason

		// if the same operation exists on the actual service class, invoke it
		try {
			method = serviceConfig.getService().getClass().getMethod(
					remoting.getOperation(), opargsClasses);
		} catch (NoSuchMethodException e) {
			return e;
		}

		try {
			return method.invoke(serviceConfig.getService(),
					(Object[]) remoting.getBody());
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
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

	public Object handleException(Exception ex) {
		return new ErrorMessage(ex);
	}

	public Object readExternal(String name, ObjectInput deserializer) {
		return null;
	}

	public boolean writeExternal(Object o, ObjectOutput serialiser) {
		return false;
	}

}
