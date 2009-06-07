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

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Subscription {

	private final String channel;

	private final String topic;

	private final Set<String> ids = Collections
			.synchronizedSet(new HashSet<String>());

	private final MessagingManager messagingManager;

	public Subscription(String channel, String topic,
			MessagingManager messagingManager) {
		super();
		this.channel = channel;
		this.topic = topic;
		this.messagingManager = messagingManager;
	}

	public String getChannel() {
		return channel;
	}

	public String getTopic() {
		return topic;
	}

	public void addSubscription(String id) {
		ids.add(id);
	}

	/**
	 * Remove a subscription id and return the number of subscription ids still
	 * registered.
	 * 
	 * @param id
	 *            the subscription id to remove
	 * @return the number of subscription ids still registered
	 */
	public int removeSubscription(String id) {
		ids.remove(id);
		return ids.size();
	}

	public void addMessage(String sourceID, Object o) {

		for (String id : ids) {
			if (id.equals(sourceID)) {
				continue;
			}
			messagingManager.sendMessage(id, channel, o);
		}

	}

	public boolean isSubscribed(String subscriptionID) {
		return ids.contains(subscriptionID);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((channel == null) ? 0 : channel.hashCode());
		result = prime * result + ((topic == null) ? 0 : topic.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final Subscription other = (Subscription) obj;
		if (channel == null) {
			if (other.channel != null)
				return false;
		} else if (!channel.equals(other.channel))
			return false;
		if (topic == null) {
			if (other.topic != null)
				return false;
		} else if (!topic.equals(other.topic))
			return false;
		return true;
	}

}
