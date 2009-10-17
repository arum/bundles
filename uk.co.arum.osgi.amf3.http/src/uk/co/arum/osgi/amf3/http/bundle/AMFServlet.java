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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;

import org.osgi.service.event.EventAdmin;
import org.osgi.service.http.HttpService;
import org.osgi.service.log.LogService;

import uk.co.arum.osgi.amf3.AMFFactory;
import uk.co.arum.osgi.amf3.http.AMFHttpConstants;
import uk.co.arum.osgi.amf3.http.HttpRequestContext;
import uk.co.arum.osgi.amf3.http.events.HttpSessionCreatedEvent;
import uk.co.arum.osgi.amf3.http.events.HttpSessionExpiredEvent;
import uk.co.arum.osgi.amf3.io.AMFProcessor;
import uk.co.arum.osgi.glue.Activatable;
import uk.co.arum.osgi.glue.Glueable;

public class AMFServlet extends HttpServlet implements Glueable, Activatable {

	private static final String ALIAS_OSGIAMF = "/amf3osgi";

	private static final long serialVersionUID = 1L;

	private CompoundAMFFactory compoundFactory;

	private AMFProcessor processor;

	private HttpService httpService;

	private EventAdmin eventAdmin;

	private LogService logService;

	public AMFServlet() {
	}

	public void bind(HttpService httpService) {
		this.httpService = httpService;
	}

	public void bind(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void bind(LogService logService) {
		this.logService = logService;
	}

	public void bind(AMFFactory[] factories) {
		compoundFactory = new CompoundAMFFactory();
		for (AMFFactory factory : factories) {
			compoundFactory.add(factory);
		}
	}

	public void unbind(AMFFactory[] factories) {
		for (AMFFactory factory : factories) {
			compoundFactory.remove(factory);
		}
		compoundFactory = null;
	}

	public void activate() throws Exception {
		processor = new AMFProcessor(compoundFactory);
		httpService.registerServlet(ALIAS_OSGIAMF, this, null, null);
	}

	public void deactivate() throws Exception {
		httpService.unregister(ALIAS_OSGIAMF);
		processor = null;
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		resp.sendError(HttpServletResponse.SC_METHOD_NOT_ALLOWED);
	}

	@Override
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		final HttpRequestContext context = new HttpRequestContext(request,
				response);
		HttpSession session = request.getSession();

		if (session.isNew()) {
			// set the interval - this should be configurable, but can be
			// changed by event handlers of the HttpSessionCreatedEvent
			session
					.setMaxInactiveInterval(AMFHttpConstants.SESSION_MAX_INTERVAL);

			// dispatch a new session event
			if (null != eventAdmin) {
				eventAdmin.postEvent(new HttpSessionCreatedEvent(context));
			} else {
				logService.log(LogService.LOG_WARNING,
						"HttpSessionCreatedEvent: No event admin service");
			}

			// add listener so that we can dispatch session expired events
			session.setAttribute(AMFHttpConstants.OSGI_SESSION_ATTRIBUTE_NAME,
					new HttpSessionBindingListener() {
						public void valueBound(HttpSessionBindingEvent event) {
							// no-op
						}

						public void valueUnbound(HttpSessionBindingEvent event) {
							if (null != eventAdmin) {
								eventAdmin
										.postEvent(new HttpSessionExpiredEvent(
												context));
							} else {
								logService
										.log(LogService.LOG_WARNING,
												"HttpSessionExpiredEvent: No event admin service");
							}
						}
					});
		}

		ByteArrayOutputStream outBytes = new ByteArrayOutputStream();

		processor.process(request.getInputStream(), outBytes);

		response.setContentType("application/x-amf");
		response.setContentLength(outBytes.size());
		response.getOutputStream().write(outBytes.toByteArray());
	}

	public String getServiceFilter(String serviceName, String name) {
		return null;
	}
}
