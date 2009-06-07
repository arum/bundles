/*
  uk.co.arum.osgi.amf3.sample 
  
  Copyright (C) 2008 - 2009 Arum Systems Ltd

  This file is part of the uk.co.arum.osgi.amf3.sample bundle.

  uk.co.arum.osgi.amf3.sample is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  uk.co.arum.osgi.amf3.sample is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package uk.co.arum.osgi.amf3.sample.bundle;

import java.util.Date;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.osgi.service.http.HttpService;

import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;
import uk.co.arum.osgi.amf3.sample.SimpleService;

public class Activator implements BundleActivator {

	@SuppressWarnings("unchecked")
	public void start(BundleContext context) throws Exception {

		ServiceReference ref = context.getServiceReference(HttpService.class
				.getName());

		if (null == ref) {
			throw new RuntimeException("Unable to find HttpService");
		}

		HttpService http = (HttpService) context.getService(ref);
		http.registerResources("/sample", "/flexbin", null);

		Dictionary properties = new Hashtable();
		properties.put("AMF_SERVICE_NAME", SimpleService.class.getName());
		context.registerService(SimpleService.class.getName(),
				new SimpleServiceImpl(), properties);

		// if an event admin is available, we'll handle events for demo purposes
		registerEventHandler(context);

	}

	public void stop(BundleContext context) throws Exception {
	}

	@SuppressWarnings("unchecked")
	private void registerEventHandler(BundleContext context) {
		try {

			PublishedObjectEventHandler handler = new PublishedObjectEventHandler(
					context);

			Hashtable ht = new Hashtable();
			ht.put(EventConstants.EVENT_TOPIC,
					new String[] { PublishedObjectEvent.TOPIC });

			context.registerService(EventHandler.class.getName(), handler, ht);

		} catch (Exception ex) {
			// ignore
		}
	}

	class PublishedObjectEventHandler implements EventHandler {

		private final BundleContext context;

		public PublishedObjectEventHandler(BundleContext context) {
			super();
			this.context = context;
		}

		public void handleEvent(Event event) {
			if (event instanceof PublishedObjectEvent) {
				PublishedObjectEvent poe = (PublishedObjectEvent) event;

				if (poe.getSourceID() == null) {
					return;
				}

				System.out.println(poe.getChannelID() + " : "
						+ poe.getMessageID() + " : " + poe.getSourceID()
						+ " : " + poe.getObject());

				ServiceReference ref = context
						.getServiceReference(EventAdmin.class.getName());
				if (null != ref) {
					EventAdmin admin = (EventAdmin) context.getService(ref);

					String replyText = "Reply to published object "
							+ new Date() + " : " + poe.getObject();

					Map<String, String> theReply = new HashMap<String, String>();
					theReply.put("Say", replyText);
					
					admin.postEvent(poe.createReply(theReply));
					context.ungetService(ref);
				}
			}
		}

	}

}
