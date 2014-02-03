package org.overture.pog.assistant;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.expressions.ACaseAlternative;
import org.overture.ast.expressions.PExp;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.PType;
import org.overture.pog.obligation.POCaseContext;
import org.overture.pog.obligation.POContextStack;
import org.overture.pog.obligation.PONotCaseContext;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.typechecker.assistant.expression.ACaseAlternativeAssistantTC;

public class ACaseAlternativeAssistantPOG extends ACaseAlternativeAssistantTC
{

	protected static IPogAssistantFactory af;

	@SuppressWarnings("static-access")
	public ACaseAlternativeAssistantPOG(IPogAssistantFactory af)
	{
		super(af);
		this.af = af;
	}

	public ProofObligationList getProofObligations(
			ACaseAlternative node,
			QuestionAnswerAdaptor<POContextStack, ProofObligationList> rootVisitor,
			POContextStack question, PType type) throws AnalysisException
	{

		PPattern pattern = node.getPattern();
		PExp cexp = node.getCexp();

		ProofObligationList obligations = new ProofObligationList();
		question.push(new POCaseContext(pattern, type, cexp, af));
		obligations.addAll(node.getResult().apply(rootVisitor, question));
		question.pop();
		question.push(new PONotCaseContext(pattern, type, cexp, af));

		return obligations;
	}

}
