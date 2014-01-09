/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.server.ui.modules.mongo.manage;

import com.vaadin.data.Item;
import org.safehaus.kiskis.mgmt.shared.protocol.ExpiringCache;
import com.vaadin.data.Property;
import com.vaadin.terminal.Sizeable;
import com.vaadin.ui.Alignment;
import com.vaadin.ui.Button;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Embedded;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.VerticalLayout;
import java.text.MessageFormat;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.MongoModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Commands;
import org.safehaus.kiskis.mgmt.server.ui.modules.mongo.common.Constants;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;
import org.safehaus.kiskis.mgmt.shared.protocol.Command;
import org.safehaus.kiskis.mgmt.shared.protocol.MongoClusterInfo;
import org.safehaus.kiskis.mgmt.shared.protocol.Response;
import org.safehaus.kiskis.mgmt.shared.protocol.ServiceLocator;
import org.safehaus.kiskis.mgmt.shared.protocol.Task;
import org.safehaus.kiskis.mgmt.shared.protocol.Util;
import org.safehaus.kiskis.mgmt.shared.protocol.api.AgentManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.CommandManagerInterface;
import org.safehaus.kiskis.mgmt.shared.protocol.api.ResponseListener;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author dilshat
 */
public class Manager implements ResponseListener {

    private static final Logger LOG = Logger.getLogger(Manager.class.getName());

    private final VerticalLayout contentRoot;
    private final CommandManagerInterface commandManager;
    private final AgentManagerInterface agentManager;
    private final ComboBox clusterCombo;
    private final ExpiringCache<UUID, ManagerAction> actionsCache = new ExpiringCache<UUID, ManagerAction>();

    public Manager() {
        //get db and transport managers
        agentManager = ServiceLocator.getService(AgentManagerInterface.class);
        commandManager = ServiceLocator.getService(CommandManagerInterface.class);

        contentRoot = new VerticalLayout();
        contentRoot.setSpacing(true);
        contentRoot.setWidth(90, Sizeable.UNITS_PERCENTAGE);
        contentRoot.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        VerticalLayout content = new VerticalLayout();
        content.setSpacing(true);
        content.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        content.setHeight(100, Sizeable.UNITS_PERCENTAGE);

        contentRoot.addComponent(content);
        contentRoot.setComponentAlignment(content, Alignment.TOP_CENTER);
        contentRoot.setMargin(true);

        //tables go here
        final Table configServersTable = createTableTemplate("Config Servers");
        final Table routersTable = createTableTemplate("Query Routers");
        final Table dataNodesTable = createTableTemplate("Data Nodes");
        //tables go here

        Label clusterNameLabel = new Label("Select the cluster");
        content.addComponent(clusterNameLabel);

        HorizontalLayout topContent = new HorizontalLayout();
        topContent.setSpacing(true);

        clusterCombo = new ComboBox();
        clusterCombo.setMultiSelect(false);
        clusterCombo.setImmediate(true);
        clusterCombo.setTextInputAllowed(false);
        clusterCombo.setWidth(300, Sizeable.UNITS_PIXELS);
        clusterCombo.addListener(new Property.ValueChangeListener() {

            @Override
            public void valueChange(Property.ValueChangeEvent event) {
                if (event.getProperty().getValue() instanceof MongoClusterInfo) {
                    MongoClusterInfo clusterInfo = (MongoClusterInfo) event.getProperty().getValue();
                    populateTable(configServersTable, clusterInfo.getConfigServers(), NodeType.CONFIG_NODE);
                    populateTable(routersTable, clusterInfo.getRouters(), NodeType.ROUTER_NODE);
                    populateTable(dataNodesTable, clusterInfo.getDataNodes(), NodeType.DATA_NODE);
                    actionsCache.clear();
                }
            }
        });

        topContent.addComponent(clusterCombo);

        Button refreshClustersBtn = new Button("Refresh clusters");
        refreshClustersBtn.addListener(new Button.ClickListener() {

            @Override
            public void buttonClick(Button.ClickEvent event) {
                refreshClustersInfo();
            }
        });

        topContent.addComponent(refreshClustersBtn);

        content.addComponent(topContent);

        HorizontalLayout midContent = new HorizontalLayout();
        midContent.setWidth(100, Sizeable.UNITS_PERCENTAGE);

        midContent.addComponent(configServersTable);

        midContent.addComponent(routersTable);

        content.addComponent(midContent);

        content.addComponent(dataNodesTable);

        refreshClustersInfo();
    }

