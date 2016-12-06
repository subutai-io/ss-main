package io.subutai.core.appender;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import io.subutai.hub.share.dto.SubutaiSystemLog;

import static junit.framework.TestCase.assertEquals;


@RunWith( MockitoJUnitRunner.class )
public class SubutaiSystemLogTest
{
    private SubutaiSystemLog subutaiSystemLog;


    private static final long TIMESTAMP = System.currentTimeMillis();
    private static final String SOURCE = "PEER";
    private static final String LOGGER = "LOGGER";
    private static final String ERR_MSG = "ERROR";
    private static final String ERR_STACKTRACE = "ERROR\nCAUSE";


    @Before
    public void setUp() throws Exception
    {
        subutaiSystemLog =
                new SubutaiSystemLog( SOURCE, SubutaiSystemLog.LogType.ERROR, TIMESTAMP, LOGGER, ERR_MSG, ERR_STACKTRACE );
    }


    @Test
    public void testProperties() throws Exception
    {
        assertEquals( TIMESTAMP, subutaiSystemLog.getTimeStamp() );
        assertEquals( SOURCE, subutaiSystemLog.getSource() );
        assertEquals( LOGGER, subutaiSystemLog.getLoggerName() );
        assertEquals( ERR_MSG, subutaiSystemLog.getRenderedMessage() );
        assertEquals( ERR_STACKTRACE, subutaiSystemLog.getStackTrace() );
    }
}
