package io.subutai.hub.share.parser;


import io.subutai.hub.share.resource.ContainerResourceType;
import io.subutai.hub.share.resource.ResourceValue;
import io.subutai.hub.share.resource.ResourceValueParser;


/**
 * Common resource value parser
 */
public final class CommonResourceValueParser
{
    private CommonResourceValueParser()
    {
        throw new IllegalAccessError( "Utility class" );
    }


    public static ResourceValueParser getInstance( ContainerResourceType type )
    {
        ResourceValueParser result = null;
        switch ( type )
        {
            case CPU:
                result = CpuResourceValueParser.getInstance();
                break;
            case CPUSET:
                result = CpuSetResourceValueParser.getInstance();
                break;
            case RAM:
                result = RamResourceValueParser.getInstance();
                break;
            case NET:
                result = NetResourceValueParser.getInstance();
                break;
            case OPT:
            case HOME:
            case VAR:
            case ROOTFS:
                result = DiskResourceValueParser.getInstance();
                break;
        }
        return result;
    }


    public static ResourceValue parse( String resource, ContainerResourceType type )
    {
        ResourceValueParser parser = getInstance( type );
        if ( parser == null )
        {
            throw new IllegalArgumentException( "Resource value parser not registered for type: " + type );
        }
        return parser.parse( resource );
    }
}
