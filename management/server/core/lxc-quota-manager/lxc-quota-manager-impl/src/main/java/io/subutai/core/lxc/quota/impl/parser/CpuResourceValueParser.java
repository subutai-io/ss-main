package io.subutai.core.lxc.quota.impl.parser;


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Preconditions;

import io.subutai.common.resource.NumericValueResource;
import io.subutai.common.resource.ResourceValueParser;


/**
 * Percent value resource parser
 */
public class CpuResourceValueParser implements ResourceValueParser
{
    private static final String QUOTA_REGEX = "(\\d+)(%)?";
    private static final Pattern QUOTA_PATTERN = Pattern.compile( QUOTA_REGEX );
    private static CpuResourceValueParser instance;


    public static CpuResourceValueParser getInstance()
    {
        if ( instance == null )
        {
            instance = new CpuResourceValueParser();
        }
        return instance;
    }


    private CpuResourceValueParser() {}


    @Override
    public NumericValueResource parse( String resource )
    {
        Preconditions.checkNotNull( resource );

        Matcher quotaMatcher = QUOTA_PATTERN.matcher( resource.trim() );
        if ( quotaMatcher.matches() )
        {
            String value = quotaMatcher.group( 1 );
            String acronym = quotaMatcher.group( 2 );
            if ( acronym != null && "%".equals( acronym.trim() ) )
            {
                throw new IllegalArgumentException( "Invalid measure unit." );
            }
            return new NumericValueResource( value );
        }
        else
        {
            throw new IllegalArgumentException( String.format( "Could not parse resource: %s", resource ) );
        }
    }
}
