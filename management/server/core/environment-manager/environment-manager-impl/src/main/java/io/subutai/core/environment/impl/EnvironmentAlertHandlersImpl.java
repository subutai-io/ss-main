package io.subutai.core.environment.impl;


import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Predicate;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

import io.subutai.common.peer.AlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandler;
import io.subutai.common.peer.EnvironmentAlertHandlers;
import io.subutai.common.peer.EnvironmentId;


/**
 * Environments alert handlers
 */
public class EnvironmentAlertHandlersImpl implements EnvironmentAlertHandlers
{
    private EnvironmentId environmentId;
    private Map<EnvironmentAlertHandler, AlertHandler> handlers =
            new TreeMap<>( new EnvironmentAlertHandlerComparator() );


    public EnvironmentAlertHandlersImpl( final EnvironmentId environmentId )
    {
        this.environmentId = environmentId;
    }


    /**
     * Adds alert handler by id. AlertHandler may be null.
     *
     * @param environmentAlertHandler alert handler id
     * @param alertHandler alert handler
     */
    @Override
    public void add( EnvironmentAlertHandler environmentAlertHandler, AlertHandler alertHandler )
    {
        Preconditions.checkNotNull( environmentAlertHandler, "Invalid environment alert handler" );
        Preconditions.checkArgument( !Strings.isNullOrEmpty( environmentAlertHandler.getAlertHandlerId() ),
                "Invalid environment handler id" );
        Preconditions.checkNotNull( environmentAlertHandler.getAlertHandlerPriority(),
                "Invalid environment handler priority" );

        this.handlers.put( environmentAlertHandler, alertHandler );
    }


    @Override
    public EnvironmentId getEnvironmentId()
    {
        return environmentId;
    }


    @Override
    public Map<EnvironmentAlertHandler, AlertHandler> getAllHandlers()
    {
        return handlers;
    }


    @Override
    @SuppressWarnings( "unchecked" )
    public Collection<AlertHandler> getEffectiveHandlers()
    {
        return CollectionUtils.select( handlers.values(), new Predicate()
        {
            @Override
            public boolean evaluate( final Object o )
            {
                return o != null;
            }
        } );
    }


    public AlertHandler getHandler( final EnvironmentAlertHandler handlerId )
    {
        return handlers.get( handlerId );
    }
}
