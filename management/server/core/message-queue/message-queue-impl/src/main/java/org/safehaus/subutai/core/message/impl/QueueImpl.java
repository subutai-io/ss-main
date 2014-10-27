package org.safehaus.subutai.core.message.impl;


import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.safehaus.subutai.common.exception.HTTPException;
import org.safehaus.subutai.common.util.JsonUtil;
import org.safehaus.subutai.common.util.RestUtil;
import org.safehaus.subutai.core.message.api.Message;
import org.safehaus.subutai.core.message.api.MessageException;
import org.safehaus.subutai.core.message.api.MessageListener;
import org.safehaus.subutai.core.message.api.MessageStatus;
import org.safehaus.subutai.core.message.api.Queue;
import org.safehaus.subutai.core.peer.api.Peer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonSyntaxException;


/**
 * Implementation of Queue
 */
public class QueueImpl implements Queue
{
    private static final Logger LOG = LoggerFactory.getLogger( QueueImpl.class.getName() );
    private final Set<MessageListener> listeners =
            Collections.newSetFromMap( new ConcurrentHashMap<MessageListener, Boolean>() );


    @Override
    public Message createMessage( final Serializable payload ) throws MessageException
    {
        return new MessageImpl( payload );
    }


    @Override
    public UUID sendMessage( final Peer peer, final Message message, final String recipient, final long ttl )
            throws MessageException
    {
        //TODO save to persistent queue so that background task will try to send it
        try
        {
            Map<String, String> params = new HashMap<>();
            params.put( "message", JsonUtil.toJson( message ) );
            RestUtil.post( "http://172.16.131.203:8181/cxf/queue/message", params );
        }
        catch ( HTTPException e )
        {
            e.printStackTrace();
        }
        return null;
    }


    @Override
    public MessageStatus getMessageStatus( final UUID messageId )
    {
        return null;
    }


    //TODO notify listener in thread pool (see metric)
    @Override
    public void processMessage( String messageJson ) throws MessageException
    {
        try
        {
            Message message = JsonUtil.fromJson( messageJson, MessageImpl.class );

            for ( MessageListener listener : listeners )
            {
                listener.onMessage( message );
            }
        }
        catch ( JsonSyntaxException e )
        {
            LOG.error( "Error in processMessage", e );
        }
    }


    @Override
    public void addMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            listeners.add( listener );
        }
    }


    @Override
    public void removeMessageListener( final MessageListener listener )
    {
        if ( listener != null )
        {
            listeners.remove( listener );
        }
    }
}
