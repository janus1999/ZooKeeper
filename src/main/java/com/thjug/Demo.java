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
package com.thjug;

import com.thjug.factory.ZooKeeperFactory;
import com.thjug.process.LogStatCallback;
import com.thjug.process.LogWatcher;
import java.io.IOException;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author nuboat
 */
public class Demo {

	private static final Logger LOG = LoggerFactory.getLogger(Demo.class);

	public void run() throws IOException, KeeperException, InterruptedException {

		final ZooKeeper zk = ZooKeeperFactory.createZooKeeper(new LogWatcher());

		final String createresult = zk.create("/javaapi"
				, "Hello World".getBytes()
				, ZooDefs.Ids.OPEN_ACL_UNSAFE
				, CreateMode.PERSISTENT_SEQUENTIAL);
		LOG.info("Create Result : {}", createresult);

		zk.exists(createresult, true, new LogStatCallback(), null);

		final Stat existsresult = zk.exists(createresult, true);
		LOG.info("Exists Result : {}", existsresult.toString());

		final Stat setresult = zk.setData(createresult
				, "Yoyo Land".getBytes()
				, existsresult.getVersion());
		LOG.info("Set Result : {}", setresult.toString());
	}

}
