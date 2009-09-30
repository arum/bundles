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

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;

import uk.co.arum.osgi.amf3.flex.remoting.OSGiAMFConstants;
import uk.co.arum.osgi.amf3.flex.remoting.Translator;

public class OSGiAMFConfig {

	private final BundleContext context;

	private Map<String, OSGiServiceConfig> configs = new HashMap<String, OSGiServiceConfig>();
	private Map<Long, Long> bundleCounts = new HashMap<Long, Long>();
	private Map<Long, Bundle> bundles = new HashMap<Long, Bundle>();

	public OSGiAMFConfig(BundleContext context) {
		this.context = context;
	}

	/**
	 * Load the class with the given name from the bundles that have registered
	 * a service.
	 * <p/>
	 * 
	 * TODO cache the class, but uncache if associated service disappears
	 * 
	 * @param name
	 *            the class name to load
	 * @return the class that is required
	 * @throws RuntimeException
	 *             if there are too many bundles which can load this class
	 */
	public Class<?> loadClass(String name) {

		Set<Class<?>> classes = new HashSet<Class<?>>();
		for (Bundle bundle : bundles.values()) {
			try {
				classes.add(bundle.loadClass(name));
			} catch (ClassNotFoundException e) {
				// could well happen
			}
		}

		if (classes.size() > 1) {
			throw new RuntimeException(
					"Too many registered service bundles capable of loading ["
							+ name + "");
		} else if (classes.size() == 0) {
			return null;
		}

		return classes.iterator().next();
	}

	public void addOSGiService(ServiceReference ref) {

		Object o = context.getService(ref);

		String[] serviceTypes = (String[]) ref
				.getProperty(Constants.OBJECTCLASS);

		String serviceName = (String) ref
				.getProperty(OSGiAMFConstants.AMF_SERVICE_NAME);

		OSGiServiceConfig config = new OSGiServiceConfig(serviceName, ref
				.getBundle(), o, serviceTypes);

		configs.put(serviceName, config);

		Long count = bundleCounts.get(ref.getBundle().getBundleId());
		if (null == count) {
			count = 0L;
			bundles.put(ref.getBundle().getBundleId(), ref.getBundle());
		}
		count++;
		bundleCounts.put(ref.getBundle().getBundleId(), count);
	}

	public void removeOSGiService(ServiceReference ref) {
		String serviceName = (String) ref
				.getProperty(OSGiAMFConstants.AMF_SERVICE_NAME);

		configs.remove(serviceName);
		context.ungetService(ref);

		Long count = bundleCounts.get(ref.getBundle().getBundleId());
		if (null == count) {
			count = 0L;
		}

		count--;
		if (count <= 0) {
			bundles.remove(ref.getBundle().getBundleId());
			bundleCounts.remove(ref.getBundle().getBundleId());
		} else {
			bundleCounts.put(ref.getBundle().getBundleId(), count);
		}

	}

	public OSGiServiceConfig getServiceConfig(String destination) {
		return configs.get(destination);
	}

	public void dispose() {
		configs.clear();
	}

	public Object translate(Object from) {

		if (null == from || from instanceof Void) {
			return from;
		}

		ServiceReference[] refs = null;

		try {
			refs = context.getServiceReferences(Translator.class.getName(),
					null);
		} catch (InvalidSyntaxException e) {
			throw new RuntimeException(e);
		}

		for (ServiceReference ref : refs) {
			Translator t = (Translator) context.getService(ref);
			try {
				if (t.canTranslate(from)) {
					return t.translate(from);
				}
			} finally {
				context.ungetService(ref);
			}
		}

		return from;
	}

}