    public Component getContent() {
        return contentRoot;
    }

    private void executeManagerAction(ManagerActionType managerActionType, Agent agent, NodeType nodeType, Item row) {

        if (managerActionType == ManagerActionType.CHECK_NODE_STATUS) {

            Task checkTask = Util.createTask("Check mongo node status");
            Command checkCommand = Commands.getCheckInstanceRunningCommand(
                    MessageFormat.format("{0}{1}", agent.getHostname(), Constants.DOMAIN),
                    getNodePort(nodeType));
            checkCommand.getRequest().setUuid(agent.getUuid());
            checkCommand.getRequest().setTaskUuid(checkTask.getUuid());
            checkCommand.getRequest().setRequestSequenceNumber(checkTask.getIncrementedReqSeqNumber());
            if (commandManager.executeCommand(checkCommand)) {
                actionsCache.put(checkTask.getUuid(),
                        new ManagerAction(
                                checkTask, managerActionType,
                                row, agent, nodeType),
                        checkCommand.getRequest().getTimeout() * 1000 + 2000);
            }
        } else if (managerActionType == ManagerActionType.STOP_NODE) {

            Task stopTask = Util.createTask("Stop mongo node");
            Command stopCommand = Commands.getStopNodeCommand();
            stopCommand.getRequest().setUuid(agent.getUuid());
            stopCommand.getRequest().setTaskUuid(stopTask.getUuid());
            stopCommand.getRequest().setRequestSequenceNumber(stopTask.getIncrementedReqSeqNumber());
            if (commandManager.executeCommand(stopCommand)) {
                ManagerAction managerAction = new ManagerAction(
                        stopTask, managerActionType,
                        row, agent, nodeType);
                managerAction.disableStartStopButtons();
                actionsCache.put(stopTask.getUuid(),
                        managerAction,
                        stopCommand.getRequest().getTimeout() * 1000 + 2000);

            }
        } else if (managerActionType == ManagerActionType.START_NODE) {

            if (nodeType == NodeType.DATA_NODE) {

            } else if (nodeType == NodeType.CONFIG_NODE) {
                Task startConfigSvrTask = Util.createTask("Start config server");
                Command startConfigSvrCommand = Commands.getStartConfigServerCommand();
                startConfigSvrCommand.getRequest().setUuid(agent.getUuid());
                startConfigSvrCommand.getRequest().setTaskUuid(startConfigSvrTask.getUuid());
                startConfigSvrCommand.getRequest().setRequestSequenceNumber(startConfigSvrTask.getIncrementedReqSeqNumber());
                if (commandManager.executeCommand(startConfigSvrCommand)) {
                    ManagerAction managerAction = new ManagerAction(
                            startConfigSvrTask,
                            managerActionType,
                            row, agent, nodeType);
                    managerAction.disableStartStopButtons();
                    actionsCache.put(startConfigSvrTask.getUuid(),
                            managerAction,
                            startConfigSvrCommand.getRequest().getTimeout() * 1000 + 2000);
                }
            } else if (nodeType == NodeType.ROUTER_NODE) {

            }
        }
    }

