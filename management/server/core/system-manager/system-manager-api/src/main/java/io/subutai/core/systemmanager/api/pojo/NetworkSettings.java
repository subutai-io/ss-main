package io.subutai.core.systemmanager.api.pojo;


public interface NetworkSettings
{
    public void setSecurePortX1( final int securePortX1 );

    public int getSecurePortX1();

    public void setSecurePortX2( final int securePortX2 );

    public int getSecurePortX2();


    public String getPublicUrl();

    public void setPublicUrl( final String publicUrl );

    public int getAgentPort();

    public void setAgentPort( final int agentPort );

    public int getPublicSecurePort();

    public void setPublicSecurePort( final int publicSecurePort );

    public String getKeyServer();

    public void setKeyServer( final String keyServer );
}
