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

import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import uk.co.arum.osgi.amf3.flex.remoting.events.PublishedObjectEvent;
import uk.co.arum.osgi.glue.Activatable;
import uk.co.arum.osgi.glue.GlueableService;
import flex.messaging.messages.AsyncMessage;

@SuppressWarnings("unchecked")
public class MessagingManager implements Activatable, GlueableService,
		EventHandler {

	// TODO make this configurable? if so, how?
	public static final long SUBSCRIPTION_TIMEOUT = 1000 * 30;

	private final Map<String, Long> accessTimes = new HashMap<String, Long>();

	private EventAdmin eventAdmin;

	private final Set<Subscription> subscriptions = Collections
			.synchronizedSet(new HashSet<Subscription>());

	private Timer timer;

	private final Map<String, List> waitingMessages = new HashMap<String, List>();

	public MessagingManager() {
	}

	public void activate() throws Exception {
		timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				cleanupSubscriptions();
			}
		}, SUBSCRIPTION_TIMEOUT);
	}

	public void bind(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void deactivate() throws Exception {
		timer.cancel();
		timer = null;
	}

	public void dispatch(PublishedObjectEvent event) {

		// find a list of subscriptions for the given parameters
		for (Subscription sub : find(event.getChannelID())) {
			sub.addMessage(event.getSourceID(), event.getObject());
		}

		// if the event was generated externally dispatch an osgi event
		if (null != event.getSourceID()) {
			eventAdmin.postEvent(event);
		}
	}

	public Object[] getMessages(String subscriptionID) {

		Long lastAccess = accessTimes.get(subscriptionID);
		if (null == lastAccess) {
			return null;
		}

		long now = System.currentTimeMillis();
		long diff = now - lastAccess;

		if (diff > SUBSCRIPTION_TIMEOUT) {
			accessTimes.remove(subscriptionID);
			unsubscribe(subscriptionID, null);
			return null;
		} else {
			accessTimes.put(subscriptionID, now);
		}

		List<?> messages = waitingMessages.get(subscriptionID);

		if (messages.size() > 0) {
			waitingMessages.put(subscriptionID, new LinkedList());
			return messages.toArray();
		}

		return null;
	}

	public Dictionary getProperties(String serviceName) {

		if (serviceName.equals(EventHandler.class.getName())) {
			String[] topics = new String[] { PublishedObjectEvent.TOPIC };
			Hashtable ht = new Hashtable();
			ht.put(EventConstants.EVENT_TOPIC, topics);
			return ht;
		}

		return null;
	}

	public String getServiceFilter(String serviceName, String name) {
		return null;
	}

	public String[] getServiceNames() {
		return new String[] { EventHandler.class.getName(),
				MessagingManager.class.getName() };
	}

	public Collection<Subscription> getSubscriptions(String subscriptionID) {
		Set<Subscription> subs = new HashSet<Subscription>();

		for (Subscription sub : subscriptions) {
			if (sub.isSubscribed(subscriptionID)) {
				subs.add(sub);
			}
		}

		return subs;
	}

	public void handleEvent(Event event) {
		if (event instanceof PublishedObjectEvent) {
			PublishedObjectEvent poevent = (PublishedObjectEvent) event;

			// if the event was generated internally dispatch the event to
			// connected clients
			if (null == poevent.getSourceID()) {
				dispatch(poevent);
			}
		}
	}

	public void sendMessage(String subscriptionID, String channelID,
			Object message) {
		List messages = waitingMessages.get(subscriptionID);

		// only send if subscribed. this subscription could have timed out
		// or have been unsubscribed in the time it took to call this.
		if (null != messages) {
			AsyncMessage async = new AsyncMessage();
			// async.setClientId(UUID.randomUUID().toString().toUpperCase());
			async.setClientId(subscriptionID);
			async.setHeader("DSId", subscriptionID);
			async.setMessageId(UUID.randomUUID().toString().toUpperCase());
			async.setDestination(channelID);
			async.setBody(message);
			async.setTimestamp(System.currentTimeMillis());
			messages.add(async);
		}
	}

	public void subscribe(String subscriptionID, String channel) {

		subscriptions.add(new Subscription(channel, null, this));
		addSubscriber(subscriptionID, channel, null);

		if (null == waitingMessages.get(subscriptionID)) {
			waitingMessages.put(subscriptionID, new LinkedList());
		}

		if (null == accessTimes.get(subscriptionID)) {
			accessTimes.put(subscriptionID, System.currentTimeMillis());
		}

	}

	public void unsubscribe(String subscriptionID, String channel) {

		Set<Subscription> expiredSubscriptions = new HashSet<Subscription>();
		for (Subscription sub : getSubscriptions(subscriptionID)) {
			if (sub.getChannel().equals(channel)) {
				if (sub.removeSubscription(subscriptionID) <= 0) {
					expiredSubscriptions.add(sub);
				}
			}
		}

		subscriptions.removeAll(expiredSubscriptions);

	}

	private void addSubscriber(String subscriptionID, String channel,
			String topic) {
		Collection<Subscription> subs = find(channel);
		for (Subscription sub : subs) {
			sub.addSubscription(subscriptionID);
		}
	}

	private void cleanupSubscriptions() {

		long now = System.currentTimeMillis();
		Set<String> ids = new HashSet<String>(accessTimes.keySet());
		for (String id : ids) {
			Long lastAccess = accessTimes.get(id);
			long diff = now - lastAccess;
			if (diff > SUBSCRIPTION_TIMEOUT) {
				unsubscribe(id, null);
				accessTimes.remove(id);
				waitingMessages.remove(id);
			}
		}

	}

	private Collection<Subscription> find(String channelID) {
		Set<Subscription> subs = new HashSet<Subscription>();

		for (Subscription sub : subscriptions) {
			if (channelID.equals(sub.getChannel())) {
				subs.add(sub);
			}
		}

		return subs;
	}

}
