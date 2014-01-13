package org.apache.example;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueueSyncPrimitive implements Watcher {

	private static final Logger LOG = LoggerFactory.getLogger(QueueSyncPrimitive.class);
	static Integer mutex;

	public static void main(String args[]) {
		queueTest(new String[]{"192.168.6.26:2181", "/q1", "40", "p"});
	}

	public static void queueTest(String args[]) {
		final Queue q = new Queue(args[0], args[1]);

		final Integer max = new Integer(args[2]);

		if (args[3].equals("p")) {
			LOG.info("Producer");
			for (int i = 0; i < max; i++) {
				try {
					q.produce(10 + i);
				} catch (final KeeperException | InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		} else {
			LOG.info("Consumer");
			do {
				try {
					final int r = q.consume();
					LOG.info("Item: " + r);
				} catch (final InterruptedException | KeeperException e) {
					LOG.error(e.getMessage(), e);
				}
			} while(true);
		}
	}

	String root;
	ZooKeeper zk;

	QueueSyncPrimitive(final String address) {
		mutex = new Integer(-1);
		try {
			LOG.info("Starting ZK:");
			zk = new ZooKeeper(address, 3000, this);
			LOG.info("Finished starting ZK: " + zk);
		} catch (final IOException e) {
			LOG.info(e.toString());
		}
	}

	@Override
	synchronized public void process(final WatchedEvent event) {
		synchronized (mutex) {
			LOG.info("Process: " + event.getType());
			mutex.notify();
		}
	}

	static public class Queue extends QueueSyncPrimitive {

		/**
		 * Constructor of producer-consumer queue
		 *
		 * @param address
		 * @param name
		 */
		Queue(final String address, final String name) {
			super(address);
			this.root = name;
			if (zk != null) {
				try {
					final Stat s = zk.exists(root, false);
					if (s == null) {
						zk.create(root, new byte[0], Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
					}
				} catch (final KeeperException | InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
		}

		/**
		 * Add element to the queue.
		 *
		 * @param i
		 * @return
		 */
		boolean produce(final int i) throws KeeperException, InterruptedException {
			final ByteBuffer b = ByteBuffer.allocate(4);
			final byte[] value;

			// Add child with value i
			b.putInt(i);
			value = b.array();
			zk.create(root + "/element", value, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT_SEQUENTIAL);

			return true;
		}

		/**
		 * Remove first element from the queue.
		 *
		 * @return
		 * @throws KeeperException
		 * @throws InterruptedException
		 */
		int consume() throws KeeperException, InterruptedException {
			int retvalue;
			final Stat stat = new Stat();

			// Get the first element available
			while (true) {
				synchronized (mutex) {
					final List<String> list = zk.getChildren(root, true);
					if (list.isEmpty()) {
						LOG.info("Going to wait");
						mutex.wait();
					} else {
						String min = list.get(0).substring(7);
						for (final String s : list) {
							final String tempValue = s.substring(7);

							if (Integer.valueOf(tempValue) < Integer.valueOf(min)) {
								min = tempValue;
							}
						}
						LOG.info("Temporary value: " + root + "/element" + min);
						final byte[] b = zk.getData(root + "/element" + min, false, stat);
						zk.delete(root + "/element" + min, stat.getVersion());
						final ByteBuffer buffer = ByteBuffer.wrap(b);
						retvalue = buffer.getInt();

						return retvalue;
					}
				}
			}
		}
	}
}