    @Override
    public void onResponse(Response response) {
        try {

            if (response != null && response.getTaskUuid() != null) {
                ManagerAction managerAction = actionsCache.get(response.getTaskUuid());
                if (managerAction != null) {
                    boolean actionCompleted = false;
                    managerAction.addOutput(response.getStdOut());
                    if (managerAction.getManagerActionType() == ManagerActionType.CHECK_NODE_STATUS) {
                        if (managerAction.getOutput().contains("couldn't connect to server")) {
                            managerAction.enableStartButton();
                            actionCompleted = true;
                        } else if (managerAction.getOutput().
                                contains("connecting to")) {
                            managerAction.enableStopButton();
                            actionCompleted = true;
                        } else if (managerAction.getOutput().contains("mongo: not found")) {
                            actionCompleted = true;
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.START_NODE) {
                        if (managerAction.getOutput().contains("child process started successfully, parent exiting")) {
                            actionCompleted = true;
                            //add check command to actualize buttons
                            executeManagerAction(ManagerActionType.CHECK_NODE_STATUS,
                                    managerAction.getAgent(), managerAction.getNodeType(),
                                    managerAction.getRow());
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.STOP_NODE) {
                        if (Util.isFinalResponse(response)) {
                            //add check command to actualize buttons
                            executeManagerAction(ManagerActionType.CHECK_NODE_STATUS,
                                    managerAction.getAgent(), managerAction.getNodeType(),
                                    managerAction.getRow());
                        }
                    } else if (managerAction.getManagerActionType() == ManagerActionType.DESTROY_NODE) {
                    }
                    if (actionCompleted || Util.isFinalResponse(response)) {

                        Task task = managerAction.getTask();
                        task.setTaskStatus(actionCompleted ? TaskStatus.SUCCESS : TaskStatus.FAIL);
                        Util.saveTask(task);
                        actionsCache.remove(managerAction.getTask().getUuid());
                        managerAction.hideProgress();
                    }
                }
            }
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Error in onResponse", e);
        }
    }

    @Override
    public String getSource() {
        return MongoModule.MODULE_NAME;
    }

    private void show(String notification) {
        contentRoot.getWindow().showNotification(notification);
    }

    private void populateTable(final Table table, List<UUID> agentUUIDs, final NodeType nodeType) {

        table.removeAllItems();

        for (UUID agentUUID : agentUUIDs) {

            final Agent agent = agentManager.getAgent(agentUUID);
            Button checkBtn = new Button("Check");
            Button startBtn = new Button("Start");
            Button stopBtn = new Button("Stop");
            Button destroyBtn = new Button("Destroy");
            stopBtn.setEnabled(false);
            startBtn.setEnabled(false);

            Object rowId = table.addItem(new Object[]{
                agent.getHostname(),
                checkBtn,
                startBtn,
                stopBtn,
                destroyBtn,
                null},
                    null);

            final Item row = table.getItem(rowId);

            startBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.START_NODE, agent, nodeType, row);
                }
            });
            stopBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.STOP_NODE, agent, nodeType, row);
                }
            });
            destroyBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                }
            });

            checkBtn.addListener(new Button.ClickListener() {

                @Override
                public void buttonClick(Button.ClickEvent event) {
                    executeManagerAction(ManagerActionType.CHECK_NODE_STATUS, agent, nodeType, row);
                }
            });
        }
    }

    private Table createTableTemplate(String caption) {
        Table table = new Table(caption);
        table.addContainerProperty(Constants.TABLE_HOST_PROPERTY, String.class, null);
        table.addContainerProperty(Constants.TABLE_CHECK_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_START_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_STOP_PROPERTY, Button.class, null);
        table.addContainerProperty(Constants.TABLE_DESTROY_PROPERTY, Button.class, null);
        table.addContainerProperty("Status", Embedded.class, null);
        table.setWidth(100, Sizeable.UNITS_PERCENTAGE);
        table.setHeight(250, Sizeable.UNITS_PIXELS);
        table.setPageLength(10);
        table.setSelectable(false);
        table.setImmediate(true);
        return table;
    }

    private void refreshClustersInfo() {
        List<MongoClusterInfo> mongoClusterInfos = commandManager.getMongoClustersInfo();
        clusterCombo.removeAllItems();
        if (mongoClusterInfos != null) {
            for (MongoClusterInfo clusterInfo : mongoClusterInfos) {
                clusterCombo.addItem(clusterInfo);
                clusterCombo.setItemCaption(clusterInfo,
                        String.format("Name: %s RS: %s", clusterInfo.getClusterName(), clusterInfo.getReplicaSetName()));
            }
        }
    }

    private String getNodePort(NodeType nodeType) {
        if (nodeType == NodeType.CONFIG_NODE) {
            return Constants.CONFIG_SRV_PORT + "";
        } else if (nodeType == NodeType.ROUTER_NODE) {
            return Constants.ROUTER_PORT + "";
        } else {
            return Constants.DATA_NODE_PORT + "";
        }
    }

}
