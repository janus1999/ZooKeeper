/*
 * Attribution
 * CC BY
 * This license lets others distribute, remix, tweak,
 * and build upon your work, even commercially,
 * as long as they credit you for the original creation.
 * This is the most accommodating of licenses offered.
 * Recommended for maximum dissemination and use of licensed materials.
 *
 * http://creativecommons.org/licenses/by/3.0/
 * http://creativecommons.org/licenses/by/3.0/legalcode
 */
package com.thjug.process;

import org.apache.zookeeper.AsyncCallback;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nuboat
 */
public class LogStatCallback implements AsyncCallback.StatCallback {

	private static final Logger LOG = LoggerFactory.getLogger(LogStatCallback.class);

	@Override
	public void processResult(final int rc, final String path, final Object ctx, final Stat stat) {
		LOG.info("rc: {}", rc);
		LOG.info("path: {}", path);
		LOG.info("ctx: {}", ctx);
		LOG.info("Stat: {}", stat.toString());
	}

}
