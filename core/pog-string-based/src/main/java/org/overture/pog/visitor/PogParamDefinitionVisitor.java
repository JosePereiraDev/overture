package org.overture.pog.visitor;

import java.util.LinkedList;
import java.util.List;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.QuestionAnswerAdaptor;
import org.overture.ast.definitions.AAssignmentDefinition;
import org.overture.ast.definitions.AClassClassDefinition;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.AEqualsDefinition;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.APerSyncDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.definitions.traces.PTraceCoreDefinition;
import org.overture.ast.definitions.traces.PTraceDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.AIdentifierPattern;
import org.overture.ast.patterns.AIgnorePattern;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.PType;
import org.overture.ast.util.PTypeSet;
import org.overture.pog.obligation.FuncPostConditionObligation;
import org.overture.pog.obligation.OperationPostConditionObligation;
import org.overture.pog.obligation.POContextStack;
import org.overture.pog.obligation.POFunctionDefinitionContext;
import org.overture.pog.obligation.POFunctionResultContext;
import org.overture.pog.obligation.PONameContext;
import org.overture.pog.obligation.POOperationDefinitionContext;
import org.overture.pog.obligation.ParameterPatternObligation;
import org.overture.pog.obligation.ProofObligationList;
import org.overture.pog.obligation.SatisfiabilityObligation;
import org.overture.pog.obligation.StateInvariantObligation;
import org.overture.pog.obligation.SubTypeObligation;
import org.overture.pog.obligation.ValueBindingObligation;
import org.overture.pog.util.POException;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.definition.PDefinitionAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternListAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

