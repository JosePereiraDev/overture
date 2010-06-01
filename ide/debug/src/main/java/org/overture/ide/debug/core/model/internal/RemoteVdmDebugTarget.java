package org.overture.ide.debug.core.model.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.model.IProcess;
import org.overture.ide.debug.core.VdmDebugPlugin;
import org.overture.ide.debug.core.IDbgpService;
import org.overture.ide.debug.core.IDebugOptions;
import org.overture.ide.debug.core.dbgp.internal.utils.Util;
import org.overture.ide.debug.core.model.IVdmBreakpointPathMapper;



public class RemoteVdmDebugTarget extends VdmDebugTarget {

	private static final String LAUNCH_CONFIGURATION_ATTR_REMOTE_WORKING_DIR = "remoteWorkingDir"; //$NON-NLS-1$
	private static final String LAUNCH_CONFIGURATION_ATTR_STRIP_SRC_FOLDERS = "stripSourceFolders"; //$NON-NLS-1$

	/**
	 * @param modelId
	 * @param dbgpService
	 * @param sessionId
	 * @param launch
	 * @param process
	 */
	public RemoteVdmDebugTarget(String modelId, IDbgpService dbgpService,
			String sessionId, ILaunch launch, IProcess process) {
		super(modelId, dbgpService, sessionId, launch, process);
	}

	/**
	 * @param modelId
	 * @param dbgpService
	 * @param sessionId
	 * @param launch
	 * @param process
	 * @param options
	 */
	public RemoteVdmDebugTarget(String modelId, IDbgpService dbgpService,
			String sessionId, ILaunch launch, IProcess process,
			IDebugOptions options) {
		super(modelId, dbgpService, sessionId, launch, process, options);
	}

	protected IVdmBreakpointPathMapper createPathMapper() {
		String remoteWorkingDir = null;
		boolean stripSrcFolders = false;

		try {
			remoteWorkingDir = getLaunch().getLaunchConfiguration()
					.getAttribute(LAUNCH_CONFIGURATION_ATTR_REMOTE_WORKING_DIR,
							Util.EMPTY_STRING);
		} catch (CoreException e) {
			VdmDebugPlugin.log(e);
		}

		try {
			stripSrcFolders = getLaunch().getLaunchConfiguration()
					.getAttribute(LAUNCH_CONFIGURATION_ATTR_STRIP_SRC_FOLDERS,
							false);
		} catch (CoreException e) {
			VdmDebugPlugin.log(e);
		}

		return new VdmBreakpointPathMapper(getVdmProject(),
				remoteWorkingDir, stripSrcFolders);
	}

	@Override
	public boolean isRemote() {
		return true;
	}

}
