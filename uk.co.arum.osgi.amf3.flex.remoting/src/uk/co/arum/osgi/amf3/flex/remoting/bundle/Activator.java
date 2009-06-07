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

import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import uk.co.arum.osgi.amf3.AMFFactory;
import uk.co.arum.osgi.amf3.flex.remoting.OSGiAMFConstants;
import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;

public class Activator implements BundleActivator {

	private MessagingManager messagingManager;

	private FlexRemotingAMFFactory remotingFactory;

	private OSGiAMFConfig config;

	private ServiceTracker amfServicesTracker;

	private ServiceTracker eventAdminServiceTracker;

	private ServiceTracker logServiceTracker;

	public void start(BundleContext context) throws Exception {
		config = new OSGiAMFConfig(context);

		messagingManager = new MessagingManager();

		remotingFactory = new FlexRemotingAMFFactory(config,
				messagingManager);
		
		eventAdminServiceTracker = new EventAdminServiceTracker(context,
				EventAdmin.class.getName(), null);
		eventAdminServiceTracker.open();

		logServiceTracker = new LogServiceTracker(context, LogService.class
				.getName(), null);
		logServiceTracker.open();

		registerPublishedObjectEventHandler(context);

		context.registerService(AMFFactory.class.getName(),
				remotingFactory, null);

		// start listening for remote objects that have a destination id

		amfServicesTracker = new AMFServicesTracker(context);
		amfServicesTracker.open();

	}

	@SuppressWarnings("unchecked")
	private void registerPublishedObjectEventHandler(BundleContext context) {
		String[] topics = new String[] { PublishedObjectEvent.TOPIC };
		Hashtable ht = new Hashtable();
		ht.put(EventConstants.EVENT_TOPIC, topics);
		context.registerService(EventHandler.class.getName(), messagingManager,
				ht);
	}

	public void stop(BundleContext context) throws Exception {

		if (amfServicesTracker != null) {
			amfServicesTracker.close();
			amfServicesTracker = null;
		}

		if (eventAdminServiceTracker != null) {
			eventAdminServiceTracker.close();
			eventAdminServiceTracker = null;
		}

		if (null != config) {
			config.dispose();
		}

	}

	private final class EventAdminServiceTracker extends ServiceTracker {
		private Set<EventAdmin> eventAdminSet = new HashSet<EventAdmin>();

		private EventAdminServiceTracker(BundleContext context, String clazz,
				ServiceTrackerCustomizer customizer) {
			super(context, clazz, customizer);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			EventAdmin service = (EventAdmin) context.getService(reference);
			eventAdminSet.add(service);
			messagingManager.setEventAdmin(service);
			return super.addingService(reference);
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			eventAdminSet.remove(service);
			if (eventAdminSet.size() > 0) {
				messagingManager.setEventAdmin(eventAdminSet.iterator().next());
			} else {
				messagingManager.setEventAdmin(null);
			}
		}
	}

	private final class AMFServicesTracker extends ServiceTracker {
		private AMFServicesTracker(BundleContext context)
				throws InvalidSyntaxException {
			super(context, context.createFilter("("
					+ OSGiAMFConstants.AMF_SERVICE_NAME + "=*)"), null);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			config.addOSGiService(reference);
			return super.addingService(reference);
		}

		@Override
		public void removedService(ServiceReference reference, Object service) {
			config.removeOSGiService(reference);
			super.removedService(reference, service);
		}
	}

	private final class LogServiceTracker extends ServiceTracker {
		private Set<LogService> logSet = new HashSet<LogService>();

		private LogServiceTracker(BundleContext context, String clazz,
				ServiceTrackerCustomizer customizer) {
			super(context, clazz, customizer);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			LogService service = (LogService) context.getService(reference);
			remotingFactory.setLogService(service);
			messagingManager.setLogService(service);
			logSet.add(service);
			return service;
		}

		@Override
		public void removedService(ServiceReference reference,
				Object serviceObject) {
			logSet.remove(serviceObject);
			if (logSet.size() > 0) {
				LogService service = logSet.iterator().next();
				messagingManager.setLogService(service);
				remotingFactory.setLogService(service);
			} else {
				messagingManager.setLogService(null);
				remotingFactory.setLogService(null);
			}
		}
	}

}
