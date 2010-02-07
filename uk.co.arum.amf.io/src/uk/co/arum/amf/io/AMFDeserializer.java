package uk.co.arum.amf.io;

import java.io.DataInputStream;

public class AMFDeserializer {

	private DataInputStream in;

	public AMFDeserializer(DataInputStream in) {
		this.in = in;
	}

	public Object readObject() {
		return null;
	}

}
