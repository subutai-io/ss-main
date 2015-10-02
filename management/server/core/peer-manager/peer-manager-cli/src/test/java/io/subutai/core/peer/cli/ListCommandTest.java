package io.subutai.core.peer.cli;


import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import io.subutai.common.peer.Peer;
import io.subutai.common.peer.PeerException;
import io.subutai.common.peer.PeerInfo;
import io.subutai.common.test.SystemOutRedirectTest;
import io.subutai.core.peer.api.PeerManager;

import com.google.common.collect.Lists;

import static junit.framework.TestCase.assertTrue;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;


@RunWith( MockitoJUnitRunner.class )
public class ListCommandTest extends SystemOutRedirectTest
{
    private static final String PEER_ID = UUID.randomUUID().toString();
    private static final String ERR_MSG = "error";
    @Mock
    PeerManager peerManager;
    @Mock
    Peer peer;
    @Mock
    PeerInfo peerInfo;

    ListCommand command;


    @Before
    public void setUp() throws Exception
    {
        command = new ListCommand();
        command.setPeerManager( peerManager );
        when( peerManager.getPeers() ).thenReturn( Lists.newArrayList( peer ) );
        when( peer.isOnline() ).thenReturn( true );
        when( peer.getId() ).thenReturn( PEER_ID );
        when( peer.getPeerInfo() ).thenReturn( peerInfo );
    }


    @Test
    public void testDoExecute() throws Exception
    {
        command.doExecute();

        assertTrue( getSysOut().contains( PEER_ID.toString() ) );

        doThrow( new PeerException( ERR_MSG ) ).when( peer ).isOnline();

        command.doExecute();

        assertTrue( getSysOut().contains( ERR_MSG ) );
    }
}
