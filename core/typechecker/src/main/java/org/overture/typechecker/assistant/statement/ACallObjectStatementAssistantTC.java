package org.overture.typechecker.assistant.statement;

import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.expressions.PExp;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class ACallObjectStatementAssistantTC
{
	protected static ITypeCheckerAssistantFactory af;

	@SuppressWarnings("static-access")
	public ACallObjectStatementAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public static List<PType> getArgTypes(LinkedList<PExp> args,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{
		List<PType> types = new LinkedList<PType>();

		for (PExp e : args)
		{
			types.add(e.apply(rootVisitor, question));
		}

		return types;
	}

	public static void checkArgTypes(PType type, List<PType> ptypes,
			List<PType> atypes)
	{
		if (ptypes.size() != atypes.size())
		{
			TypeCheckerErrors.report(3211, "Expecting " + ptypes.size()
					+ " arguments", type.getLocation(), type);
		} else
		{
			int i = 0;

			for (PType atype : atypes)
			{
				PType ptype = ptypes.get(i++);

				if (!TypeComparator.compatible(ptype, atype))
				{
					TypeCheckerErrors.report(3212, "Unexpected type for argument "
							+ i, atype.getLocation(), atype);
					TypeCheckerErrors.detail2("Expected", ptype, "Actual", atype);
				}
			}
		}
	}

}
