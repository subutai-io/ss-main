package io.subutai.core.metric.impl;


import java.util.UUID;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.containsString;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;


/**
 * Test for ResourceHostMetric
 */
public class ResourceHostMetricTest
{

    @Test
    public void testSetPeerId() throws Exception
    {
        String peerId = UUID.randomUUID().toString();
        ResourceHostMetricImpl resourceHostMetric = new ResourceHostMetricImpl();

        resourceHostMetric.setPeerId( peerId );

        assertEquals( peerId, resourceHostMetric.getPeerId() );
    }


    @Test
    public void testToString() throws Exception
    {
        String peerId = UUID.randomUUID().toString();
        ResourceHostMetricImpl resourceHostMetric = new ResourceHostMetricImpl();

        resourceHostMetric.setPeerId( peerId );

        assertThat( resourceHostMetric.toString(), containsString( peerId.toString() ) );
    }
}
