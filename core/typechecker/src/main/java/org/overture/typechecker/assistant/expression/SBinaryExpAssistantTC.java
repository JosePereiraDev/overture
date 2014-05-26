package org.overture.typechecker.assistant.expression;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.expressions.SBooleanBinaryExp;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class SBinaryExpAssistantTC
{

	protected ITypeCheckerAssistantFactory af;

	public SBinaryExpAssistantTC(ITypeCheckerAssistantFactory af)
	{
		this.af = af;
	}

	public ABooleanBasicType binaryCheck(SBooleanBinaryExp node,
			ABooleanBasicType expected,
			IQuestionAnswer<TypeCheckInfo, PType> rootVisitor,
			TypeCheckInfo question) throws AnalysisException
	{

		node.getLeft().apply(rootVisitor, question);
		node.getRight().apply(rootVisitor, question);

		if (!af.createPTypeAssistant().isType(node.getLeft().getType(), expected.getClass()))
		{
			TypeCheckerErrors.report(3065, "Left hand of " + node.getOp()
					+ " is not " + expected, node.getLocation(), node);
		}

		if (!af.createPTypeAssistant().isType(node.getRight().getType(), expected.getClass()))
		{
			TypeCheckerErrors.report(3066, "Right hand of " + node.getOp()
					+ " is not " + expected, node.getLocation(), node);
		}

		node.setType(expected);
		return (ABooleanBasicType) node.getType();

	}

}
