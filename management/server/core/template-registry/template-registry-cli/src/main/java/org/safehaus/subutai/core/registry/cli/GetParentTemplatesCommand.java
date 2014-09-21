package org.safehaus.subutai.core.registry.cli;


import java.util.List;

import org.safehaus.subutai.core.registry.api.Template;
import org.safehaus.subutai.core.registry.api.TemplateRegistryManager;

import org.apache.karaf.shell.commands.Argument;
import org.apache.karaf.shell.commands.Command;
import org.apache.karaf.shell.console.OsgiCommandSupport;

import com.google.common.base.Strings;


/**
 * CLI for TemplateRegistryManager.getParentTemplates command
 */
@Command(scope = "registry", name = "get-parent-templates", description = "Get all parent templates")
public class GetParentTemplatesCommand extends OsgiCommandSupport
{
    @Argument(index = 0, name = "child template name", required = true, multiValued = false,
            description = "child template name")
    String childTemplateName;
    @Argument(index = 1, name = "lxc arch", required = false, multiValued = false,
            description = "lxc arch, default = amd64")
    String lxcArch;

    private TemplateRegistryManager templateRegistryManager;


    public void setTemplateRegistryManager( final TemplateRegistryManager templateRegistryManager )
    {
        this.templateRegistryManager = templateRegistryManager;
    }


    @Override
    protected Object doExecute() throws Exception
    {
        List<Template> templates =
                Strings.isNullOrEmpty( lxcArch ) ? templateRegistryManager.getParentTemplates( childTemplateName ) :
                templateRegistryManager.getParentTemplates( childTemplateName, lxcArch );

        if ( !templates.isEmpty() )
        {
            for ( Template template : templates )
            {
                System.out.println( template );
            }
        }
        else
        {
            System.out.println( String.format( "Parent templates of %s not found", childTemplateName ) );
        }

        return null;
    }
}
