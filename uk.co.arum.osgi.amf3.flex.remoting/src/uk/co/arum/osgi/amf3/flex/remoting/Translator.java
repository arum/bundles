package uk.co.arum.osgi.amf3.flex.remoting;

public interface Translator {

	boolean canTranslate(Object o);

	Object translate(Object o);

}
