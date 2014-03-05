/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 *//*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


package org.safehaus.kiskis.mgmt.server.ui.modules.oozie.wizard.exec;

import java.util.LinkedList;
import java.util.Queue;
import java.util.Set;
import org.safehaus.kiskis.mgmt.api.taskrunner.TaskCallback;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.OozieModule;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.commands.OozieCommands;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieCommandEnum;
import org.safehaus.kiskis.mgmt.server.ui.modules.oozie.management.OozieTable;
import org.safehaus.kiskis.mgmt.shared.protocol.*;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.TaskStatus;

/**
 *
 * @author bahadyr
 */
public class ServiceManager {

    private final Queue<Task> tasks = new LinkedList<Task>();
    private Task currentTask;
    private final OozieTable oozieTable;

    public ServiceManager(OozieTable oozieTable) {
        this.oozieTable = oozieTable;
    }

    public void runCommand(Set<Agent> agents, OozieCommandEnum cce) {
        Task startTask = new Task("Run command");
        for (Agent agent : agents) {
            Request command = new OozieCommands().getCommand(cce);
            command.setUuid(agent.getUuid());
            startTask.addRequest(command);
        }
        tasks.add(startTask);
        start();
    }

    public void runCommand(Agent agent, OozieCommandEnum cce) {
        Task startTask = new Task("Run command");
        Request command = new OozieCommands().getCommand(cce);
        command.setUuid(agent.getUuid());
        startTask.addRequest(command);
        tasks.add(startTask);
        start();
    }

    public void start() {
        moveToNextTask();
        if (currentTask != null) {

            OozieModule.getTaskRunner().executeTask(currentTask, new TaskCallback() {

                @Override
                public Task onResponse(Task task, Response response, String stdOut, String stdErr) {
                    if (task.getTaskStatus() == TaskStatus.SUCCESS) {
                        moveToNextTask();
                        if (currentTask != null) {
                            return currentTask;
                        } else {
                            oozieTable.manageUI(task.getTaskStatus());
                        }
                    }
                    return null;
                }
            });
        }
    }

    public void moveToNextTask() {
        currentTask = tasks.poll();
    }

    public Task getCurrentTask() {
        return currentTask;
    }

}
