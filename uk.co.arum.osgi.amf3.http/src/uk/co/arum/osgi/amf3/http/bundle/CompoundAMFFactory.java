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

import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.HashSet;
import java.util.Set;

import uk.co.arum.osgi.amf3.AMFFactory;

public class CompoundAMFFactory implements AMFFactory {

	private Set<AMFFactory> factories = new HashSet<AMFFactory>();

	public String getDescription() {
		return "A compound factory";
	}

	void add(AMFFactory factory) {
		factories.add(factory);
	}

	void remove(AMFFactory factory) {
		factories.remove(factory);
	}

	public boolean isExternaliser(String name) {
		for (AMFFactory factory : factories) {
			if (factory.isExternaliser(name)) {
				return true;
			}
		}
		return false;
	}

	public Class<?> loadClass(String name) {
		for (AMFFactory factory : factories) {
			Class<?> c = factory.loadClass(name);
			if (null != c) {
				return c;
			}
		}

		return null;
	}

	public Object newInstance(String name) {
		for (AMFFactory factory : factories) {
			Object o = factory.newInstance(name);
			if (null != o) {
				return o;
			}
		}

		return null;
	}

	public Object process(Object in) {
		for (AMFFactory factory : factories) {
			Object out = factory.process(in);
			if (null != out) {
				return out;
			}
		}
		return null;
	}

	public String getResponseTarget(Object response) {
		for (AMFFactory factory : factories) {
			String target = factory.getResponseTarget(response);
			if (null != target) {
				return target;
			}
		}
		return null;
	}

	public Class<?> getObjectClass(Object o) {
		for (AMFFactory factory : factories) {
			Class<?> c = factory.getObjectClass(o);
			if (null != c) {
				return c;
			}
		}
		return o.getClass();
	}

	public Object readExternal(String name, ObjectInput input) {
		for (AMFFactory factory : factories) {
			Object o = factory.readExternal(name, input);
			if (null != o) {
				return o;
			}
		}
		return null;
	}

	public boolean writeExternal(Object o, ObjectOutput output) {
		for (AMFFactory factory : factories) {
			if (factory.writeExternal(o, output)) {
				return true;
			}
		}
		return false;
	}

}
