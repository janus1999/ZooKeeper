package org.apache.example;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Executor implements Watcher, Runnable, DataMonitor.DataMonitorListener {

	private static final Logger LOG = LoggerFactory.getLogger(Executor.class);
	
	public static void main(final String[] args) {
		final String hostPort = "192.168.6.26:2181"; // args[0];
		final String znode = "/example"; // args[1];
		final String filename = "/home/entiera/zoo.txt"; // args[2];
		final String exec[] = new String[] { "notepad.exe" } ; // new String[args.length - 3];
		try {
			new Executor(hostPort, znode, filename, exec).run();
		} catch (final IOException | KeeperException e) {
			
			LOG.error(e.getMessage(), e);
		}
	}

	String znode;

	final DataMonitor dm;

	final ZooKeeper zk;

	final String filename;

	final String exec[];

	Process child;

	public Executor(final String hostPort, final String znode, final String filename, final String exec[])
			throws KeeperException, IOException {
		this.filename = filename;
		this.exec = exec;
		zk = new ZooKeeper(hostPort, 3000, this);
		dm = new DataMonitor(zk, znode, null, this);
	}

	/**
	 * *************************************************************************
	 * We do process any events ourselves, we just need to forward them on.
	 *
	 * @see org.apache.zookeeper.Watcher#process(org.apache.zookeeper.proto.WatcherEvent)
	 */
	@Override
	public void process(final WatchedEvent event) {
		LOG.info("process");
		dm.process(event);
	}

	@Override
	public void run() {
		LOG.info("run");
		try {
			synchronized (this) {
				while (!dm.dead) {
					wait();
				}
			}
		} catch (final InterruptedException e) {
			LOG.error(e.getMessage(), e);
		}
	}

	@Override
	public void closing(final int rc) {
		LOG.info("closing");
		synchronized (this) {
			notifyAll();
		}
	}

	@Override
	public void exists(final byte[] data) {
		LOG.info("exists");
		if (data == null) {
			if (child != null) {
				LOG.info("Killing process");
				child.destroy();
				try {
					child.waitFor();
				} catch (final InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			child = null;
		} else {
			if (child != null) {
				LOG.info("Stopping child");
				child.destroy();
				try {
					child.waitFor();
				} catch (final InterruptedException e) {
					LOG.error(e.getMessage(), e);
				}
			}
			try {
				try (final FileOutputStream fos = new FileOutputStream(filename)) {
					fos.write(data);
				}
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}
			try {
				LOG.info("Starting child");
				child = Runtime.getRuntime().exec(exec);
				new ThreadWriter(child.getInputStream(), System.out).start();
				new ThreadWriter(child.getErrorStream(), System.err).start();
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}
		}
	}

	static class ThreadWriter extends Thread {

		final OutputStream os;
		final InputStream is;

		ThreadWriter(final InputStream is, final OutputStream os) {
			this.is = is;
			this.os = os;
		}

		@Override
		public void run() {
			final byte b[] = new byte[80];
			int rc;
			try {
				while ((rc = is.read(b)) > 0) {
					os.write(b, 0, rc);
				}
			} catch (final IOException e) {
				LOG.error(e.getMessage(), e);
			}

		}
	}
}
