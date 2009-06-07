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

package uk.co.arum.osgi.amf3.sample;

import java.util.ArrayList;
import java.util.List;

public class SampleObject {

	private SampleObject parent;

	private String name;

	private long value;

	private List<SampleObject> children;

	public SampleObject getParent() {
		return parent;
	}

	public void setParent(SampleObject parent) {
		this.parent = parent;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getValue() {
		return value;
	}

	public void setValue(long value) {
		this.value = value;
	}

	public List<SampleObject> getChildren() {
		if (null == children) {
			children = new ArrayList<SampleObject>();
		}
		return children;
	}

	public void setChildren(List<SampleObject> children) {
		this.children = children;
	}

}
