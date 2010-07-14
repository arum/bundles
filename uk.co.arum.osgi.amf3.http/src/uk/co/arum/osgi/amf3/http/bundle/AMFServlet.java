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
import java.util.Map;

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

public class AMFServlet extends HttpServlet {

	private static final String AMF_SERVLET_ALIAS = "amf.servlet.alias";

	private static final long serialVersionUID = 1L;

	private CompoundAMFFactory compoundFactory = new CompoundAMFFactory();

	private AMFProcessor processor;

	private HttpService httpService;

	private EventAdmin eventAdmin;

	private LogService logService;

	private String alias;

	public AMFServlet() {
	}

	public void bindHttpService(HttpService httpService) {
		this.httpService = httpService;
	}

	public void unbindHttpService(HttpService httpService) {
		if (this.httpService == httpService) {
			this.httpService = null;
		}
	}

	public void bindEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	public void unbindEventAdmin(EventAdmin eventAdmin) {
		if (this.eventAdmin == eventAdmin) {
			this.eventAdmin = null;
		}
	}

	public void bindLogService(LogService logService) {
		this.logService = logService;
	}

	public void unbindLogService(LogService logService) {
		if (this.logService == logService) {
			this.logService = null;
		}
	}

	public void addAMFFactory(AMFFactory factory) {
		compoundFactory.add(factory);
	}

	public void removeAMFFactory(AMFFactory factory) {
		compoundFactory.remove(factory);
	}

	public void activate(Map<String, Object> properties) throws Exception {
		logService.log(LogService.LOG_INFO, "AMFServlet - ACTIVATED : "
				+ properties);
		alias = (String) properties.get(AMF_SERVLET_ALIAS);
		httpService.registerServlet(alias, this, null, null);
		processor = new AMFProcessor(compoundFactory);
	}

	public void deactivate() throws Exception {
		logService.log(LogService.LOG_INFO, "AMFServlet - DEACTIVATING");
		processor = null;
		if (null != httpService && alias != null) {
			httpService.unregister(alias);
		}
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
			} else if (null != logService) {
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
												event.getSession()));
							} else if (null != logService) {
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

}
