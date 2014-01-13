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

import java.io.IOException;
import org.apache.zookeeper.KeeperException;
import static org.testng.Assert.*;
import org.testng.annotations.Test;

/**
 *
 * @author nuboat
 */
public class DemoNGTest {

	@Test
	public void testRun() throws Exception {
		try {
			final Demo instance = new Demo();
			instance.run();
		} catch(final IOException | KeeperException | InterruptedException e) {
			fail(e.getMessage(), e);
		}
	}

}
