package org.safehaus.subutai.common.util;


import java.util.UUID;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;


/**
 * Created by dilshat on 8/25/14.
 */
public class UUIDUtil
{

    //    public static UUID generateTimeBasedUUID()
    //    {
    //        return Generators.timeBasedGenerator( EthernetAddress.fromInterface() ).generate();
    //    }
    //
    //
    //    public static UUID generateCassandraUUID()
    //    {
    //        return Generators.randomBasedGenerator().generate();
    //    }
    //
    //
    //    public static UUID generateStringUUID( String id )
    //    {
    //        return Generators.nameBasedGenerator().generate( id );
    //    }
    //
    //
    //    public static UUID generateMACBasedUUID()
    //    {
    //        return UUID.nameUUIDFromBytes( SysUtil.getMacAddress().getBytes() );
    //    }


    public static UUID generateTimeBasedUUID()
    {
        return Generators.timeBasedGenerator( EthernetAddress.fromInterface() ).generate();
    }


    public static UUID generateMACBasedUUID()
    {
        return UUID.nameUUIDFromBytes( SysUtil.getMacAddress().getBytes() );
    }
}
