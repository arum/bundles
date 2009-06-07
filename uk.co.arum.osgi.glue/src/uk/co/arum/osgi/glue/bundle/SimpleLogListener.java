package uk.co.arum.osgi.glue.bundle;

import java.util.Date;

import org.osgi.service.log.LogEntry;
import org.osgi.service.log.LogListener;

public class SimpleLogListener implements LogListener {

	public void logged(LogEntry entry) {
		Throwable ex = entry.getException();
		String error = (null == ex) ? "" : " : " + ex.getMessage();
		if (null != ex) {
			ex.printStackTrace();
		}

		System.out.printf("[%s] %d : [%s]%s", new Date(), entry.getLevel(),
				entry.getMessage(), error);
		System.out.println();
	}
}
