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

import org.osgi.framework.Bundle;

public class OSGiServiceConfig {

	private final String name;
	private final Object service;
	private final String[] serviceTypes;
	private final Bundle provider;

	public OSGiServiceConfig(String name, Bundle provider, Object service,
			String[] serviceTypes) {
		this.name = name;
		this.provider = provider;
		this.service = service;
		this.serviceTypes = serviceTypes;
	}

	public Object getService() {
		return service;
	}

	public String getName() {
		return name;
	}

	public String[] getServiceTypes() {
		return serviceTypes;
	}

	public Bundle getProvider() {
		return provider;
	}

}
