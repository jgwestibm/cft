/*******************************************************************************
 * Copyright (c) 2016 Pivotal Software, Inc. and others 
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

import java.util.List;

import org.eclipse.cft.server.core.internal.log.CFApplicationLogListener;
import org.eclipse.cft.server.core.internal.log.CFStreamingLogToken;
import org.eclipse.cft.server.core.internal.log.CloudLog;
import org.eclipse.core.runtime.CoreException;

/**
 * NOTE: Internal use only. This API is changing and is used for internal v2 CF Java client contribution into CFT.
 *
 */
public interface CFClient {

	/**
	 * 
	 * @throws CoreException if failed to login.
	 */
	public String login() throws CoreException;

	public CFStreamingLogToken streamLogs(String appName, CFApplicationLogListener listener) throws CoreException;

	public List<CloudLog> getRecentLogs(String appName) throws CoreException;

}
