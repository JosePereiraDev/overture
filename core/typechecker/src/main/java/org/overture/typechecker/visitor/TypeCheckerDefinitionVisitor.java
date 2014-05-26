package org.overture.typechecker.visitor;

import java.util.Collections;
import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.AAssignmentDefinition;
import org.overture.ast.definitions.AClassInvariantDefinition;
import org.overture.ast.definitions.AEqualsDefinition;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AExternalDefinition;
import org.overture.ast.definitions.AImplicitFunctionDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AImportedDefinition;
import org.overture.ast.definitions.AInheritedDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.ALocalDefinition;
import org.overture.ast.definitions.AMultiBindListDefinition;
import org.overture.ast.definitions.AMutexSyncDefinition;
import org.overture.ast.definitions.ANamedTraceDefinition;
import org.overture.ast.definitions.APerSyncDefinition;
import org.overture.ast.definitions.ARenamedDefinition;
import org.overture.ast.definitions.AStateDefinition;
import org.overture.ast.definitions.AThreadDefinition;
import org.overture.ast.definitions.ATypeDefinition;
import org.overture.ast.definitions.AUntypedDefinition;
import org.overture.ast.definitions.AValueDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.definitions.traces.AApplyExpressionTraceCoreDefinition;
import org.overture.ast.definitions.traces.ABracketedExpressionTraceCoreDefinition;
import org.overture.ast.definitions.traces.ALetBeStBindingTraceDefinition;
import org.overture.ast.definitions.traces.ALetDefBindingTraceDefinition;
import org.overture.ast.definitions.traces.ARepeatTraceDefinition;
import org.overture.ast.definitions.traces.ATraceDefinitionTerm;
import org.overture.ast.definitions.traces.PTraceDefinition;
import org.overture.ast.expressions.ANotYetSpecifiedExp;
import org.overture.ast.expressions.ASubclassResponsibilityExp;
import org.overture.ast.expressions.AUndefinedExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.lex.VDMToken;
import org.overture.ast.node.NodeList;
import org.overture.ast.patterns.APatternListTypePair;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.ast.patterns.PPattern;
import org.overture.ast.statements.AErrorCase;
import org.overture.ast.statements.AExternalClause;
import org.overture.ast.statements.ANotYetSpecifiedStm;
import org.overture.ast.statements.ASubclassResponsibilityStm;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.AFieldField;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.ANamedInvariantType;
import org.overture.ast.types.ANatNumericBasicType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.AProductType;
import org.overture.ast.types.ARecordInvariantType;
import org.overture.ast.types.AUnknownType;
import org.overture.ast.types.AVoidType;
import org.overture.ast.types.PType;
import org.overture.typechecker.Environment;
import org.overture.typechecker.FlatCheckedEnvironment;
import org.overture.typechecker.FlatEnvironment;
import org.overture.typechecker.PrivateClassEnvironment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeChecker;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.util.HelpLexNameToken;
import org.overture.typechecker.utilities.DefinitionTypeResolver;
import org.overture.typechecker.utilities.type.QualifiedDefinition;

public class TypeCheckerDefinitionVisitor extends AbstractTypeCheckVisitor
{

	public TypeCheckerDefinitionVisitor(
			IQuestionAnswer<TypeCheckInfo, PType> typeCheckVisitor)
	{
		super(typeCheckVisitor);
	}

	@Override
	public PType caseAAssignmentDefinition(AAssignmentDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		question.qualifiers = null;
		
		TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		
		node.setExpType(node.getExpression().apply(THIS, question));
		node.setType(question.assistantFactory.createPTypeAssistant().typeResolve(question.assistantFactory.createPDefinitionAssistant().getType(node), null, THIS, question));

		if (node.getExpType() instanceof AVoidType)
		{
			TypeCheckerErrors.report(3048, "Expression does not return a value", node.getExpression().getLocation(), node.getExpression());
		}

		if (!TypeComparator.compatible(node.getType(), node.getExpType()))
		{
			TypeCheckerErrors.report(3000, "Expression does not match declared type", node.getLocation(), node);
			TypeCheckerErrors.detail2("Declared", node.getType(), "Expression", node.getExpType());
		}

		return node.getType();
	}

	@Override
	public PType caseAInstanceVariableDefinition(
			AInstanceVariableDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{

		if (node.getExpression() instanceof AUndefinedExp)
		{
			if (question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()))
			{
				TypeCheckerErrors.report(3037, "Static instance variable is not initialized: "
						+ node.getName(), node.getLocation(), node);
			}
		}

		// Initializers can reference class members, so create a new env.
		// We set the type qualifier to unknown so that type-based name
		// resolution will succeed.

		Environment cenv = new PrivateClassEnvironment(question.assistantFactory, node.getClassDefinition(), question.env);

		// TODO: This should be a call to the assignment definition typecheck
		// but instance is not an subclass of
		// assignment in our tree
		node.setExpType(node.getExpression().apply(THIS, new TypeCheckInfo(question.assistantFactory, cenv, NameScope.NAMESANDSTATE, question.qualifiers)));
		node.setType(question.assistantFactory.createPTypeAssistant().typeResolve(question.assistantFactory.createPDefinitionAssistant().getType(node), null, THIS, question));

		if (node.getExpType() instanceof AVoidType)
		{
			TypeCheckerErrors.report(3048, "Expression does not return a value", node.getExpression().getLocation(), node.getExpression());
		}

		if (!TypeComparator.compatible(question.assistantFactory.createPDefinitionAssistant().getType(node), node.getExpType()))
		{
			TypeCheckerErrors.report(3000, "Expression does not match declared type", node.getLocation(), node);
			TypeCheckerErrors.detail2("Declared", question.assistantFactory.createPDefinitionAssistant().getType(node), "Expression", node.getExpType());
		}

		return node.getType();

	}

	@Override
	public PType caseAClassInvariantDefinition(AClassInvariantDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		question.qualifiers = null;
		question.scope = NameScope.NAMESANDSTATE;
		PType type = node.getExpression().apply(THIS, question);

		if (!question.assistantFactory.createPTypeAssistant().isType(type, ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3013, "Class invariant is not a boolean expression", node.getLocation(), node);
		}

		node.setType(type);
		return node.getType();
	}

