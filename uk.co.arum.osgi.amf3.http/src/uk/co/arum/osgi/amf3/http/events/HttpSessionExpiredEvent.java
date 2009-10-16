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

package uk.co.arum.osgi.amf3.http.events;

import java.util.Dictionary;

import org.osgi.service.event.Event;

import uk.co.arum.osgi.amf3.http.HttpRequestContext;

public class HttpSessionExpiredEvent extends Event {

	public static final String TOPIC = HttpSessionExpiredEvent.class.getName()
			.replace('.', '/');

	private final HttpRequestContext context;

	/**
	 * The request context that created this session.
	 * 
	 * @param context
	 *            the request context
	 */
	public HttpSessionExpiredEvent(HttpRequestContext context) {
		super(TOPIC, (Dictionary) null);
		this.context = context;
	}

	public HttpRequestContext getContext() {
		return context;
	}

}
