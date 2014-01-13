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
package com.thjug.factory;

import java.io.IOException;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;

/**
 *
 * @author nuboat
 */
public class ZooKeeperFactory {

	private static final String HOST = "192.168.6.26:2181";
	private static final int SESSION_TIMEOUT = 3000;

	public static ZooKeeper createZooKeeper(final Watcher watcher)
		throws IOException {
		
		return new ZooKeeper(HOST, SESSION_TIMEOUT, watcher);
	}

	private ZooKeeperFactory() { }

}
