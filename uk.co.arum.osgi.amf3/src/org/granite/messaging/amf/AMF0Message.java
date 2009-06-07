/*
 * www.openamf.org
 *
 * Distributable under LGPL license.
 * See terms of license at gnu.org.
 */

package org.granite.messaging.amf;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * AMF Message
 * 
 * @author Jason Calabrese <jasonc@missionvi.com>
 * @author Pat Maddox <pergesu@users.sourceforge.net>
 * @see AMF0Header
 * @see AMF0Body
 * @version $Revision: 1.13 $, $Date: 2003/11/30 02:25:00 $
 */
public class AMF0Message implements Serializable, Iterable<AMF0Body> {

	private static final long serialVersionUID = 1L;

	public static final String CONTENT_TYPE = "application/x-amf";
	public static final int CURRENT_VERSION = 3;

	protected int version = CURRENT_VERSION;
	protected final List<AMF0Header> headers = new ArrayList<AMF0Header>();
	protected final List<AMF0Body> bodies = new ArrayList<AMF0Body>();

	public void addHeader(String key, boolean required, Object value) {
		addHeader(new AMF0Header(key, required, value));
	}

	public void addHeader(AMF0Header header) {
		headers.add(header);
	}

	public int getHeaderCount() {
		return headers.size();
	}

	public AMF0Header getHeader(int index) {
		return headers.get(index);
	}

	/**
	 * 
	 * @return a List that contains zero or more {@link AMF0Header} objects
	 * 
	 */
	public List<AMF0Header> getHeaders() {
		return headers;
	}

	public AMF0Body addBody(String target, String response, Object value,
			byte type) {
		return addBody(new AMF0Body(target, response, value, type));
	}

	public AMF0Body addBody(AMF0Body body) {
		bodies.add(body);
		return body;
	}

	public int getBodyCount() {
		return bodies.size();
	}

	public AMF0Body getBody(int index) {
		return bodies.get(index);
	}

	public Iterator<AMF0Body> iterator() {
		return bodies.iterator();
	}

	public boolean isFirstMessage() {
		if (bodies.size() == 1)
			return bodies.get(0).isFirstBody();

		for (AMF0Body body : bodies) {
			if (body.isFirstBody())
				return true;
		}

		return false;
	}

	public int getVersion() {
		return version;
	}

	public void setVersion(int version) {
		this.version = version;
	}

}
