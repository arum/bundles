/*
  uk.co.arum.osgi.amf3.http 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3.http bundle.

  uk.co.arum.osgi.amf3.http is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3.http is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3.http.bundle;

import java.util.HashSet;
import java.util.Set;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;
import org.osgi.util.tracker.ServiceTracker;
import org.osgi.util.tracker.ServiceTrackerCustomizer;

import uk.co.arum.osgi.amf3.AMFFactory;

public class Activator implements BundleActivator {

	private static final String ALIAS_OSGIAMF = "/amf3osgi";

	private AMFServlet servlet;
	private ServiceTracker httpServiceTracker;
	private ServiceTracker logServiceTracker;
	private ServiceTracker eventAdminServiceTracker;
	private ServiceTracker amfFactoryServiceTracker;
	private CompoundAMFFactory factory;

	public void start(BundleContext context) throws Exception {

		factory = new CompoundAMFFactory();

		servlet = new AMFServlet(factory);

		logServiceTracker = new LogServiceTracker(context, LogService.class
				.getName(), null);
		logServiceTracker.open();

		eventAdminServiceTracker = new EventAdminServiceTracker(context,
				EventAdmin.class.getName(), null);
		eventAdminServiceTracker.open();

		httpServiceTracker = new HttpServiceTracker(context, HttpService.class
				.getName(), null);
		httpServiceTracker.open();

		amfFactoryServiceTracker = new AMFFactoryServiceTracker(context,
				AMFFactory.class.getName(), null);
		amfFactoryServiceTracker.open();

	}

	public void stop(BundleContext context) throws Exception {

		if (null != httpServiceTracker) {
			httpServiceTracker.close();
			httpServiceTracker = null;
		}

		if (null != amfFactoryServiceTracker) {
			amfFactoryServiceTracker.close();
			amfFactoryServiceTracker = null;
		}

		if (null != eventAdminServiceTracker) {
			eventAdminServiceTracker.close();
			eventAdminServiceTracker = null;
		}

	}

	private final class AMFFactoryServiceTracker extends ServiceTracker {
		private AMFFactoryServiceTracker(BundleContext context, String clazz,
				ServiceTrackerCustomizer customizer) {
			super(context, clazz, customizer);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			AMFFactory factoryService = (AMFFactory) context
					.getService(reference);
			factory.add(factoryService);
			return factoryService;
		}

		@Override
		public void removedService(ServiceReference reference,
				Object serviceObject) {
			AMFFactory factoryService = (AMFFactory) serviceObject;
			factory.remove(factoryService);
			context.ungetService(reference);
		}
	}

	private final class HttpServiceTracker extends ServiceTracker {
		private HttpServiceTracker(BundleContext context, String clazz,
				ServiceTrackerCustomizer customizer) {
			super(context, clazz, customizer);
		}

		@Override
		public Object addingService(ServiceReference reference) {
			HttpService service = (HttpService) context.getService(reference);

			try {
				service.registerServlet(ALIAS_OSGIAMF, servlet, null, null);
			} catch (Exception e) {
				context.ungetService(reference);
				throw new RuntimeException(e);
			}

			return service;
		}

		@Override
		public void removedService(ServiceReference reference,
				Object serviceObject) {
			HttpService service = (HttpService) serviceObject;
			service.unregister(ALIAS_OSGIAMF);
			context.ungetService(reference);
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
			servlet.setEventAdmin(service);
			eventAdminSet.add(service);
			return service;
		}

		@Override
		public void removedService(ServiceReference reference,
				Object serviceObject) {
			eventAdminSet.remove(serviceObject);
			if (eventAdminSet.size() > 0) {
				servlet.setEventAdmin(eventAdminSet.iterator().next());
			} else {
				servlet.setEventAdmin(null);
			}
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
			servlet.setLogService(service);
			logSet.add(service);
			return service;
		}

		@Override
		public void removedService(ServiceReference reference,
				Object serviceObject) {
			logSet.remove(serviceObject);
			if (logSet.size() > 0) {
				servlet.setLogService(logSet.iterator().next());
			} else {
				servlet.setLogService(null);
			}
		}
	}
}
