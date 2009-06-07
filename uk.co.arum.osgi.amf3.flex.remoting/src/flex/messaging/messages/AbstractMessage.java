/*
  GRANITE DATA SERVICES
  Copyright (C) 2007-2008 ADEQUATE SYSTEMS SARL

  This file is part of Granite Data Services.

  Granite Data Services is free software; you can redistribute it and/or modify
  it under the terms of the GNU Lesser General Public License as published by
  the Free Software Foundation; either version 3 of the License, or (at your
  option) any later version.

  Granite Data Services is distributed in the hope that it will be useful, but
  WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License
  for more details.

  You should have received a copy of the GNU Lesser General Public License
  along with this library; if not, see <http://www.gnu.org/licenses/>.
 */

package flex.messaging.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @author Franck WOLFF
 */
public abstract class AbstractMessage implements Message {

	private static final long serialVersionUID = 1L;

	private Object body = null;
	private Object clientId = null;
	private String destination = null;
	private Map<String, Object> headers = null;
	private String messageId = null;
	private long timestamp = 0L;
	private long timeToLive = 0L;

	public AbstractMessage() {
		super();
	}

	public AbstractMessage(Message request) {
		super();
		this.messageId = UUID.randomUUID().toString().toUpperCase();
		this.timestamp = System.currentTimeMillis();
		this.clientId = UUID.randomUUID().toString().toUpperCase();
	}

	public Object getBody() {
		return body;
	}

	public void setBody(Object body) {
		this.body = body;
	}

	public Object getClientId() {
		return clientId;
	}

	public void setClientId(Object clientId) {
		this.clientId = clientId;
	}

	public String getDestination() {
		return destination;
	}

	public void setDestination(String destination) {
		this.destination = destination;
	}

	public Map<String, Object> getHeaders() {
		return headers;
	}

	public void setHeaders(Map<String, Object> headers) {
		this.headers = headers;
	}

	public Object getHeader(String name) {
		return (headers != null ? headers.get(name) : null);
	}

	public boolean headerExists(String name) {
		return (headers != null ? headers.containsKey(name) : false);
	}

	public void setHeader(String name, Object value) {
		if (headers == null)
			headers = new HashMap<String, Object>();
		headers.put(name, value);
	}

	public String getMessageId() {
		return messageId;
	}

	public void setMessageId(String messageId) {
		this.messageId = messageId;
	}

	public long getTimestamp() {
		return timestamp;
	}

	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}

	public long getTimeToLive() {
		return timeToLive;
	}

	public void setTimeToLive(long timeToLive) {
		this.timeToLive = timeToLive;
	}

}
