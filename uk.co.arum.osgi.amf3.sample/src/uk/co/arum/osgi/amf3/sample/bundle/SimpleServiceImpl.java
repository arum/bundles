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

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;

import uk.co.arum.osgi.amf3.flex.remoting.RemotingContext;
import uk.co.arum.osgi.amf3.http.HttpRequestContext;
import uk.co.arum.osgi.amf3.sample.SampleObject;
import uk.co.arum.osgi.amf3.sample.SimpleService;
import uk.co.arum.osgi.amf3.sample.VerySimpleObject;

public class SimpleServiceImpl implements SimpleService {

	private SampleObject sampleObject;

	public SimpleServiceImpl() {

		SampleObject parent = createSampleObject(null, "parent", 666);

		sampleObject = createSampleObject(parent, "sample object", 1984);
		parent.getChildren().add(sampleObject);

		sampleObject.getChildren().add(
				createSampleObject(sampleObject, "child 1", 1918));
		sampleObject.getChildren().add(
				createSampleObject(sampleObject, "child 2", 1066));

	}

	public void bindHttpService(HttpService httpService) {
		try {
			httpService.registerResources("/sample", "/flexbin", null);
		} catch (NamespaceException e) {
			throw new RuntimeException(e);
		}
	}

	public void unbindHttpService(HttpService httpService) {
		httpService.unregister("/sample");
	}

	private SampleObject createSampleObject(SampleObject parent, String name,
			long value) {
		SampleObject sampleObject = new SampleObject();
		sampleObject.setParent(parent);
		sampleObject.setName(name);
		sampleObject.setValue(value);
		return sampleObject;
	}

	public String echo(String text) {
		dumpRequest();
		return text;
	}

	public String reverse(String text) {
		dumpRequest();
		return new StringBuffer(text).reverse().toString();
	}

	public double random() {
		dumpRequest();
		return Math.random();
	}

	public SampleObject getSampleObject() {
		return sampleObject;
	}

	public SampleObject sendSampleObject(SampleObject incoming) {
		incoming.setValue(incoming.getValue() * 2);
		return incoming;
	}

	public VerySimpleObject sendVerySimpleObject(VerySimpleObject incoming) {
		return incoming;
	}

	public void throwException(String message) {
		throw new RuntimeException(message);
	}

	/**
	 * Shows access to the {@link HttpRequestContext}.
	 */
	private void dumpRequest() {

		HttpServletRequest request = HttpRequestContext.getCurrent()
				.getRequest();

		System.out.println("Method: " + request.getMethod());

		System.out.println("Credentials: "
				+ RemotingContext.getCurrent().getMessage().getHeader(
						"DSRemoteCredentials"));

		boolean headers = false;
		System.out.println("Headers: ");
		Enumeration<?> headersEnum = request.getHeaderNames();
		while (headersEnum.hasMoreElements()) {
			headers = true;
			String headerName = (String) headersEnum.nextElement();
			System.out.println("\t" + headerName + ":");
			Enumeration<?> headerValuesEnum = request.getHeaders(headerName);
			while (headerValuesEnum.hasMoreElements()) {
				System.out.println("\t\t" + headerValuesEnum.nextElement());
			}
		}

		if (!headers) {
			System.out.println("\tNone");
		}

		boolean params = false;
		System.out.println("Parameters: ");
		Enumeration<?> paramsEnum = request.getParameterNames();
		while (paramsEnum.hasMoreElements()) {
			params = true;
			String paramsName = (String) paramsEnum.nextElement();
			System.out.println("\t" + paramsName + ":");
			String[] paramValues = request.getParameterValues(paramsName);
			for (String value : paramValues) {
				System.out.println("\t\t" + value);
			}
		}

		if (!params) {
			System.out.println("\tNone");
		}

	}

}
