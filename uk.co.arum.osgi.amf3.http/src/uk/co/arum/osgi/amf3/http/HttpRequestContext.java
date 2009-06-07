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

package uk.co.arum.osgi.amf3.http;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class HttpRequestContext {

	private static ThreadLocal<HttpRequestContext> threadedRequest = new ThreadLocal<HttpRequestContext>();

	public static HttpRequestContext getCurrent() {
		return threadedRequest.get();
	}

	private final HttpServletRequest request;

	private final HttpServletResponse response;

	private final Map<String, Object> properties;

	public HttpRequestContext(HttpServletRequest request,
			HttpServletResponse response) {
		this.request = request;
		this.response = response;
		this.properties = new HashMap<String, Object>();
		threadedRequest.set(this);
	}

	public HttpServletRequest getRequest() {
		return request;
	}

	public HttpServletResponse getResponse() {
		return response;
	}

	/**
	 * Can be used by other bundles to set properties for this request.
	 * 
	 * @return properties specific to this context
	 */
	public Map<String, Object> getProperties() {
		return properties;
	}

}
