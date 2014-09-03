package org.safehaus.subutai.impl.pig;


import com.google.common.base.Preconditions;
import org.safehaus.subutai.core.agent.api.AgentManager;
import org.safehaus.subutai.core.command.api.CommandRunner;
import org.safehaus.subutai.core.db.api.DbManager;
import org.safehaus.subutai.api.pig.Config;
import org.safehaus.subutai.api.pig.Pig;
import org.safehaus.subutai.core.tracker.api.Tracker;
import org.safehaus.subutai.impl.pig.handler.DestroyNodeOperationHandler;
import org.safehaus.subutai.impl.pig.handler.InstallOperationHandler;
import org.safehaus.subutai.common.protocol.AbstractOperationHandler;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


public class PigImpl implements Pig {

	protected Commands commands;
	private CommandRunner commandRunner;
	private AgentManager agentManager;
	private DbManager dbManager;
	private Tracker tracker;
	private ExecutorService executor;


	public PigImpl(CommandRunner commandRunner, AgentManager agentManager, DbManager dbManager, Tracker tracker) {
		this.commands = new Commands(commandRunner);
		this.commandRunner = commandRunner;
		this.agentManager = agentManager;
		this.dbManager = dbManager;
		this.tracker = tracker;
	}


	public void init() {
		executor = Executors.newCachedThreadPool();
	}


	public void destroy() {
		executor.shutdown();
	}


	public Commands getCommands() {
		return commands;
	}


	public AgentManager getAgentManager() {
		return agentManager;
	}


	public DbManager getDbManager() {
		return dbManager;
	}


	public Tracker getTracker() {
		return tracker;
	}


	public CommandRunner getCommandRunner() {
		return commandRunner;
	}


	@Override
	public UUID installCluster(Config config) {

		Preconditions.checkNotNull(config, "Configuration is null");

		AbstractOperationHandler operationHandler = new InstallOperationHandler(this, config);
		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}


	@Override
	public UUID uninstallCluster(final String clusterName) {
		return null;
	}

	@Override
	public List<Config> getClusters() {
		return dbManager.getInfo(Config.PRODUCT_KEY, Config.class);
	}

	@Override
	public Config getCluster(String clusterName) {
		return dbManager.getInfo(Config.PRODUCT_KEY, clusterName, Config.class);
	}

	@Override
	public UUID destroyNode(final String clusterName, final String lxcHostname) {

		AbstractOperationHandler operationHandler = new DestroyNodeOperationHandler(this, clusterName, lxcHostname);

		executor.execute(operationHandler);

		return operationHandler.getTrackerId();
	}
}
