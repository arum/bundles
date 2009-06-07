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

/**
 * @author Franck WOLFF
 */
public class AsyncMessage extends AbstractMessage {

	private static final long serialVersionUID = 1L;

	public static final String SUBTOPIC_HEADER = "DSSubtopic";
	public static final String DESTINATION_CLIENT_ID_HEADER = "DSDstClientId";

	private String correlationId;

	public AsyncMessage() {
		super();

		setHeaders(new HashMap<String, Object>());
	}

	public AsyncMessage(Message request) {
		super(request);

		setHeaders(new HashMap<String, Object>());
		this.correlationId = request.getMessageId();
	}

	public String getCorrelationId() {
		return correlationId;
	}

	public void setCorrelationId(String correlationId) {
		this.correlationId = correlationId;
	}

	@Override
	public Message clone() {
		AsyncMessage msg = new AsyncMessage();
		msg.setBody(getBody());
		msg.setClientId(getClientId());
		msg.setCorrelationId(getCorrelationId());
		msg.setDestination(getDestination());
		msg.setMessageId(getMessageId());
		msg.setHeaders(new HashMap<String, Object>(getHeaders()));
		msg.setTimestamp(getTimestamp());
		msg.setTimeToLive(getTimeToLive());
		return msg;
	}

}
