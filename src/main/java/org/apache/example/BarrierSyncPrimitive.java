package org.apache.example;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Random;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BarrierSyncPrimitive implements Watcher {

	private static final Logger LOG = LoggerFactory.getLogger(BarrierSyncPrimitive.class);
	static Integer mutex;

	public static void main(final String args[]) {
		barrierTest(new String[]{"192.168.6.26:2181", "/b1", "4"});
	}

	public static void barrierTest(String args[]) {
		final Barrier b = new Barrier(args[0], args[1], new Integer(args[2]));
		try {
			final boolean flag = b.enter();
			LOG.info("Entered barrier: " + args[2]);
			if (!flag) {
				LOG.info("Error when entering the barrier");
			}
		} catch (final KeeperException | InterruptedException e) {
			LOG.info(e.getMessage(), e);
		}

		// Generate random integer
		final Random rand = new Random();
		final int r = rand.nextInt(100);

		// Loop for rand iterations
		for (int i = 0; i < r; i++) {
			try {
				Thread.sleep(100);
			} catch (final InterruptedException e) {
				LOG.error(e.getMessage(), e);
			}
		}

		try {
			b.leave();
		} catch (final KeeperException | InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
		LOG.info("Left barrier");
	}
	
	String root;
	ZooKeeper zk;

	BarrierSyncPrimitive(final String address) {
		mutex = new Integer(-1);
		try {
			LOG.info("Starting ZK:");
			zk = new ZooKeeper(address, 3000, this);
		} catch (final IOException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	synchronized public void process(final WatchedEvent event) {
		synchronized (mutex) {
			LOG.info("Process: " + event.getType());
			mutex.notify();
		}
	}

	static public class Barrier extends BarrierSyncPrimitive {

		int size;
		String name;
		String namesequence;

		/**
		 * Barrier constructor
		 *
		 * @param address
		 * @param root
		 * @param size
		 */
		Barrier(final String address, final String root, final int size) {
			super(address);
			this.root = root;
			this.size = size;

			// Create barrier node
			if (zk != null) {
				try {
					final Stat s = zk.exists(root, false);
					if (s == null) {
						zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				} catch (final KeeperException e) {
					LOG.error("Keeper exception when instantiating queue: " + e.toString(), e);
				} catch (final InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}

			// My node name
			try {
				name = InetAddress.getLocalHost().getCanonicalHostName().toString();
			} catch (final UnknownHostException e) {
				LOG.error(e.getMessage(), e);
			}

		}

		/**
		 * Join barrier
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */
		boolean enter() throws KeeperException, InterruptedException {
			namesequence = zk.create(root + "/" + name, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
			while (true) {
				synchronized (mutex) {
					final List<String> list = zk.getChildren(root, true);
					if (list.size() < size) {
						mutex.wait();
					} else {
						return true;
					}
				}
			}
		}

		/**
		 * Wait until all reach barrier
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */
		boolean leave() throws KeeperException, InterruptedException {
			zk.delete(namesequence, 0);
			while (true) {
				synchronized (mutex) {
					final List<String> list = zk.getChildren(root, true);
					if (list.size() > 0) {
						mutex.wait();
					} else {
						return true;
					}
				}
			}
		}
	}

}
