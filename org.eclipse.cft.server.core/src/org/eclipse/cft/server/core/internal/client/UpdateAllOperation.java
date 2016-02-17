/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc. 
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Apache License v2.0 which accompanies this distribution. 
 * 
 * The Eclipse Public License is available at 
 * 
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * and the Apache License v2.0 is available at 
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * You may elect to redistribute this code under either of these licenses.
 *  
 *  Contributors:
 *     Pivotal Software, Inc. - initial API and implementation
 ********************************************************************************/
package org.eclipse.cft.server.core.internal.client;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.cloudfoundry.client.lib.domain.ApplicationStats;
import org.cloudfoundry.client.lib.domain.CloudApplication;
import org.cloudfoundry.client.lib.domain.CloudService;
import org.eclipse.cft.server.core.internal.CloudFoundryServer;
import org.eclipse.cft.server.core.internal.CloudServerEvent;
import org.eclipse.cft.server.core.internal.Messages;
import org.eclipse.cft.server.core.internal.ServerEventHandler;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.wst.server.core.IModule;
import org.eclipse.wst.server.core.IServer;

/**
 * Updates all modules and services in the server
 *
 */
public class UpdateAllOperation extends BehaviourOperation {

	public UpdateAllOperation(CloudFoundryServerBehaviour behaviour) {
		super(behaviour, null);
	}

	@Override
	public void run(IProgressMonitor monitor) throws CoreException {

		CloudFoundryServer cloudServer = getBehaviour().getCloudFoundryServer();
		SubMonitor subMonitor = SubMonitor.convert(monitor);
		subMonitor.beginTask(NLS.bind(Messages.CloudBehaviourOperations_REFRESHING_APPS_AND_SERVICES,
				cloudServer.getServer().getId()), 100);

		// Get updated list of cloud applications from the server
		List<CloudApplication> applications = getBehaviour().getApplications(subMonitor.newChild(50));

		// update applications and deployments from server
		Map<String, CloudApplication> deployedApplicationsByName = new LinkedHashMap<String, CloudApplication>();
		Map<String, ApplicationStats> stats = new LinkedHashMap<String, ApplicationStats>();

		for (CloudApplication application : applications) {
			ApplicationStats sts = getBehaviour().getApplicationStats(application.getName(), subMonitor);
			stats.put(application.getName(), sts);
			deployedApplicationsByName.put(application.getName(), application);
		}

		cloudServer.addAndDeleteModules(deployedApplicationsByName, stats);

		// Skip modules that are starting
		cloudServer.updateModulesState(new int[] { IServer.STATE_STARTING });

		// Clear publish error
		for (IModule module : cloudServer.getServer().getModules()) {
			CloudFoundryApplicationModule appModule = cloudServer.getExistingCloudModule(module);
			if (appModule != null) {
				appModule.setStatus(null);
				appModule.validateDeploymentInfo();
			}
		}

		List<CloudService> services = getBehaviour().getServices(subMonitor.newChild(20));

		ServerEventHandler.getDefault().fireServerEvent(new CloudRefreshEvent(getBehaviour().getCloudFoundryServer(),
				getModule(), CloudServerEvent.EVENT_SERVER_REFRESHED, services));

		subMonitor.worked(20);

	}

}