	@Override
	public PType caseAEqualsDefinition(AEqualsDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		question.qualifiers = null;

		node.setExpType(node.getTest().apply(THIS, question));
		PPattern pattern = node.getPattern();

		if (pattern != null)
		{
			question.assistantFactory.createPPatternAssistant().typeResolve(pattern, THIS, question);
			node.setDefs(question.assistantFactory.createPPatternAssistant().getDefinitions(pattern, node.getExpType(), question.scope));
			node.setDefType(node.getExpType());
		} else if (node.getTypebind() != null)
		{
			question.assistantFactory.createATypeBindAssistant().typeResolve(node.getTypebind(), THIS, question);
			ATypeBind typebind = node.getTypebind();

			if (!TypeComparator.compatible(typebind.getType(), node.getExpType()))
			{
				TypeCheckerErrors.report(3014, "Expression is not compatible with type bind", typebind.getLocation(), typebind);
			}

			node.setDefType(typebind.getType()); // Effectively a cast
			node.setDefs(question.assistantFactory.createPPatternAssistant().getDefinitions(typebind.getPattern(), node.getDefType(), question.scope));
		} else
		{
			question.qualifiers = null;
			PType st = node.getSetbind().getSet().apply(THIS, question);

			if (!question.assistantFactory.createPTypeAssistant().isSet(st))
			{
				TypeCheckerErrors.report(3015, "Set bind is not a set type?", node.getLocation(), node);
				node.setDefType(node.getExpType());
			} else
			{
				PType setof = question.assistantFactory.createPTypeAssistant().getSet(st).getSetof();

				if (!TypeComparator.compatible(node.getExpType(), setof))
				{
					TypeCheckerErrors.report(3016, "Expression is not compatible with set bind", node.getSetbind().getLocation(), node.getSetbind());
				}

				node.setDefType(setof); // Effectively a cast
			}

			question.assistantFactory.createPPatternAssistant().typeResolve(node.getSetbind().getPattern(), THIS, question);
			node.setDefs(question.assistantFactory.createPPatternAssistant().getDefinitions(node.getSetbind().getPattern(), node.getDefType(), question.scope));
		}

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(node.getDefs(), THIS, question);
		return node.getType();
	}

