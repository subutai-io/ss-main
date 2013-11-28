/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.cassandra.wizzard;

import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.*;

/**
 *
 * @author bahadyr
 */
public class Step41 extends Panel {

    public Step41(final CassandraWizard cassandraWizard) {
        setCaption("Configuration");
        setSizeFull();

        VerticalLayout verticalLayout = new VerticalLayout();
        verticalLayout.setSizeFull();
        verticalLayout.setHeight(600, Sizeable.UNITS_PIXELS);
        verticalLayout.setMargin(true);

        GridLayout grid = new GridLayout(6, 10);
        grid.setSpacing(true);
        grid.setSizeFull();

        Panel panel = new Panel();
        Label menu = new Label("Cluster Install Wizard<br>"
                + " 1) Welcome<br>"
                + " 2) List nodes<br>"
                + " 3) Installation<br>"
                + " 4) <font color=\"#f14c1a\"><strong>Configuration</strong></font><br>");
        menu.setContentMode(Label.CONTENT_XHTML);
        panel.addComponent(menu);

        grid.addComponent(menu, 0, 0, 1, 5);
        grid.setComponentAlignment(panel, Alignment.TOP_CENTER);

        final TextField clusterName = new TextField("Name your Cluster:");

        grid.addComponent(clusterName, 2, 0, 5, 1);
        grid.setComponentAlignment(clusterName, Alignment.MIDDLE_CENTER);

        Button next = new Button("Next");
        next.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                if (clusterName.getValue().toString().length() > 0) {
                    cassandraWizard.getCluster().setName(clusterName.getValue().toString());
                    cassandraWizard.showNext();
                } else {
                    getWindow().showNotification(
                            "Please provide cluster name.",
                            Window.Notification.TYPE_TRAY_NOTIFICATION);
                }
            }
        });
        Button back = new Button("Back");
        back.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                cassandraWizard.showBack();
            }
        });

        HorizontalLayout horizontalLayout = new HorizontalLayout();
        horizontalLayout.addComponent(back);
        horizontalLayout.addComponent(next);

        verticalLayout.addComponent(grid);
        verticalLayout.addComponent(horizontalLayout);

        addComponent(verticalLayout);
    }

}
