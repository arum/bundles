package uk.co.arum.osgi.amf3.sample.test001;

import java.util.HashSet;
import java.util.Set;

public class TopLevel {

	private Set<MidLevel> midLevels;

	public Set<MidLevel> getMidLevels() {
		if (null == midLevels) {
			midLevels = new HashSet<MidLevel>();
		}
		return midLevels;
	}

	public void setMidLevels(Set<MidLevel> midLevels) {
		getMidLevels().clear();
		if (null != midLevels) {
			getMidLevels().addAll(midLevels);
		}
	}

}