public class PogParamDefinitionVisitor<Q extends POContextStack, A extends ProofObligationList>
		extends QuestionAnswerAdaptor<POContextStack, ProofObligationList>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -3086193431700309588L;
	final private QuestionAnswerAdaptor<POContextStack, ProofObligationList> rootVisitor;
	final private QuestionAnswerAdaptor<POContextStack, ProofObligationList> mainVisitor;

	public PogParamDefinitionVisitor(
			QuestionAnswerAdaptor<POContextStack, ProofObligationList> parentVisitor,
			QuestionAnswerAdaptor<POContextStack, ProofObligationList> mainVisitor)
	{
		this.rootVisitor = parentVisitor;
		this.mainVisitor = mainVisitor;
	}

	public PogParamDefinitionVisitor(
			QuestionAnswerAdaptor<POContextStack, ProofObligationList> parentVisitor)
	{
		this.rootVisitor = parentVisitor;
		this.mainVisitor = this;
	}

	@Override
	// from [1] pg. 35 we have an:
	// explicit function definition = identifier,
	// [ type variable list ], �:�, function type,
	// identifier, parameters list, �==�,
	// function body,
	// [ �pre�, expression ],
	// [ �post�, expression ],
	// [ �measure�, name ] ;
	public ProofObligationList caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();
			LexNameList pids = new LexNameList();
			boolean matchNeeded = false; 

			// add all defined names from the function parameter list
			for (List<PPattern> patterns : node.getParamPatternList())
			{
				for (PPattern p : patterns)
					for (PDefinition def : p.getDefinitions())
						pids.add(def.getName());
				
				if (!PPatternListAssistantTC.alwaysMatches(patterns))
				{
					matchNeeded = true;
				}
			}

			// check for duplicates
			if (pids.hasDuplicates() || matchNeeded)
			{
				obligations.add(new ParameterPatternObligation(node, question));
			}

			// do proof obligations for the pre-condition
			PExp precondition = node.getPrecondition();
			if (precondition != null)
			{
				question.push(new POFunctionDefinitionContext(node, false));
				obligations.addAll(precondition.apply(rootVisitor, question));
				question.pop();
			}

			// do proof obligations for the post-condition
			PExp postcondition = node.getPostcondition();
			if (postcondition != null)
			{
				question.push(new POFunctionDefinitionContext(node, false));
				obligations.add(new FuncPostConditionObligation(node, question));
				question.push(new POFunctionResultContext(node));
				obligations.addAll(postcondition.apply(rootVisitor, question));
				question.pop();
				question.pop();
			}

			// do proof obligations for the function body

			question.push(new POFunctionDefinitionContext(node, true));
			PExp body = node.getBody();
			int sizeBefore = question.size();
			obligations.addAll(body.apply(rootVisitor, question));
			assert sizeBefore <= question.size();

			// do proof obligation for the return type
			if (node.getIsUndefined()
					|| !TypeComparator.isSubType(node.getActualResult(), node.getExpectedResult()))
			{
				obligations.add(new SubTypeObligation(node, node.getExpectedResult(), node.getActualResult(), question));
			}
			question.pop();

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList defaultSClassDefinition(SClassDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			ProofObligationList proofObligationList = new ProofObligationList();

			for (PDefinition def : node.getDefinitions())
			{
				proofObligationList.addAll(def.apply(mainVisitor, question));
			}
			return proofObligationList;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAClassInvariantDefinition(
			AClassInvariantDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList list = new ProofObligationList();

			if (!node.getClassDefinition().getHasContructors())
			{
				list.add(new StateInvariantObligation(node, question));
			}

			return list;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAEqualsDefinition(AEqualsDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			ProofObligationList list = new ProofObligationList();

			PPattern pattern = node.getPattern();
			if (pattern != null)
			{
				if (!(pattern instanceof AIdentifierPattern)
						&& !(pattern instanceof AIgnorePattern)
						&& node.getExpType() instanceof AUnionType)
				{
					PType patternType = PPatternAssistantTC.getPossibleType(pattern); // With unknowns
					AUnionType ut = (AUnionType) node.getExpType();
					PTypeSet set = new PTypeSet();

					for (PType u : ut.getTypes())
					{
						if (TypeComparator.compatible(u, patternType))
						{
							set.add(u);
						}
					}

					if (!set.isEmpty())
					{
						PType compatible = set.getType(node.getLocation());

						if (!TypeComparator.isSubType(question.checkType(node.getTest(), node.getExpType()), compatible))
						{
							list.add(new ValueBindingObligation(node, question));
							list.add(new SubTypeObligation(node.getTest(), compatible, node.getExpType(), question));
						}
					}
				}
			} else if (node.getTypebind() != null)
			{
				if (!TypeComparator.isSubType(question.checkType(node.getTest(), node.getExpType()), node.getDefType()))
				{
					list.add(new SubTypeObligation(node.getTest(), node.getDefType(), node.getExpType(), question));
				}
			} else if (node.getSetbind() != null)
			{
				list.addAll(node.getSetbind().getSet().apply(rootVisitor, question));
			}

			list.addAll(node.getTest().apply(rootVisitor, question));
			return list;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}

	}

	@Override
	public ProofObligationList caseAImplicitFunctionDefinition(
			AImplicitFunctionDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();
			LexNameList pids = new LexNameList();
			boolean matchNeeded = false;
			
			for (APatternListTypePair pltp : node.getParamPatterns())
			{
				for (PPattern p : pltp.getPatterns())
				{
					for (PDefinition def : p.getDefinitions())
						pids.add(def.getName());
				}
				
				if (!PPatternListAssistantTC.alwaysMatches(pltp.getPatterns()))
				{
					matchNeeded = true;
				}
			}

			if (pids.hasDuplicates() || matchNeeded)
			{
				obligations.add(new ParameterPatternObligation(node, question));
			}

			if (node.getPrecondition() != null)
			{
				obligations.addAll(node.getPrecondition().apply(rootVisitor, question));
			}

			if (node.getPostcondition() != null)
			{
				if (node.getBody() != null) // else satisfiability, below
				{
					question.push(new POFunctionDefinitionContext(node, false));
					obligations.add(new FuncPostConditionObligation(node, question));
					question.pop();
				}

				question.push(new POFunctionResultContext(node));
				obligations.addAll(node.getPostcondition().apply(rootVisitor, question));
				question.pop();
			}

			if (node.getBody() == null)
			{
				if (node.getPostcondition() != null)
				{
					question.push(new POFunctionDefinitionContext(node, false));
					obligations.add(new SatisfiabilityObligation(node, question));
					question.pop();
				}
			} else
			{
				question.push(new POFunctionDefinitionContext(node, true));
				obligations.addAll(node.getBody().apply(rootVisitor, question));

				if (node.getIsUndefined()
						|| !TypeComparator.isSubType(node.getActualResult(),  node.getType().getResult()))
				{
					obligations.add(new SubTypeObligation(node, node.getType().getResult(), node.getActualResult(), question));
				}
				
				question.pop();
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAExplicitOperationDefinition(
			AExplicitOperationDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();
			LexNameList pids = new LexNameList();

			// add all defined names from the function parameter list
			for (PPattern p : node.getParameterPatterns())
				for (PDefinition def : p.getDefinitions())
					pids.add(def.getName());

			if (pids.hasDuplicates() || !PPatternListAssistantTC.alwaysMatches(node.getParameterPatterns()))
			{
				obligations.add(new ParameterPatternObligation(node, question));
			}

			if (node.getPrecondition() != null)
			{
				obligations.addAll(node.getPrecondition().apply(rootVisitor, question));
			}

			if (node.getPostcondition() != null)
			{
				obligations.addAll(node.getPostcondition().apply(rootVisitor, question));
				obligations.add(new OperationPostConditionObligation(node, question));
			}

			obligations.addAll(node.getBody().apply(rootVisitor, question));

			if (node.getIsConstructor() && node.getClassDefinition() != null
					&& node.getClassDefinition().getInvariant() != null)
			{
				obligations.add(new StateInvariantObligation(node, question));
			}

			if (!node.getIsConstructor()
					&& !TypeComparator.isSubType(node.getActualResult(), ((AOperationType) node.getType()).getResult()))
			{
				obligations.add(new SubTypeObligation(node, node.getActualResult(), question));
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();
			LexNameList pids = new LexNameList();
			LinkedList<APatternListTypePair> plist = node.getParameterPatterns();

			LinkedList<PPattern> tmpPatterns = new LinkedList<PPattern>();
			for (APatternListTypePair tp : plist)
			{
				for (PPattern p : tp.getPatterns())
				{
					tmpPatterns.add(p);
					for (PDefinition def : p.getDefinitions())
						pids.add(def.getName());
				}
			}

			if (pids.hasDuplicates() || !PPatternListAssistantTC.alwaysMatches(tmpPatterns))
			{
				obligations.add(new ParameterPatternObligation(node, question));
			}

			if (node.getPrecondition() != null)
			{
				obligations.addAll(node.getPrecondition().apply(rootVisitor, question));
			}

			if (node.getPostcondition() != null)
			{
				obligations.addAll(node.getPostcondition().apply(rootVisitor, question));
				obligations.add(new OperationPostConditionObligation(node, question));
			}

			if (node.getBody() != null)
			{
				obligations.addAll(node.getBody().apply(rootVisitor, question));

				if (node.getIsConstructor()
						&& node.getClassDefinition() != null
						&& node.getClassDefinition().getInvariant() != null)
				{
					obligations.add(new StateInvariantObligation(node, question));
				}

				if (!node.getIsConstructor()
						&& !TypeComparator.isSubType(node.getActualResult(), ((AOperationType) node.getType()).getResult()))
				{
					obligations.add(new SubTypeObligation(node, node.getActualResult(), question));
				}
			} else
			{
				if (node.getPostcondition() != null)
				{
					question.push(new POOperationDefinitionContext(node, false, node.getStateDefinition()));
					obligations.add(new SatisfiabilityObligation(node, node.getStateDefinition(), question));
					question.pop();
				}
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAAssignmentDefinition(
			AAssignmentDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();

			PExp expression = node.getExpression();
			PType type = node.getType();
			PType expType = node.getExpType();

			obligations.addAll(expression.apply(rootVisitor, question));

			if (!TypeComparator.isSubType(question.checkType(expression, expType), type))
			{
				obligations.add(new SubTypeObligation(expression, type, expType, question));
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList defaultPDefinition(PDefinition node,
			POContextStack question)
	{
		return new ProofObligationList();
	}

	public ProofObligationList caseAInstanceVariableDefinition(
			AInstanceVariableDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();

			PExp expression = node.getExpression();
			PType type = node.getType();
			PType expType = node.getExpType();

			obligations.addAll(expression.apply(rootVisitor, question));

			if (!TypeComparator.isSubType(question.checkType(expression, expType), type))
			{
				obligations.add(new SubTypeObligation(expression, type, expType, question));
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAPerSyncDefinition(APerSyncDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			question.push(new PONameContext(new LexNameList(node.getOpname())));
			ProofObligationList list = node.getGuard().apply(rootVisitor, question);
			question.pop();
			return list;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAStateDefinition(AStateDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			ProofObligationList list = new ProofObligationList();

			if (node.getInvdef() != null)
			{
				list.addAll(node.getInvdef().apply(mainVisitor, question));
			}

			return list;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseATypeDefinition(ATypeDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			ProofObligationList list = new ProofObligationList();

			AExplicitFunctionDefinition invDef = node.getInvdef();

			if (invDef != null)
			{
				list.addAll(invDef.apply(mainVisitor, question));
			}

			return list;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList caseAValueDefinition(AValueDefinition node,
			POContextStack question) throws AnalysisException
	{
		try
		{
			ProofObligationList obligations = new ProofObligationList();

			PExp exp = node.getExpression();
			obligations.addAll(exp.apply(rootVisitor, question));

			PPattern pattern = node.getPattern();
			PType type = node.getType();

			if (!(pattern instanceof AIdentifierPattern)
					&& !(pattern instanceof AIgnorePattern)
					&& PTypeAssistantTC.isUnion(type))
			{
				PType patternType = PPatternAssistantTC.getPossibleType(pattern);
				AUnionType ut = PTypeAssistantTC.getUnion(type);
				PTypeSet set = new PTypeSet();

				for (PType u : ut.getTypes())
				{
					if (TypeComparator.compatible(u, patternType))
						set.add(u);
				}

				if (!set.isEmpty())
				{
					PType compatible = set.getType(node.getLocation());
					if (!TypeComparator.isSubType(type, compatible))
					{
						obligations.add(new ValueBindingObligation(node, question));
						obligations.add(new SubTypeObligation(exp, compatible, type, question));
					}
				}
			}

			if (!TypeComparator.isSubType(question.checkType(exp, node.getExpType()), type))
			{
				obligations.add(new SubTypeObligation(exp, type, node.getExpType(), question));
			}

			return obligations;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList defaultPTraceDefinition(PTraceDefinition node,
			POContextStack question)
	{

		return new ProofObligationList();
	}

	@Override
	public ProofObligationList defaultPTraceCoreDefinition(
			PTraceCoreDefinition node, POContextStack question)
	{

		return new ProofObligationList();
	}

	@Override
	public ProofObligationList caseAClassClassDefinition(
			AClassClassDefinition node, POContextStack question)
			throws AnalysisException
	{
		try
		{
			ProofObligationList proofObligationList = new ProofObligationList();

			for (PDefinition def : node.getDefinitions())
			{
				question.push(new PONameContext(PDefinitionAssistantTC.getVariableNames(def)));
				proofObligationList.addAll(def.apply(mainVisitor, question));
				question.pop();
			}
			return proofObligationList;
		} catch (Exception e)
		{
			throw new POException(node, e);
		}
	}

	@Override
	public ProofObligationList createNewReturnValue(INode node,
			POContextStack question)
	{
		return new ProofObligationList();
	}

	@Override
	public ProofObligationList createNewReturnValue(Object node,
			POContextStack question)
	{
		return new ProofObligationList();
	}

}