	@Override
	public PType caseAExplicitFunctionDefinition(
			AExplicitFunctionDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{

		NodeList<PDefinition> defs = new NodeList<PDefinition>(node);
		TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		
		if (node.getTypeParams() != null)
		{
			defs.addAll(question.assistantFactory.createAExplicitFunctionDefinitionAssistant().getTypeParamDefinitions(node));
		}

		PType expectedResult = question.assistantFactory.createAExplicitFunctionDefinitionAssistant().checkParams(node, node.getParamPatternList().listIterator(), (AFunctionType) node.getType());
		node.setExpectedResult(expectedResult);
		List<List<PDefinition>> paramDefinitionList = question.assistantFactory.createAExplicitFunctionDefinitionAssistant().getParamDefinitions(node, (AFunctionType) node.getType(), node.getParamPatternList(), node.getLocation());

		Collections.reverse(paramDefinitionList);

		for (List<PDefinition> pdef : paramDefinitionList)
		{
			defs.addAll(pdef); // All definitions of all parameter lists
		}

		FlatCheckedEnvironment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);

		local.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
		local.setEnclosingDefinition(node);

		// building the new scope for subtypechecks

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(defs, this, new TypeCheckInfo(question.assistantFactory, local, question.scope, question.qualifiers)); // can be this because its a definition list

		if (question.env.isVDMPP()
				&& !question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()))
		{
			local.add(question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node));
		}

		List<QualifiedDefinition> qualified = new Vector<QualifiedDefinition>(); 

		if (node.getPredef() != null)
		{
			// building the new scope for subtypechecks

			PType b = node.getPredef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMES));
			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeChecker.report(3018, "Precondition returns unexpected type", node.getLocation());
				TypeChecker.detail2("Actual", b, "Expected", expected);
			}
			
			qualified = node.getPredef().getBody().apply(question.assistantFactory.getQualificationVisitor(), new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMES));

			for (QualifiedDefinition qdef: qualified)
			{
				qdef.qualifyType();
			}
		}

		if (node.getPostdef() != null)
		{
			LexNameToken result = new LexNameToken(node.getName().getModule(), "RESULT", node.getLocation());
			PPattern rp = AstFactory.newAIdentifierPattern(result);
			List<PDefinition> rdefs = question.assistantFactory.createPPatternAssistant().getDefinitions(rp, expectedResult, NameScope.NAMES);
			FlatCheckedEnvironment post = new FlatCheckedEnvironment(question.assistantFactory, rdefs, local, NameScope.NAMES);

			// building the new scope for subtypechecks
			PType b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, post, NameScope.NAMES));
			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeChecker.report(3018, "Postcondition returns unexpected type", node.getLocation());
				TypeChecker.detail2("Actual", b, "Expected", expected);
			}
		}

		// This check returns the type of the function body in the case where
		// all of the curried parameter sets are provided.

		PType actualResult = node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope, null, expectedResult, null));

		node.setActualResult(actualResult);

		for (QualifiedDefinition qdef: qualified)
		{
			qdef.resetType();
		}

		if (!TypeComparator.compatible(expectedResult, node.getActualResult()))
		{
			TypeChecker.report(3018, "Function returns unexpected type", node.getLocation());
			TypeChecker.detail2("Actual", node.getActualResult(), "Expected", expectedResult);
		}

		if (question.assistantFactory.createPTypeAssistant().narrowerThan(node.getType(), node.getAccess()))
		{
			TypeCheckerErrors.report(3019, "Function parameter visibility less than function definition", node.getLocation(), node);
		}

		if (node.getMeasure() == null && node.getRecursive())
		{
			TypeCheckerErrors.warning(5012, "Recursive function has no measure", node.getLocation(), node);
		} else if (node.getMeasure() != null)
		{
			if (question.env.isVDMPP())
			{
				node.getMeasure().setTypeQualifier(question.assistantFactory.createAExplicitFunctionDefinitionAssistant().getMeasureParams(node));
			}
			node.setMeasureDef(question.env.findName(node.getMeasure(), question.scope));

			if (node.getMeasureDef() == null)
			{
				TypeCheckerErrors.report(3270, "Measure " + node.getMeasure()
						+ " is not in scope", node.getMeasure().getLocation(), node.getMeasure());
			} else if (!(node.getMeasureDef() instanceof AExplicitFunctionDefinition))
			{
				TypeCheckerErrors.report(3271, "Measure " + node.getMeasure()
						+ " is not an explicit function", node.getMeasure().getLocation(), node.getMeasure());
			} else if (node.getMeasureDef() == node)
			{
				TypeCheckerErrors.report(3304, "Recursive function cannot be its own measure", node.getMeasure().getLocation(), node.getMeasure());
			} else
			{
				AExplicitFunctionDefinition efd = (AExplicitFunctionDefinition) node.getMeasureDef();

				if (node.getTypeParams() == null && efd.getTypeParams() != null)
				{
					TypeCheckerErrors.report(3309, "Measure must not be polymorphic", node.getMeasure().getLocation(), node.getMeasure());
				} else if (node.getTypeParams() != null
						&& efd.getTypeParams() == null)
				{
					TypeCheckerErrors.report(3310, "Measure must also be polymorphic", node.getMeasure().getLocation(), node.getMeasure());
				} else if (node.getTypeParams() != null
						&& efd.getTypeParams() != null
						&& !node.getTypeParams().equals(efd.getTypeParams()))
				{
					TypeCheckerErrors.report(3318, "Measure's type parameters must match function's", node.getMeasure().getLocation(), node.getMeasure());
					TypeChecker.detail2("Actual", efd.getTypeParams(), "Expected", node.getTypeParams());
				}

				AFunctionType mtype = (AFunctionType) efd.getType();

				if (!TypeComparator.compatible(mtype.getParameters(), question.assistantFactory.createAExplicitFunctionDefinitionAssistant().getMeasureParams(node), question.assistantFactory))
				{
					TypeCheckerErrors.report(3303, "Measure parameters different to function", node.getMeasure().getLocation(), node.getMeasure());
					TypeChecker.detail2(node.getMeasure().getFullName(), mtype.getParameters(), "Expected", question.assistantFactory.createAExplicitFunctionDefinitionAssistant().getMeasureParams(node));
				}

				if (!(mtype.getResult() instanceof ANatNumericBasicType))
				{
					if (mtype.getResult() instanceof AProductType)
					{
						AProductType pt = question.assistantFactory.createPTypeAssistant().getProduct(mtype.getResult());

						for (PType t : pt.getTypes())
						{
							if (!(t instanceof ANatNumericBasicType))
							{
								TypeCheckerErrors.report(3272, "Measure range is not a nat, or a nat tuple", node.getMeasure().getLocation(), node.getMeasure());
								TypeCheckerErrors.detail("Actual", mtype.getResult());
								break;
							}
						}

						node.setMeasureLexical(pt.getTypes().size());
					} else
					{
						TypeCheckerErrors.report(3272, "Measure range is not a nat, or a nat tuple", node.getMeasure().getLocation(), node.getMeasure());
						TypeCheckerErrors.detail("Actual", mtype.getResult());
					}
				}
			}
		}

		if (!(node.getBody() instanceof ANotYetSpecifiedExp)
				&& !(node.getBody() instanceof ASubclassResponsibilityExp))
		{
			local.unusedCheck();
		}

		node.setType(node.getType());
		return node.getType();
	}

	@Override
	public PType caseAExternalDefinition(AExternalDefinition node,
			TypeCheckInfo question)
	{
		// Nothing to do - state is type checked separately
		return null;
	}

	@Override
	public PType caseAImplicitFunctionDefinition(
			AImplicitFunctionDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		List<PDefinition> defs = new Vector<PDefinition>();

		if (node.getTypeParams() != null)
		{
			defs.addAll(question.assistantFactory.createAImplicitFunctionDefinitionAssistant().getTypeParamDefinitions(node));
		}

		List<PDefinition> argdefs = new Vector<PDefinition>();

		for (APatternListTypePair pltp : node.getParamPatterns())
		{
			argdefs.addAll(question.assistantFactory.createAPatternListTypePairAssistant().getDefinitions(pltp, NameScope.LOCAL));
		}

		defs.addAll(question.assistantFactory.createPDefinitionAssistant().checkDuplicatePatterns(node, argdefs));
		FlatCheckedEnvironment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
		local.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
		local.setEnclosingDefinition(node);

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(defs, THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope, question.qualifiers));

		List<QualifiedDefinition> qualified = new Vector<QualifiedDefinition>(); 

		if (node.getPredef() != null)
		{
			PType b = node.getPredef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Precondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}

			qualified = node.getPredef().getBody().apply(question.assistantFactory.getQualificationVisitor(), new TypeCheckInfo(question.assistantFactory, local, question.scope));

			for (QualifiedDefinition qdef: qualified)
			{
				qdef.qualifyType();
			}
		}

		if (node.getBody() != null)
		{
			if (node.getClassDefinition() != null
					&& !question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()))
			{
				local.add(question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node));
			}

			node.setActualResult(node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope, question.qualifiers, node.getResult().getType(), null)));

			if (!TypeComparator.compatible(node.getResult().getType(), node.getActualResult()))
			{
				TypeCheckerErrors.report(3029, "Function returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", node.getActualResult(), "Expected", node.getResult().getType());
			}
		}

		for (QualifiedDefinition qdef: qualified)
		{
			qdef.resetType();
		}

		if (question.assistantFactory.createPTypeAssistant().narrowerThan(question.assistantFactory.createPDefinitionAssistant().getType(node), node.getAccess()))
		{
			TypeCheckerErrors.report(3030, "Function parameter visibility less than function definition", node.getLocation(), node);
		}

		// The result variables are in scope for the post condition

		if (node.getPostdef() != null)
		{
			PType b = null;

			if (node.getResult() != null)
			{
				List<PDefinition> postdefs = question.assistantFactory.createAPatternTypePairAssistant().getDefinitions(node.getResult());
				FlatCheckedEnvironment post = new FlatCheckedEnvironment(question.assistantFactory, postdefs, local, NameScope.NAMES);
				post.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
				post.setEnclosingDefinition(node);
				b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, post, NameScope.NAMES));
				post.unusedCheck();
			} else
			{
				b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMES));
			}

			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Postcondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}
		}

		if (node.getMeasure() == null && node.getRecursive())
		{
			TypeCheckerErrors.warning(5012, "Recursive function has no measure", node.getLocation(), node);
		} else if (node.getMeasure() != null)
		{
			if (question.env.isVDMPP())
			{
				node.getMeasure().setTypeQualifier(((AFunctionType) node.getType()).getParameters());
			}
			node.setMeasureDef(question.env.findName(node.getMeasure(), question.scope));

			if (node.getBody() == null)
			{
				TypeCheckerErrors.report(3273, "Measure not allowed for an implicit function", node.getMeasure().getLocation(), node);
			} else if (node.getMeasureDef() == null)
			{
				TypeCheckerErrors.report(3270, "Measure " + node.getMeasure()
						+ " is not in scope", node.getMeasure().getLocation(), node.getMeasure());
			} else if (!(node.getMeasureDef() instanceof AExplicitFunctionDefinition))
			{
				TypeCheckerErrors.report(3271, "Measure " + node.getMeasure()
						+ " is not an explicit function", node.getMeasure().getLocation(), node.getMeasure());
			} else
			{
				AExplicitFunctionDefinition efd = (AExplicitFunctionDefinition) node.getMeasureDef();

				if (node.getTypeParams() == null && efd.getTypeParams() != null)
				{
					TypeCheckerErrors.report(3309, "Measure must not be polymorphic", node.getMeasure().getLocation(), node.getMeasure());
				} else if (node.getTypeParams() != null
						&& efd.getTypeParams() == null)
				{
					TypeCheckerErrors.report(3310, "Measure must also be polymorphic", node.getMeasure().getLocation(), node.getMeasure());
				} else if (node.getTypeParams() != null
						&& efd.getTypeParams() != null
						&& !node.getTypeParams().equals(efd.getTypeParams()))
				{
					TypeCheckerErrors.report(3318, "Measure's type parameters must match function's", node.getMeasure().getLocation(), node.getMeasure());
					TypeCheckerErrors.detail2("Actual", efd.getTypeParams(), "Expected", node.getTypeParams());
				}

				AFunctionType mtype = (AFunctionType) node.getMeasureDef().getType();

				if (!TypeComparator.compatible(mtype.getParameters(), ((AFunctionType) node.getType()).getParameters(), question.assistantFactory ))
				{
					TypeCheckerErrors.report(3303, "Measure parameters different to function", node.getMeasure().getLocation(), node.getMeasure());
					TypeCheckerErrors.detail2(node.getMeasure().getName(), mtype.getParameters(), node.getName().getName(), ((AFunctionType) node.getType()).getParameters());
				}

				if (!(mtype.getResult() instanceof ANatNumericBasicType))
				{
					if (question.assistantFactory.createPTypeAssistant().isProduct(mtype.getResult()))
					{
						AProductType pt = question.assistantFactory.createPTypeAssistant().getProduct(mtype.getResult());

						for (PType t : pt.getTypes())
						{
							if (!(t instanceof ANatNumericBasicType))
							{
								TypeCheckerErrors.report(3272, "Measure range is not a nat, or a nat tuple", node.getMeasure().getLocation(), node.getMeasure());
								TypeCheckerErrors.detail("Actual", mtype.getResult());
							}
						}

						node.setMeasureLexical(pt.getTypes().size());
					} else
					{
						TypeCheckerErrors.report(3272, "Measure range is not a nat, or a nat tuple", node.getMeasure().getLocation(), node.getMeasure());
						TypeCheckerErrors.detail("Actual", mtype.getResult());
					}
				}
			}
		}

		if (!(node.getBody() instanceof ANotYetSpecifiedExp)
				&& !(node.getBody() instanceof ASubclassResponsibilityExp))
		{
			local.unusedCheck();
		}

		node.setType(node.getType());
		return node.getType();
	}

	@Override
	public PType caseAExplicitOperationDefinition(
			AExplicitOperationDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		List<PType> ptypes = ((AOperationType) node.getType()).getParameters();

		if (node.getParameterPatterns().size() > ptypes.size())
		{
			TypeCheckerErrors.report(3023, "Too many parameter patterns", node.getLocation(), node);
			TypeCheckerErrors.detail2("Type params", ptypes.size(), "Patterns", node.getParameterPatterns().size());
			return null;
		} else if (node.getParameterPatterns().size() < ptypes.size())
		{
			TypeCheckerErrors.report(3024, "Too few parameter patterns", node.getLocation(), node);
			TypeCheckerErrors.detail2("Type params", ptypes.size(), "Patterns", node.getParameterPatterns().size());
			return null;
		}

		node.setParamDefinitions(question.assistantFactory.createAExplicitOperationDefinitionAssistant().getParamDefinitions(node));
		question.assistantFactory.createPDefinitionListAssistant().typeCheck(node.getParamDefinitions(), THIS, new TypeCheckInfo(question.assistantFactory, question.env, NameScope.NAMESANDSTATE, question.qualifiers));

		FlatCheckedEnvironment local = new FlatCheckedEnvironment(question.assistantFactory, node.getParamDefinitions(), question.env, NameScope.NAMESANDSTATE);
		local.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
		local.setEnclosingDefinition(node);

		if (question.env.isVDMPP())
		{
			if (!question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()))
			{
				local.add(question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node));
			}

			if (node.getName().getName().equals(node.getClassDefinition().getName().getName()))
			{
				node.setIsConstructor(true);
				node.getClassDefinition().setHasContructors(true);

				if (question.assistantFactory.createPAccessSpecifierAssistant().isAsync(node.getAccess()))
				{
					TypeCheckerErrors.report(3286, "Constructor cannot be 'async'", node.getLocation(), node);
				}

				if (question.assistantFactory.createPTypeAssistant().isClass(((AOperationType) node.getType()).getResult()))
				{
					AClassType ctype = question.assistantFactory.createPTypeAssistant().getClassType(((AOperationType) node.getType()).getResult());

					if (ctype.getClassdef() != node.getClassDefinition())
					{
						// FIXME: This is a TEST, it should be tried to see if
						// it is valid
						TypeCheckerErrors.report(3025, "Constructor operation must have return type "
								+ node.getClassDefinition().getName().getName(), node.getLocation(), node);
					}
					// TODO: THIS COULD BE A HACK to code (ctype.getClassdef()
					// != node.getClassDefinition())
					if (!HelpLexNameToken.isEqual(ctype.getClassdef().getName(), node.getClassDefinition().getName()))
					{
						TypeCheckerErrors.report(3025, "Constructor operation must have return type "
								+ node.getClassDefinition().getName().getName(), node.getLocation(), node);
					}
				} else
				{
					TypeCheckerErrors.report(3026, "Constructor operation must have return type "
							+ node.getClassDefinition().getName().getName(), node.getLocation(), node);
				}
			}
		}

		List<QualifiedDefinition> qualified = new Vector<QualifiedDefinition>(); 

		if (node.getPredef() != null)
		{
			FlatEnvironment pre = new FlatEnvironment(question.assistantFactory, new Vector<PDefinition>(), local);
			pre.setEnclosingDefinition(node.getPredef());

			PType b = node.getPredef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, pre, NameScope.NAMESANDSTATE));

			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Precondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}

			qualified = node.getPredef().getBody().apply(question.assistantFactory.getQualificationVisitor(), new TypeCheckInfo(question.assistantFactory, pre, NameScope.NAMESANDSTATE));

			for (QualifiedDefinition qdef: qualified)
			{
				qdef.qualifyType();
			}
		}

		if (node.getPostdef() != null)
		{
			LexNameToken result = new LexNameToken(node.getName().getModule(), "RESULT", node.getLocation());
			PPattern rp = AstFactory.newAIdentifierPattern(result);
			List<PDefinition> rdefs = question.assistantFactory.createPPatternAssistant().getDefinitions(rp, ((AOperationType) node.getType()).getResult(), NameScope.NAMESANDANYSTATE);
			FlatEnvironment post = new FlatEnvironment(question.assistantFactory, rdefs, local);
			post.setEnclosingDefinition(node.getPostdef());
			PType b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, post, NameScope.NAMESANDANYSTATE));
			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Postcondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}
		}

		PType expectedResult = ((AOperationType) node.getType()).getResult();
		PType actualResult = node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE, null, null, expectedResult));
		node.setActualResult(actualResult);
		boolean compatible = TypeComparator.compatible(expectedResult, node.getActualResult());

		for (QualifiedDefinition qdef: qualified)
		{
			qdef.resetType();
		}

		if (node.getIsConstructor()
				&& !question.assistantFactory.createPTypeAssistant().isType(node.getActualResult(), AVoidType.class)
				&& !compatible || !node.getIsConstructor() && !compatible)
		{
			TypeCheckerErrors.report(3027, "Operation returns unexpected type", node.getLocation(), node);
			TypeCheckerErrors.detail2("Actual", node.getActualResult(), "Expected", ((AOperationType) node.getType()).getResult());
		} else if (!node.getIsConstructor()
				&& !question.assistantFactory.createPTypeAssistant().isUnknown(actualResult))
		{
			if (question.assistantFactory.createPTypeAssistant().isVoid(((AOperationType) node.getType()).getResult())
					&& !question.assistantFactory.createPTypeAssistant().isVoid(actualResult))
			{
				TypeCheckerErrors.report(3312, "Void operation returns non-void value", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", actualResult, "Expected", ((AOperationType) node.getType()).getResult());
			} else if (!question.assistantFactory.createPTypeAssistant().isVoid(((AOperationType) node.getType()).getResult())
					&& question.assistantFactory.createPTypeAssistant().hasVoid(actualResult))
			{
				TypeCheckerErrors.report(3313, "Operation returns void value", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", actualResult, "Expected", ((AOperationType) node.getType()).getResult());
			}
		}

		if (question.assistantFactory.createPAccessSpecifierAssistant().isAsync(node.getAccess())
				&& !question.assistantFactory.createPTypeAssistant().isType(((AOperationType) node.getType()).getResult(), AVoidType.class))
		{
			TypeCheckerErrors.report(3293, "Asynchronous operation "
					+ node.getName() + " cannot return a value", node.getLocation(), node);
		}

		if (question.assistantFactory.createPTypeAssistant().narrowerThan(node.getType(), node.getAccess()))
		{
			TypeCheckerErrors.report(3028, "Operation parameter visibility less than operation definition", node.getLocation(), node);
		}

		if (!(node.getBody() instanceof ANotYetSpecifiedStm)
				&& !(node.getBody() instanceof ASubclassResponsibilityStm))
		{
			local.unusedCheck();
		}
		node.setType(node.getType());
		return node.getType();
	}

	@Override
	public PType caseAImplicitOperationDefinition(
			AImplicitOperationDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		question = new TypeCheckInfo(question.assistantFactory, question.env, NameScope.NAMESANDSTATE, question.qualifiers);
		List<PDefinition> defs = new Vector<PDefinition>();
		List<PDefinition> argdefs = new Vector<PDefinition>();

		if (question.env.isVDMPP())
		{
			node.setStateDefinition(question.env.findClassDefinition());
		} else
		{
			node.setStateDefinition(question.env.findStateDefinition());
		}

		for (APatternListTypePair ptp : node.getParameterPatterns())
		{
			argdefs.addAll(question.assistantFactory.createAPatternListTypePairAssistant().getDefinitions(ptp, NameScope.LOCAL));
		}

		defs.addAll(question.assistantFactory.createPDefinitionAssistant().checkDuplicatePatterns(node, argdefs));

		if (node.getResult() != null)
		{
			defs.addAll(question.assistantFactory.createPPatternAssistant().getDefinitions(node.getResult().getPattern(), ((AOperationType) node.getType()).getResult(), NameScope.LOCAL));
		}

		// Now we build local definitions for each of the externals, so
		// that they can be added to the local environment, while the
		// global state is made inaccessible - but only if we have
		// an "ext" clause

		boolean limitStateScope = false;

		if (node.getExternals().size() != 0)
		{
			for (AExternalClause clause : node.getExternals())
			{
				TypeComparator.checkComposeTypes(clause.getType(), question.env, false);
				
				for (ILexNameToken exname : clause.getIdentifiers())
				{
					PDefinition sdef = question.env.findName(exname, NameScope.STATE);
					question.assistantFactory.createAExternalClauseAssistant().typeResolve(clause, THIS, question);

					if (sdef == null)
					{
						TypeCheckerErrors.report(3031, "Unknown state variable "
								+ exname, exname.getLocation(), exname);
					} else
					{
						if (!(clause.getType() instanceof AUnknownType)
								&& !question.assistantFactory.createPTypeAssistant().equals(sdef.getType(), clause.getType()))
						{
							TypeCheckerErrors.report(3032, "State variable "
									+ exname + " is not this type", node.getLocation(), node);
							TypeCheckerErrors.detail2("Declared", sdef.getType(), "ext type", clause.getType());
						} else
						{
							defs.add(AstFactory.newAExternalDefinition(sdef, clause.getMode()));

							// VDM++ "ext wr" clauses in a constructor
							// effectively
							// initialize the instance variable concerned.

							if (clause.getMode().getType() == VDMToken.WRITE
									&& sdef instanceof AInstanceVariableDefinition
									&& node.getName().getName().equals(node.getClassDefinition().getName().getName()))
							{
								AInstanceVariableDefinition iv = (AInstanceVariableDefinition) sdef;
								iv.setInitialized(true);
							}
						}
					}
				}
			}

			// All relevant globals are now in defs (local), so we
			// limit the state searching scope

			limitStateScope = true;
		}

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(defs, THIS, question);

		FlatCheckedEnvironment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
		local.setLimitStateScope(limitStateScope);
		local.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
		local.setEnclosingDefinition(node);

		if (question.env.isVDMPP())
		{
			if (node.getName().getName().equals(node.getClassDefinition().getName().getName()))
			{

				node.setIsConstructor(true);
				node.getClassDefinition().setHasContructors(true);

				if (question.assistantFactory.createPAccessSpecifierAssistant().isAsync(node.getAccess()))
				{
					TypeCheckerErrors.report(3286, "Constructor cannot be 'async'", node.getLocation(), node);
				}

				if (question.assistantFactory.createPTypeAssistant().isClass(((AOperationType) node.getType()).getResult()))
				{
					AClassType ctype = question.assistantFactory.createPTypeAssistant().getClassType(((AOperationType) node.getType()).getResult());

					if (ctype.getClassdef() != node.getClassDefinition())
					{
						TypeCheckerErrors.report(3025, "Constructor operation must have return type "
								+ node.getClassDefinition().getName().getName(), node.getLocation(), node);
					}
				} else
				{
					TypeCheckerErrors.report(3026, "Constructor operation must have return type "
							+ node.getClassDefinition().getName().getName(), node.getLocation(), node);

				}
			}
		}

		List<QualifiedDefinition> qualified = new Vector<QualifiedDefinition>(); 

		if (node.getPredef() != null)
		{
			FlatEnvironment pre = new FlatEnvironment(question.assistantFactory, new Vector<PDefinition>(), local);
			pre.setEnclosingDefinition(node.getPredef());
			PType b = node.getPredef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, pre, NameScope.NAMESANDSTATE));
			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Precondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}

			qualified = node.getPredef().getBody().apply(question.assistantFactory.getQualificationVisitor(), new TypeCheckInfo(question.assistantFactory, pre, NameScope.NAMESANDSTATE));

			for (QualifiedDefinition qdef: qualified)
			{
				qdef.qualifyType();
			}
		}

		if (node.getBody() != null)
		{
			if (node.getClassDefinition() != null
					&& !question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()))
			{
				local.add(question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node));
			}

			PType expectedResult = ((AOperationType) node.getType()).getResult();
			node.setActualResult(node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE, null, null, expectedResult)));

			boolean compatible = TypeComparator.compatible(expectedResult, node.getActualResult());

			if (node.getIsConstructor()
					&& !question.assistantFactory.createPTypeAssistant().isType(node.getActualResult(), AVoidType.class)
					&& !compatible || !node.getIsConstructor() && !compatible)
			{
				TypeCheckerErrors.report(3035, "Operation returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", node.getActualResult(), "Expected", ((AOperationType) node.getType()).getResult());
			} else if (!node.getIsConstructor()
					&& !question.assistantFactory.createPTypeAssistant().isUnknown(node.getActualResult()))
			{
				if (question.assistantFactory.createPTypeAssistant().isVoid(((AOperationType) node.getType()).getResult())
						&& !question.assistantFactory.createPTypeAssistant().isVoid(node.getActualResult()))
				{
					TypeCheckerErrors.report(3312, "Void operation returns non-void value", node.getLocation(), node);
					TypeCheckerErrors.detail2("Actual", node.getActualResult(), "Expected", ((AOperationType) node.getType()).getResult());
				} else if (!question.assistantFactory.createPTypeAssistant().isVoid(((AOperationType) node.getType()).getResult())
						&& question.assistantFactory.createPTypeAssistant().hasVoid(node.getActualResult()))
				{
					TypeCheckerErrors.report(3313, "Operation returns void value", node.getLocation(), node);
					TypeCheckerErrors.detail2("Actual", node.getActualResult(), "Expected", ((AOperationType) node.getType()).getResult());
				}
			}
		}

		for (QualifiedDefinition qdef: qualified)
		{
			qdef.resetType();
		}

		if (question.assistantFactory.createPAccessSpecifierAssistant().isAsync(node.getAccess())
				&& !question.assistantFactory.createPTypeAssistant().isType(((AOperationType) node.getType()).getResult(), AVoidType.class))
		{
			TypeCheckerErrors.report(3293, "Asynchronous operation "
					+ node.getName() + " cannot return a value", node.getLocation(), node);
		}

		if (question.assistantFactory.createPTypeAssistant().narrowerThan(node.getType(), node.getAccess()))
		{
			TypeCheckerErrors.report(3036, "Operation parameter visibility less than operation definition", node.getLocation(), node);
		}

		// The result variables are in scope for the post condition

		if (node.getPostdef() != null)
		{
			PType b = null;

			if (node.getResult() != null)
			{
				List<PDefinition> postdefs = question.assistantFactory.createAPatternTypePairAssistant().getDefinitions(node.getResult());
				FlatCheckedEnvironment post = new FlatCheckedEnvironment(question.assistantFactory, postdefs, local, NameScope.NAMESANDANYSTATE);
				post.setStatic(question.assistantFactory.createPAccessSpecifierAssistant().isStatic(node.getAccess()));
				post.setEnclosingDefinition(node.getPostdef());
				b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, post, NameScope.NAMESANDANYSTATE));
				post.unusedCheck();
			} else
			{
				FlatEnvironment post = new FlatEnvironment(question.assistantFactory, new Vector<PDefinition>(), local);
				post.setEnclosingDefinition(node.getPostdef());
				b = node.getPostdef().getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, post, NameScope.NAMESANDANYSTATE));
			}

			ABooleanBasicType expected = AstFactory.newABooleanBasicType(node.getLocation());

			if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
			{
				TypeCheckerErrors.report(3018, "Postcondition returns unexpected type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Actual", b, "Expected", expected);
			}
		}

		if (node.getErrors() != null)
		{
			for (AErrorCase error : node.getErrors())
			{
				TypeCheckInfo newQuestion = new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE);
				PType a = error.getLeft().apply(THIS, newQuestion);

				if (!question.assistantFactory.createPTypeAssistant().isType(a, ABooleanBasicType.class))
				{
					TypeCheckerErrors.report(3307, "Errs clause is not bool -> bool", error.getLeft().getLocation(), error.getLeft());
				}

				newQuestion.scope = NameScope.NAMESANDANYSTATE;
				PType b = error.getRight().apply(THIS, newQuestion);

				if (!question.assistantFactory.createPTypeAssistant().isType(b, ABooleanBasicType.class))
				{
					TypeCheckerErrors.report(3307, "Errs clause is not bool -> bool", error.getRight().getLocation(), error.getRight());
				}
			}
		}

		if (!(node.getBody() instanceof ANotYetSpecifiedStm)
				&& !(node.getBody() instanceof ASubclassResponsibilityStm))
		{
			local.unusedCheck();
		}
		// node.setType(node.getActualResult());
		return node.getType();
	}

	@Override
	public PType caseAImportedDefinition(AImportedDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		node.setType(node.getDef().apply(THIS, question));

		return node.getType();
	}

	@Override
	public PType caseAInheritedDefinition(AInheritedDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		node.setType(node.getSuperdef().apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseALocalDefinition(ALocalDefinition node,
			TypeCheckInfo question)
	{
		if (node.getType() != null)
		{
			node.setType(question.assistantFactory.createPTypeAssistant().typeResolve(node.getType(), null, THIS, question));
		}

		return node.getType();
	}

	@Override
	public PType caseAMultiBindListDefinition(AMultiBindListDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		if (node.getType() != null)
		{
			TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		}
		
		List<PDefinition> defs = new Vector<PDefinition>();

		for (PMultipleBind mb : node.getBindings())
		{
			PType type = mb.apply(THIS, question);
			defs.addAll(question.assistantFactory.createPMultipleBindAssistant().getDefinitions(mb, type, question));
		}

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(defs, THIS, question);
		node.setDefs(defs);
		return null;
	}

	@Override
	public PType caseAMutexSyncDefinition(AMutexSyncDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		SClassDefinition classdef = question.env.findClassDefinition();

		if (node.getOperations().isEmpty())
		{
			// Add all locally visibly callable operations for mutex(all)

			for (PDefinition def : node.getClassDefinition().apply(question.assistantFactory.getDefinitionCollector()))
				//SClassDefinitionAssistantTC.getLocalDefinitions(node.getClassDefinition()))
			{
				if (question.assistantFactory.createPDefinitionAssistant().isCallableOperation(def)
						&& !def.getName().getName().equals(classdef.getName().getName()))
				{
					node.getOperations().add(def.getName());
				}
			}
		}

		for (ILexNameToken opname : node.getOperations())
		{
			int found = 0;

			for (PDefinition def : classdef.getDefinitions())
			{
				if (def.getName() != null && def.getName().matches(opname))
				{
					found++;

					if (!question.assistantFactory.createPDefinitionAssistant().isCallableOperation(def))
					{
						TypeCheckerErrors.report(3038, opname
								+ " is not an explicit operation", opname.getLocation(), opname);
					}
				}
			}

			if (found == 0)
			{
				TypeCheckerErrors.report(3039, opname + " is not in scope", opname.getLocation(), opname);
			} else if (found > 1)
			{
				TypeCheckerErrors.warning(5002, "Mutex of overloaded operation", opname.getLocation(), opname);
			}

			if (opname.getName().equals(classdef.getName().getName()))
			{
				TypeCheckerErrors.report(3040, "Cannot put mutex on a constructor", opname.getLocation(), opname);
			}

			for (ILexNameToken other : node.getOperations())
			{
				if (opname != other && HelpLexNameToken.isEqual(opname, other))
				{
					TypeCheckerErrors.report(3041, "Duplicate mutex name", opname.getLocation(), opname);
				}
			}

		}
		return null;
	}

	@Override
	public PType caseANamedTraceDefinition(ANamedTraceDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		if (question.env.isVDMPP())
		{
			question = new TypeCheckInfo(question.assistantFactory, new FlatEnvironment(question.assistantFactory, question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node), question.env), question.scope, question.qualifiers);
		}

		for (ATraceDefinitionTerm term : node.getTerms())
		{
			question.assistantFactory.createPTraceDefinitionAssistant().typeCheck(term.getList(), THIS, new TypeCheckInfo(question.assistantFactory, question.env, NameScope.NAMESANDSTATE));
		}

		return null;
	}

	@Override
	public PType caseAPerSyncDefinition(APerSyncDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		Environment base = question.env;

		SClassDefinition classdef = base.findClassDefinition();
		int opfound = 0;
		int perfound = 0;
		Boolean isStatic = null;

		for (PDefinition def : classdef.getDefinitions())
		{
			if (def.getName() != null
					&& def.getName().matches(node.getOpname()))
			{
				opfound++;

				if (!question.assistantFactory.createPDefinitionAssistant().isCallableOperation(def))
				{
					TypeCheckerErrors.report(3042, node.getOpname()
							+ " is not an explicit operation", node.getOpname().getLocation(), node.getOpname());
				}

				if (isStatic != null && isStatic != question.assistantFactory.createPDefinitionAssistant().isStatic(def))
				{
					TypeCheckerErrors.report(3323, "Overloaded operation cannot mix static and non-static",
							node.getLocation(), node.getOpname());
				}
				
				isStatic = question.assistantFactory.createPDefinitionAssistant().isStatic(def);
			}

			if (def instanceof APerSyncDefinition)
			{
				APerSyncDefinition psd = (APerSyncDefinition) def;

				if (psd.getOpname().equals(node.getOpname()))
				{
					perfound++;
				}
			}
		}

		ILexNameToken opname = node.getOpname();

		if (opfound == 0)
		{
			TypeCheckerErrors.report(3043, opname + " is not in scope", opname.getLocation(), opname);
		}
		else if (opfound > 1)
		{
			TypeCheckerErrors.warning(5003, "Permission guard of overloaded operation", opname.getLocation(), opname);
		}

		if (perfound != 1)
		{
			TypeCheckerErrors.report(3044, "Duplicate permission guard found for "
					+ opname, opname.getLocation(), opname);
		}

		if (opname.getName().equals(classdef.getName().getName()))
		{
			TypeCheckerErrors.report(3045, "Cannot put guard on a constructor", opname.getLocation(), opname);
		}

		FlatCheckedEnvironment local = new FlatCheckedEnvironment(question.assistantFactory, node, base, NameScope.NAMESANDSTATE);
		local.setEnclosingDefinition(node); // Prevent op calls

		if (isStatic != null)
		{
			local.setStatic(isStatic);
		}
	
		PType rt = node.getGuard().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE));

		if (!question.assistantFactory.createPTypeAssistant().isType(rt, ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3046, "Guard is not a boolean expression", node.getGuard().getLocation(), node.getGuard());
		}

		node.setType(rt);
		return node.getType();
	}

	@Override
	public PType caseARenamedDefinition(ARenamedDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		node.setType(node.getDef().apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseAStateDefinition(AStateDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		Environment base = question.env;

		if (base.findStateDefinition() != node)
		{
			TypeCheckerErrors.report(3047, "Only one state definition allowed per module", node.getLocation(), node);
			return null;
		}
		
		for (PDefinition def: node.getStateDefs())
		{
			if (!def.getName().getOld())	// Don't check old names
			{
				TypeComparator.checkComposeTypes(def.getType(), question.env, false);
			}
		}

		question.assistantFactory.createPDefinitionListAssistant().typeCheck(node.getStateDefs(), THIS, question);

		if (node.getInvdef() != null)
		{
			node.getInvdef().apply(THIS, question);
		}

		if (node.getInitdef() != null)
		{
			node.getInitdef().apply(THIS, question);
		}

		return null;
	}

	@Override
	public PType caseAThreadDefinition(AThreadDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{

		question.scope = NameScope.NAMESANDSTATE;
		FlatEnvironment local = new FlatEnvironment(question.assistantFactory, question.assistantFactory.createPDefinitionAssistant().getSelfDefinition(node), question.env);

		PType rt = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));

		if (!(rt instanceof AVoidType) && !(rt instanceof AUnknownType))
		{
			TypeCheckerErrors.report(3049, "Thread statement/operation must not return a value", node.getLocation(), node);
		}

		node.setType(rt);
		node.getOperationDef().setBody(node.getStatement().clone());// This
																	// operation
																	// is a
																	// wrapper
																	// for the
																	// thread
		return rt;
	}

	@Override
	public PType caseATypeDefinition(ATypeDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		if (node.getInvdef() != null)
		{
			question.scope = NameScope.NAMES;
			node.getInvdef().apply(THIS, question);
		}

		PType type = question.assistantFactory.createPDefinitionAssistant().getType(node);
		node.setType(type);

		// We have to do the "top level" here, rather than delegating to the types
		// because the definition pointer from these top level types just refers
		// to the definition we are checking, which is never "narrower" than itself.
		// See the narrowerThan method in NamedType and RecordType.

		if (type instanceof ANamedInvariantType)
		{
			ANamedInvariantType ntype = (ANamedInvariantType) type;

			if (question.assistantFactory.createPTypeAssistant().narrowerThan(ntype.getType(), node.getAccess()))
			{
				TypeCheckerErrors.report(3321, "Type component visibility less than type's definition", node.getLocation(), node);
			}
			
			// Rebuild the compose definitions, after we check whether they already exist
			node.getComposeDefinitions().clear();

			for (PType compose: TypeComparator.checkComposeTypes(ntype.getType(), question.env, true))
			{
				ARecordInvariantType rtype = (ARecordInvariantType) compose;
				node.getComposeDefinitions().add(AstFactory.newATypeDefinition(rtype.getName(), rtype, null, null));
			}
		}
		else if (type instanceof ARecordInvariantType)
		{
			ARecordInvariantType rtype = (ARecordInvariantType) type;

			for (AFieldField field : rtype.getFields())
			{
				TypeComparator.checkComposeTypes(field.getType(), question.env, false);
				
				if (question.assistantFactory.createPTypeAssistant().narrowerThan(field.getType(), node.getAccess()))
				{
					TypeCheckerErrors.report(3321, "Field type visibility less than type's definition", field.getTagname().getLocation(), field.getTagname());
				}
			}
		}

		return node.getType();

	}

	@Override
	public PType caseAUntypedDefinition(AUntypedDefinition node,
			TypeCheckInfo question)
	{

		assert false : "Can't type check untyped definition?";
		return null;
	}

	@Override
	public PType caseAValueDefinition(AValueDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		if (node.getType() != null)
		{
			TypeComparator.checkComposeTypes(node.getType(), question.env, false);
		}
		
		// Enable constraint checking
		question = question.newConstraint(node.getType());
		
		question.qualifiers = null;
		PType expType = node.getExpression().apply(THIS, question);
		node.setExpType(expType);
		PType type = node.getType(); // PDefinitionAssistant.getType(node);
		if (expType instanceof AVoidType)
		{
			TypeCheckerErrors.report(3048, "Expression does not return a value", node.getExpression().getLocation(), node.getExpression());
		} else if (type != null)
		{
			if (!TypeComparator.compatible(type, expType))
			{
				TypeCheckerErrors.report(3051, "Expression does not match declared type", node.getLocation(), node);
				TypeCheckerErrors.detail2("Declared", type, "Expression", expType);
			}
		} else
		{
			type = expType;
			node.setType(expType);
		}

		Environment base = question.env;

		if (base.isVDMPP() && type instanceof ANamedInvariantType)
		{
			ANamedInvariantType named = (ANamedInvariantType) type;
			PDefinition typedef = base.findType(named.getName(), node.getLocation().getModule());

			if (typedef == null)
			{
				TypeCheckerErrors.report(2048, "Cannot find symbol "
						+ named.getName().toString(), named.getLocation(), named);
				return node.getType();
			}

			if (question.assistantFactory.createPAccessSpecifierAssistant().narrowerThan(typedef.getAccess(), node.getAccess()))
			{
				TypeCheckerErrors.report(3052, "Value type visibility less than value definition", node.getLocation(), node);
			}
		}

		node.apply(question.assistantFactory.getDefinitionTypeResolver(),new DefinitionTypeResolver.NewQuestion(THIS,question));
//		PPatternAssistantTC.typeResolve(pattern, THIS, question);
//		question.assistantFactory.getTypeResolver().updateDefs(node, question);
		question.qualifiers = null;
		question.assistantFactory.createPDefinitionListAssistant().typeCheck(node.getDefs(), THIS, question);
		return node.getType();
	}

	@Override
	public PType caseALetDefBindingTraceDefinition(
			ALetDefBindingTraceDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		Environment local = question.env;
		for (PDefinition d : node.getLocalDefs())
		{
			question.assistantFactory.createPDefinitionAssistant().typeResolve(d, THIS, question);
			d.apply(THIS, question);
			local = new FlatCheckedEnvironment(question.assistantFactory, d, local, question.scope);
		}

		node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck(question.env);

		return null;
	}

	@Override
	public PType caseALetBeStBindingTraceDefinition(
			ALetBeStBindingTraceDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		node.setDef(AstFactory.newAMultiBindListDefinition(node.getBind().getLocation(), question.assistantFactory.createPMultipleBindAssistant().getMultipleBindList(node.getBind())));
		node.getDef().apply(THIS, question);
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, node.getDef(), question.env, question.scope);

		if (node.getStexp() != null
				&& !question.assistantFactory.createPTypeAssistant().isType(node.getStexp().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope)), ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3225, "Such that clause is not boolean", node.getStexp().getLocation(), node);
		}

		node.getBody().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck();

		return null;
	}

	@Override
	public PType caseARepeatTraceDefinition(ARepeatTraceDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		// Environment local = question.env;
		node.getCore().apply(THIS, question);

		if (node.getFrom() > node.getTo())
		{
			TypeCheckerErrors.report(3277, "Trace repeat illegal values", node.getLocation(), node);
		}

		return null;
	}

	@Override
	public PType caseABracketedExpressionTraceCoreDefinition(
			ABracketedExpressionTraceCoreDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		for (ATraceDefinitionTerm term : node.getTerms())
		{
			for (PTraceDefinition def : term.getList())
			{
				def.apply(THIS, question);
			}
		}

		return null;
	}

	@Override
	public PType caseAApplyExpressionTraceCoreDefinition(
			AApplyExpressionTraceCoreDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		node.getCallStatement().apply(THIS, question);
		return null;
	}

}
