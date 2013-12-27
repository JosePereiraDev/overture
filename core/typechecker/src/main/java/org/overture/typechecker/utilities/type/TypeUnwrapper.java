package org.overture.typechecker.utilities.type;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.AnswerAdaptor;
import org.overture.ast.node.INode;
import org.overture.ast.types.ABracketType;
import org.overture.ast.types.AOptionalType;

public abstract class TypeUnwrapper<A> extends AnswerAdaptor<A>
{

	@Override
	public A caseABracketType(ABracketType node) throws AnalysisException
	{
		return node.getType().apply(THIS);
	}

	@Override
	public A caseAOptionalType(AOptionalType node) throws AnalysisException
	{
		return node.getType().apply(THIS);
	}

	@Override
	public A createNewReturnValue(INode node)
	{
		assert false : "should not happen";
		return null;
	}

	@Override
	public A createNewReturnValue(Object node)
	{
		assert false : "should not happen";
		return null;
	}
}
