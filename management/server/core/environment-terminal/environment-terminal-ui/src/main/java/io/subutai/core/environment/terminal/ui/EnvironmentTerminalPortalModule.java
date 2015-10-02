package io.subutai.core.environment.terminal.ui;


import java.io.File;
import java.util.Date;
import java.util.Set;

import com.vaadin.ui.Component;

import io.subutai.common.environment.Environment;
import io.subutai.common.peer.ContainerHost;
import io.subutai.common.peer.EnvironmentContainerHost;
import io.subutai.common.util.FileUtil;
import io.subutai.core.environment.api.EnvironmentEventListener;
import io.subutai.core.environment.api.EnvironmentManager;
import io.subutai.server.ui.api.PortalModule;


public class EnvironmentTerminalPortalModule implements PortalModule, EnvironmentEventListener
{

    public static final String MODULE_IMAGE = "env_terminal.png";
    public static final String MODULE_NAME = "Environment Terminal";
    private EnvironmentManager environmentManager;
    private volatile Date updateDate = new Date();


    public EnvironmentTerminalPortalModule( final EnvironmentManager environmentManager )
    {
        this.environmentManager = environmentManager;
    }


    public void init()
    {
        // while empty
    }


    public void destroy()
    {
        // while empty
    }


    @Override
    public String getId()
    {
        return EnvironmentTerminalPortalModule.MODULE_NAME;
    }


    @Override
    public String getName()
    {
        return EnvironmentTerminalPortalModule.MODULE_NAME;
    }


    @Override
    public File getImage()
    {
        return FileUtil.getFile( EnvironmentTerminalPortalModule.MODULE_IMAGE, this );
    }


    @Override
    public Component createComponent()
    {
        return new TerminalForm( environmentManager, updateDate );
    }


    @Override
    public Boolean isCorePlugin()
    {
        return true;
    }


    @Override
    public void onEnvironmentCreated( final Environment environment )
    {
        //ignore
    }


    @Override
    public void onEnvironmentGrown( final Environment environment, final Set<EnvironmentContainerHost> newContainers )
    {
        updateDate.setTime( System.currentTimeMillis() );
    }


    @Override
    public void onContainerDestroyed( final Environment environment, final String containerId )
    {
        updateDate.setTime( System.currentTimeMillis() );
    }


    @Override
    public void onEnvironmentDestroyed( final String environmentId )
    {
        updateDate.setTime( System.currentTimeMillis() );
    }
}
