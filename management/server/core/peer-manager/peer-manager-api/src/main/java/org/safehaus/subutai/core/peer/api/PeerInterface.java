package org.safehaus.subutai.core.peer.api;


import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.safehaus.subutai.common.protocol.Template;
import org.safehaus.subutai.core.command.api.command.CommandException;
import org.safehaus.subutai.core.container.api.ContainerCreateException;
import org.safehaus.subutai.core.strategy.api.Criteria;


/**
 * Peer interface
 */
public interface PeerInterface
{
    public UUID getOwnerId();

    public Set<ContainerHost> getContainerHostsByEnvironmentId( UUID environmentId ) throws PeerException;

    public Set<ContainerHost> createContainers( UUID ownerPeerId, UUID environmentId, List<Template> templates,
                                                int quantity, String strategyId, List<Criteria> criteria )
            throws ContainerCreateException;

    public void startContainer( ContainerHost containerHost ) throws PeerException, CommandException;

    public void stopContainer( ContainerHost containerHost ) throws PeerException, CommandException;

    public void destroyContainer( ContainerHost containerHost ) throws PeerException;

    public boolean isConnected( Host host ) throws PeerException;
}
