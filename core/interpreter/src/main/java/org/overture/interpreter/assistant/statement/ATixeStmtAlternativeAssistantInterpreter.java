package org.overture.interpreter.assistant.statement;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.assistant.IAstAssistant;
import org.overture.ast.intf.lex.ILexLocation;
import org.overture.ast.patterns.ASetBind;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.statements.ATixeStmtAlternative;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.runtime.PatternMatchException;
import org.overture.interpreter.runtime.ValueException;
import org.overture.interpreter.runtime.VdmRuntime;
import org.overture.interpreter.runtime.VdmRuntimeError;
import org.overture.interpreter.values.Value;
import org.overture.interpreter.values.ValueSet;

public class ATixeStmtAlternativeAssistantInterpreter implements IAstAssistant
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public ATixeStmtAlternativeAssistantInterpreter(
			IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

	//FIXME only used once. inline it
	public Value eval(ATixeStmtAlternative node, ILexLocation location,
			Value exval, Context ctxt) throws AnalysisException
	{
		Context evalContext = null;

		try
		{
			if (node.getPatternBind().getPattern() != null)
			{
				evalContext = new Context(af, location, "tixe pattern", ctxt);
				evalContext.putList(af.createPPatternAssistant().getNamedValues(node.getPatternBind().getPattern(), exval, ctxt));
			} else if (node.getPatternBind().getBind() instanceof ASetBind)
			{
				ASetBind setbind = (ASetBind) node.getPatternBind().getBind();
				ValueSet set = setbind.getSet().apply(VdmRuntime.getStatementEvaluator(), ctxt).setValue(ctxt);

				if (set.contains(exval))
				{
					evalContext = new Context(af, location, "tixe set", ctxt);
					evalContext.putList(af.createPPatternAssistant().getNamedValues(setbind.getPattern(), exval, ctxt));
				} else
				{
					VdmRuntimeError.abort(setbind.getLocation(), 4049, "Value "
							+ exval + " is not in set bind", ctxt);
				}
			} else
			{
				ATypeBind typebind = (ATypeBind) node.getPatternBind().getBind();
				// Note we always perform DTC checks here...
				Value converted = exval.convertValueTo(typebind.getType(), ctxt);
				evalContext = new Context(af, location, "tixe type", ctxt);
				evalContext.putList(af.createPPatternAssistant().getNamedValues(typebind.getPattern(), converted, ctxt));
			}
		} catch (ValueException ve) // Type bind convert failure
		{
			evalContext = null;
		} catch (PatternMatchException e)
		{
			evalContext = null;
		}

		return evalContext == null ? null
				: node.getStatement().apply(VdmRuntime.getStatementEvaluator(), evalContext);
	}

}
