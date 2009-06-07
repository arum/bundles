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

package uk.co.arum.osgi.amf3.flex.remoting.events;

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.UUID;

import org.osgi.service.event.Event;

/**
 * A published object event. If source id has been set, it is assume this object
 * was published from a Flex client. Bundles are free to dispatch this event in
 * order to send objects to Flex clients subscribed to the specified channels
 * and topics, but should specify null for the source id.
 * <p/>
 * 
 * Likewise, bundles can listen for this event, filtering on the appropriate
 * properties if desired in order to handle messages from Flex clients.
 * </p>
 * 
 * @author Chris Brind
 * 
 */
public class PublishedObjectEvent extends Event {

	public static final String SOURCE_ID_PROPERTY = "source.id";

	public static final String OBJECT_CLASS_PROPERTY = "object.class";

	public static final String CHANNEL_PROPERTY = "channel.id";

	public static final String MESSAGE_ID_PROPERTY = "message.id";

	/**
	 * The osgi topic name, NOT the channel/topic used by the Flex client.
	 */
	public static final String TOPIC = PublishedObjectEvent.class.getName()
			.replace('.', '/');

	private final String sourceID;
	private final String channelID;
	private final String messageID;
	private final Object object;

	/**
	 * 
	 * @param sourceID
	 *            the source id of the event (or null if this was a server side
	 *            process)
	 * @param channelID
	 *            the channel id this was published to
	 * @param messageID
	 *            the id of the message
	 * @param object
	 *            the object that was published
	 */
	public PublishedObjectEvent(String sourceID, String channelID,
			String messageID, Object object) {
		super(TOPIC, toProperties(sourceID, channelID, messageID, object));
		this.sourceID = sourceID;
		this.channelID = channelID;

		// if this was null, an id will have been generated
		this.messageID = (String) getProperty(MESSAGE_ID_PROPERTY);

		this.object = object;
	}

	/**
	 * Convenience constructor which allows an object to be published to a
	 * channel.
	 * 
	 * @param channelID
	 *            the channel
	 * @param object
	 *            the object
	 */
	public PublishedObjectEvent(String channelID, Object object) {
		this(null, channelID, null, object);
	}

	public String getSourceID() {
		return sourceID;
	}

	public String getChannelID() {
		return channelID;
	}

	public String getMessageID() {
		return messageID;
	}

	public Object getObject() {
		return object;
	}

	@SuppressWarnings("unchecked")
	private static Dictionary toProperties(String sourceID, String channelID,
			String messageID, Object object) {

		Dictionary properties = new Hashtable();

		if (null != sourceID) {
			properties.put(SOURCE_ID_PROPERTY, sourceID);
		}

		properties.put(CHANNEL_PROPERTY, channelID);

		if (null == messageID) {
			messageID = UUID.randomUUID().toString();
		}
		properties.put(MESSAGE_ID_PROPERTY, messageID);

		properties.put(OBJECT_CLASS_PROPERTY, object.getClass());

		return properties;
	}

	/**
	 * A utility method for replying to the same channel with an object. Source
	 * is set to null to indicate the server as the source of this message.
	 * 
	 * @param o
	 *            the object to send in reply
	 * 
	 * @return an event appropriate for replying to this event
	 */
	public PublishedObjectEvent createReply(Object o) {
		return new PublishedObjectEvent(null, channelID, messageID, o);
	}
}
