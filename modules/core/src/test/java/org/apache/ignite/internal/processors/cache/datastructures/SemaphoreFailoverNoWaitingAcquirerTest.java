
package org.apache.ignite.internal.processors.cache.datastructures;

import org.apache.ignite.Ignite;
import org.apache.ignite.IgniteSemaphore;
import org.apache.ignite.cache.CacheMode;
import org.apache.ignite.configuration.AtomicConfiguration;
import org.apache.ignite.configuration.IgniteConfiguration;
import org.apache.ignite.spi.discovery.tcp.TcpDiscoverySpi;
import org.apache.ignite.spi.discovery.tcp.ipfinder.TcpDiscoveryIpFinder;
import org.apache.ignite.spi.discovery.tcp.ipfinder.vm.TcpDiscoveryVmIpFinder;
import org.apache.ignite.testframework.junits.common.GridCommonAbstractTest;

import java.util.concurrent.TimeUnit;

import static org.apache.ignite.cache.CacheMode.PARTITIONED;
import static org.apache.ignite.cache.CacheMode.REPLICATED;

/**
 * 
 * Class to test the retrieval of a permit on a semaphore after initial semaphore owner has been closed. 
 * 
 * IGNITE-7090
 * 
 * <p>
 * <b><pre>
 * </pre></b>
 *
 * @author Tim Onyschak
 * @version $Revision: $
 */
public class SemaphoreFailoverNoWaitingAcquirerTest extends GridCommonAbstractTest {
    /** */
    protected static TcpDiscoveryIpFinder ipFinder = new TcpDiscoveryVmIpFinder(true);

    /** Grid count. */
    private static final int GRID_CNT = 3;

    /** Atomics cache mode. */
    private CacheMode atomicsCacheMode;

    /** {@inheritDoc} */
    @Override protected IgniteConfiguration getConfiguration(String igniteInstanceName) throws Exception {
        IgniteConfiguration cfg = super.getConfiguration(igniteInstanceName);

        TcpDiscoverySpi spi = new TcpDiscoverySpi();

        spi.setIpFinder(ipFinder);

        cfg.setDiscoverySpi(spi);

        AtomicConfiguration atomicCfg = atomicConfiguration();

        assertNotNull(atomicCfg);

        cfg.setAtomicConfiguration(atomicCfg);

        return cfg;
    }

    /**
     * @throws Exception If failed.
     */
    public void testReleasePermitsPartitioned() throws Exception {
        atomicsCacheMode = PARTITIONED;

        doTest();
    }

    /**
     * @throws Exception If failed.
     */
    public void testReleasePermitsReplicated() throws Exception {
        atomicsCacheMode = REPLICATED;

        doTest();
    }

    /**
     * @throws Exception If failed.
     */
    private void doTest() throws Exception {
        try {
            startGrids(GRID_CNT);

            Ignite ignite = grid(0);

            IgniteSemaphore sem = ignite.semaphore("sem", 1, true, true);

            // Initialize second semaphore before the first one is broken.

            assertEquals(1, sem.availablePermits());

            sem.acquire(1);

            assertEquals(0, sem.availablePermits());

            ignite.close();

            awaitPartitionMapExchange();
            IgniteSemaphore sem2 = grid(1).semaphore("sem", 1, true, true);

            assertTrue("Could not aquire after 'restart'",sem2.tryAcquire(1, 5000, TimeUnit.MILLISECONDS));
        }
        finally {
            stopAllGrids();
        }
    }

    /**
     * @return Atomic configuration.
     */
    protected AtomicConfiguration atomicConfiguration() {
        AtomicConfiguration atomicCfg = new AtomicConfiguration();

        atomicCfg.setCacheMode(atomicsCacheMode);

        if (atomicsCacheMode == PARTITIONED)
            atomicCfg.setBackups(1);

        return atomicCfg;
    }
}
