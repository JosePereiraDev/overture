package org.overture.interpreter.eval;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.lex.Dialect;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.statements.ANotYetSpecifiedStm;
import org.overture.config.Settings;
import org.overture.interpreter.assistant.definition.ACpuClassDefinitionAssistantInterpreter;
import org.overture.interpreter.debug.BreakpointManager;
import org.overture.interpreter.runtime.ClassInterpreter;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.Interpreter;
import org.overture.interpreter.runtime.ModuleInterpreter;
import org.overture.interpreter.runtime.VdmRuntimeError;
import org.overture.interpreter.runtime.VdmRuntime;
import org.overture.interpreter.runtime.state.AModuleModulesRuntime;
import org.overture.interpreter.runtime.state.SClassDefinitionRuntime;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.Value;

public class DelegateStatementEvaluator extends StatementEvaluator
{
	
	@Override
	public Value caseANotYetSpecifiedStm(ANotYetSpecifiedStm node,
			Context ctxt) throws AnalysisException
	{
		BreakpointManager.getBreakpoint(node).check(node.getLocation(), ctxt);

		if (Settings.dialect == Dialect.VDM_SL)
		{
			ModuleInterpreter i = (ModuleInterpreter)Interpreter.getInstance();
			AModuleModules module = i.findModule(node.getLocation().getModule());

			if (module != null)
			{
				AModuleModulesRuntime	state =VdmRuntime.getNodeState(module);
				if (state.hasDelegate())
				{
					return state.invokeDelegate(ctxt);
				}
			}
		}
		else
		{
    		ObjectValue self = ctxt.getSelf();

    		if (self == null)
    		{
    			ClassInterpreter i = (ClassInterpreter)Interpreter.getInstance();
    			SClassDefinition cls = i.findClass(node.getLocation().getModule());

    			if (cls != null)
    			{
    				SClassDefinitionRuntime state =VdmRuntime.getNodeState(ctxt.assistantFactory,cls);
    				if (state.hasDelegate())
    				{
    					return state.invokeDelegate(ctxt);
    				}
    			}
    		}
    		else
    		{
    			if (self.hasDelegate(ctxt))
    			{
    				return self.invokeDelegate(ctxt);
    			}
    		}
		}

		if (node.getLocation().getModule().equals("CPU"))
		{
    		if (ctxt.title.equals("deploy(obj)"))
    		{
    			return ACpuClassDefinitionAssistantInterpreter.deploy(node,ctxt);
    		}
    		else if (ctxt.title.equals("deploy(obj, name)"))
    		{
    			return ACpuClassDefinitionAssistantInterpreter.deploy(node,ctxt);
    		}
    		else if (ctxt.title.equals("setPriority(opname, priority)"))
    		{
    			return ACpuClassDefinitionAssistantInterpreter.setPriority(node,ctxt);
    		}
		}

		return VdmRuntimeError.abort(node.getLocation(),4041, "'is not yet specified' statement reached", ctxt);
	}
}
