package org.overture.pog.assistant;

import java.util.LinkedList;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.PDefinition;
import org.overture.pog.obligation.POContextStack;
import org.overture.pog.obligation.PONameContext;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;
import org.overture.typechecker.assistant.definition.PDefinitionAssistantTC;

public class PDefinitionAssistantPOG extends PDefinitionAssistantTC
{

	public PDefinitionAssistantPOG(ITypeCheckerAssistantFactory af)
	{
		super(af);
	}

	public ProofObligationList getProofObligations(
			LinkedList<? extends PDefinition> defs,
			QuestionAnswerAdaptor<POContextStack, ProofObligationList> pogVisitor,
			POContextStack ctxt) throws AnalysisException
	{
		ProofObligationList obligations = new ProofObligationList();

		for (PDefinition d : defs)
		{
			ctxt.push(new PONameContext(getVariableNames(d)));
			obligations.addAll(d.apply(pogVisitor, ctxt));
			ctxt.pop();
		}

		return obligations;
	}

}
