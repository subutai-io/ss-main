/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.safehaus.kiskis.mgmt.impl.cassandra;

import org.safehaus.kiskis.mgmt.api.commandrunner.Command;
import org.safehaus.kiskis.mgmt.api.commandrunner.CommandsSingleton;
import org.safehaus.kiskis.mgmt.api.commandrunner.RequestBuilder;
import org.safehaus.kiskis.mgmt.shared.protocol.CommandFactory;
import org.safehaus.kiskis.mgmt.shared.protocol.Request;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.OutputRedirection;
import org.safehaus.kiskis.mgmt.shared.protocol.enums.RequestType;
import org.safehaus.kiskis.mgmt.shared.protocol.Agent;

import java.util.Iterator;
import java.util.Set;

/**
 * @author dilshat
 */
public class Commands extends CommandsSingleton {

    private static final String cassandraYamlConf = "$CASSANDRA_HOME/conf/cassandra.yaml";

    public static Request getRequestTemplate() {
        return CommandFactory.newRequest(
                RequestType.EXECUTE_REQUEST, // type
                null, //                        !! agent uuid
                CassandraImpl.MODULE_NAME, //     source
                null, //                        !! task uuid 
                1, //                           !! request sequence number
                "/", //                         cwd
                "pwd", //                        program
                OutputRedirection.RETURN, //    std output redirection 
                OutputRedirection.RETURN, //    std error redirection
                null, //                        stdout capture file path
                null, //                        stderr capture file path
                "root", //                      runas
                null, //                        arg
                null, //                        env vars
                30); //  
    }

    public static Request getUpdateAptCommand() {
        Request req = getRequestTemplate();
        req.setProgram("sleep 10; apt-get update");
        req.setStdOut(OutputRedirection.RETURN);
        req.setTimeout(90);
        return req;
    }

    public static Request getInstallCommand() {
        Request req = getRequestTemplate();
        req.setProgram("sleep 10; apt-get --force-yes --assume-yes install ksks-cassandra");
        req.setStdOut(OutputRedirection.RETURN);
        req.setTimeout(90);
        return req;
    }

    public static Request getStartCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service cassandra start");
        req.setStdOut(OutputRedirection.NO);
        return req;
    }

    public static Request getStopCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service cassandra stop");
        return req;
    }

    public static Request getStatusCommand() {
        Request req = getRequestTemplate();
        req.setProgram("service cassandra status");
        return req;
    }

    public static Request getConfigureCommand(String param) {
        Request req = getRequestTemplate();
        String rpcAddressSed = ". /etc/profile && $CASSANDRA_HOME/bin/cassandra-conf.sh " + param;
        req.setProgram(rpcAddressSed);
        return req;
    }

    public static Command getInstallCommand(Set<Agent> agents) {
//        AgentRequestBuilder ar = new AgentRequestBuilder(agent, "ls");
//        Set<AgentRequestBuilder> setAr;
//        createCommand(setAr);

        return createCommand(
                new RequestBuilder(
                        "sleep 10; apt-get --force-yes --assume-yes install ksks-cassandra")
                        .withTimeout(90).withStdOutRedirection(OutputRedirection.NO),
                agents
        );

    }

    public static Command getStartCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra start")
                ,
                agents
        );
    }

    public static Command getStopCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra stop")
                ,
                agents
        );
    }

    public static Command getStatusCommand(Set<Agent> agents) {
        return createCommand(
                new RequestBuilder(
                        "service cassandra status")
                ,
                agents
        );
    }

    public static Command getConfigureCommand(Set<Agent> agents, String param) {

        return createCommand(
                new RequestBuilder(
                        String.format("sh cassandra-conf.sh %s", param))
                ,
                agents
        );
    }
}
