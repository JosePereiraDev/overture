package org.overture.typechecker.visitor;

import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.analysis.intf.IQuestionAnswer;
import org.overture.ast.definitions.AExplicitFunctionDefinition;
import org.overture.ast.definitions.AExplicitOperationDefinition;
import org.overture.ast.definitions.AImplicitOperationDefinition;
import org.overture.ast.definitions.AInstanceVariableDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.SClassDefinition;
import org.overture.ast.expressions.AIntLiteralExp;
import org.overture.ast.expressions.ARealLiteralExp;
import org.overture.ast.expressions.AVariableExp;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.Dialect;
import org.overture.ast.lex.LexNameToken;
import org.overture.ast.lex.LexStringToken;
import org.overture.ast.patterns.ADefPatternBind;
import org.overture.ast.patterns.AExpressionPattern;
import org.overture.ast.patterns.ASetBind;
import org.overture.ast.patterns.ATypeBind;
import org.overture.ast.patterns.PBind;
import org.overture.ast.statements.AAlwaysStm;
import org.overture.ast.statements.AAssignmentStm;
import org.overture.ast.statements.AAtomicStm;
import org.overture.ast.statements.ABlockSimpleBlockStm;
import org.overture.ast.statements.ACallObjectStm;
import org.overture.ast.statements.ACallStm;
import org.overture.ast.statements.ACaseAlternativeStm;
import org.overture.ast.statements.ACasesStm;
import org.overture.ast.statements.AClassInvariantStm;
import org.overture.ast.statements.ACyclesStm;
import org.overture.ast.statements.ADurationStm;
import org.overture.ast.statements.AElseIfStm;
import org.overture.ast.statements.AErrorCase;
import org.overture.ast.statements.AErrorStm;
import org.overture.ast.statements.AExitStm;
import org.overture.ast.statements.AExternalClause;
import org.overture.ast.statements.AForAllStm;
import org.overture.ast.statements.AForIndexStm;
import org.overture.ast.statements.AForPatternBindStm;
import org.overture.ast.statements.AIfStm;
import org.overture.ast.statements.ALetBeStStm;
import org.overture.ast.statements.ALetStm;
import org.overture.ast.statements.ANonDeterministicSimpleBlockStm;
import org.overture.ast.statements.ANotYetSpecifiedStm;
import org.overture.ast.statements.APeriodicStm;
import org.overture.ast.statements.AReturnStm;
import org.overture.ast.statements.ASkipStm;
import org.overture.ast.statements.ASpecificationStm;
import org.overture.ast.statements.AStartStm;
import org.overture.ast.statements.ASubclassResponsibilityStm;
import org.overture.ast.statements.ATixeStm;
import org.overture.ast.statements.ATixeStmtAlternative;
import org.overture.ast.statements.ATrapStm;
import org.overture.ast.statements.AWhileStm;
import org.overture.ast.statements.PStm;
import org.overture.ast.statements.SSimpleBlockStm;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.ABooleanBasicType;
import org.overture.ast.types.AClassType;
import org.overture.ast.types.AFunctionType;
import org.overture.ast.types.AOperationType;
import org.overture.ast.types.ASetType;
import org.overture.ast.types.AUnionType;
import org.overture.ast.types.AUnknownType;
import org.overture.ast.types.AVoidType;
import org.overture.ast.types.PType;
import org.overture.ast.util.PTypeSet;
import org.overture.config.Settings;
import org.overture.typechecker.Environment;
import org.overture.typechecker.FlatCheckedEnvironment;
import org.overture.typechecker.FlatEnvironment;
import org.overture.typechecker.PrivateClassEnvironment;
import org.overture.typechecker.PublicClassEnvironment;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;
import org.overture.typechecker.TypeComparator;
import org.overture.typechecker.assistant.definition.PAccessSpecifierAssistantTC;
import org.overture.typechecker.assistant.definition.PDefinitionAssistantTC;
import org.overture.typechecker.assistant.definition.PDefinitionListAssistantTC;
import org.overture.typechecker.assistant.definition.SClassDefinitionAssistantTC;
import org.overture.typechecker.assistant.pattern.ATypeBindAssistantTC;
import org.overture.typechecker.assistant.pattern.PBindAssistantTC;
import org.overture.typechecker.assistant.pattern.PMultipleBindAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternAssistantTC;
import org.overture.typechecker.assistant.pattern.PPatternBindAssistantTC;
import org.overture.typechecker.assistant.statement.ABlockSimpleBlockStmAssistantTC;
import org.overture.typechecker.assistant.statement.ACallObjectStatementAssistantTC;
import org.overture.typechecker.assistant.statement.ACallStmAssistantTC;
import org.overture.typechecker.assistant.statement.ANonDeterministicSimpleBlockStmAssistantTC;
import org.overture.typechecker.assistant.statement.PStateDesignatorAssistantTC;
import org.overture.typechecker.assistant.statement.PStmAssistantTC;
import org.overture.typechecker.assistant.type.PTypeAssistantTC;

public class TypeCheckerStmVisitor extends AbstractTypeCheckVisitor
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -6418355785507933395L;

	public TypeCheckerStmVisitor(
			IQuestionAnswer<TypeCheckInfo, PType> typeCheckVisitor)
	{
		super(typeCheckVisitor);

	}

	@Override
	public PType caseAAlwaysStm(AAlwaysStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		node.getAlways().apply(THIS, question);
		node.setType(node.getBody().apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseAAssignmentStm(AAssignmentStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		node.setTargetType(node.getTarget().apply(THIS, new TypeCheckInfo(question.assistantFactory, question.env)));
		node.setExpType(node.getExp().apply(THIS, new TypeCheckInfo(question.assistantFactory, question.env, question.scope)));

		if (!TypeComparator.compatible(node.getTargetType(), node.getExpType()))
		{
			TypeCheckerErrors.report(3239, "Incompatible types in assignment", node.getLocation(), node);
			TypeCheckerErrors.detail2("Target", node.getTarget(), "Expression", node.getExp());
		}

		node.setClassDefinition(question.env.findClassDefinition());
		node.setStateDefinition(question.env.findStateDefinition());

		PDefinition encl = question.env.getEnclosingDefinition();

		if (encl != null)
		{
			if (encl instanceof AExplicitOperationDefinition)
			{
				AExplicitOperationDefinition op = (AExplicitOperationDefinition) encl;
				node.setInConstructor(op.getIsConstructor());
			} else if (encl instanceof AImplicitOperationDefinition)
			{
				AImplicitOperationDefinition op = (AImplicitOperationDefinition) encl;
				node.setInConstructor(op.getIsConstructor());
			}
		}

		if (node.getInConstructor())
		{
			// Mark assignment target as initialized (so no warnings)
			PDefinition state;
			state = PStateDesignatorAssistantTC.targetDefinition(node.getTarget(), question);

			if (state instanceof AInstanceVariableDefinition)
			{
				AInstanceVariableDefinition iv = (AInstanceVariableDefinition) state;
				iv.setInitialized(true);
			}
		}

		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAAtomicStm(AAtomicStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		for (AAssignmentStm stmt : node.getAssignments())
		{
			stmt.apply(THIS, question);
		}

		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAForPatternBindStm(AForPatternBindStm node,
			TypeCheckInfo question) throws AnalysisException
	{

		PType stype = node.getExp().apply(THIS, new TypeCheckInfo(question.assistantFactory, question.env, question.scope));
		Environment local = question.env;

		if (PTypeAssistantTC.isSeq(stype))
		{
			node.setSeqType(PTypeAssistantTC.getSeq(stype));
			node.getPatternBind().apply(THIS, new TypeCheckInfo(question.assistantFactory, question.env, question.scope));
			List<PDefinition> defs = PPatternBindAssistantTC.getDefinitions(node.getPatternBind());
			PDefinitionListAssistantTC.typeCheck(defs, THIS, new TypeCheckInfo(question.assistantFactory, question.env, question.scope));
			local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
		} else
		{
			TypeCheckerErrors.report(3223, "Expecting sequence type after 'in'", node.getLocation(), node);
		}

		PType rt = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck();
		node.setType(rt);
		return rt;
	}

	@Override
	public PType defaultSSimpleBlockStm(SSimpleBlockStm node,
			TypeCheckInfo question) throws AnalysisException
	{
		boolean notreached = false;
		PTypeSet rtypes = new PTypeSet();
		PType last = null;

		for (PStm stmt : node.getStatements())
		{
			PType stype = stmt.apply(THIS, question);

			if (notreached)
			{
				TypeCheckerErrors.warning(5006, "Statement will not be reached", stmt.getLocation(), stmt);
			} else
			{
				last = stype;
				notreached = true;

				if (stype instanceof AUnionType)
				{
					AUnionType ust = (AUnionType) stype;

					for (PType t : ust.getTypes())
					{
						ABlockSimpleBlockStmAssistantTC.addOne(rtypes, t);

						if (t instanceof AVoidType || t instanceof AUnknownType)
						{
							notreached = false;
						}
					}
				} else
				{
					ABlockSimpleBlockStmAssistantTC.addOne(rtypes, stype);

					if (stype instanceof AVoidType
							|| stype instanceof AUnknownType)
					{
						notreached = false;
					}
				}
			}
		}

		// If the last statement reached has a void component, add this to the
		// overall
		// return type, as the block may return nothing.

		if (last != null
				&& (PTypeAssistantTC.isType(last, AVoidType.class) || PTypeAssistantTC.isUnknown(last)))
		{
			rtypes.add(AstFactory.newAVoidType(node.getLocation()));
		}

		node.setType(rtypes.isEmpty() ? AstFactory.newAVoidType(node.getLocation())
				: rtypes.getType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseABlockSimpleBlockStm(ABlockSimpleBlockStm node,
			TypeCheckInfo question) throws AnalysisException
	{
		// Each dcl definition is in scope for later definitions...

		Environment local = question.env;

		for (PDefinition d : node.getAssignmentDefs())
		{
			local = new FlatCheckedEnvironment(question.assistantFactory, d, local, question.scope); // cumulative
			PDefinitionAssistantTC.implicitDefinitions(d, local);
			d.apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		}

		// For type checking purposes, the definitions are treated as
		// local variables. At runtime (below) they have to be treated
		// more like (updatable) state.

		PType r = defaultSSimpleBlockStm(node, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck(question.env);
		node.setType(r);
		return r;
	}

	@Override
	public PType caseACallObjectStm(ACallObjectStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		PType dtype = node.getDesignator().apply(THIS, question);

		if (!PTypeAssistantTC.isClass(dtype))
		{
			TypeCheckerErrors.report(3207, "Object designator is not an object type", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}

		AClassType ctype = PTypeAssistantTC.getClassType(dtype);

		SClassDefinition classdef = ctype.getClassdef();
		SClassDefinition self = question.env.findClassDefinition();
		Environment classenv = null;

		if (self == classdef
				|| PDefinitionAssistantTC.hasSupertype(self, question.assistantFactory.createPDefinitionAssistant().getType(classdef)))
		{
			// All fields visible. Note that protected fields are inherited
			// into "locals" so they are effectively private
			classenv = new PrivateClassEnvironment(question.assistantFactory, self);
		} else
		{
			// Only public fields externally visible
			classenv = new PublicClassEnvironment(question.assistantFactory, classdef);
		}

		if (node.getClassname() == null)
		{
			node.setField(new LexNameToken(ctype.getName().getName(), node.getFieldname().getName(), node.getFieldname().getLocation()));
		} else
		{
			node.setField(node.getClassname());
		}

		node.getField().getLocation().executable(true);
		List<PType> atypes = ACallObjectStatementAssistantTC.getArgTypes(node.getArgs(), THIS, question);
		node.getField().setTypeQualifier(atypes);
		PDefinition fdef = classenv.findName(node.getField(), question.scope);

		// Special code for the deploy method of CPU

		if (Settings.dialect == Dialect.VDM_RT
				&& node.getField().getModule().equals("CPU")
				&& node.getField().getName().equals("deploy"))
		{

			if (!PTypeAssistantTC.isType(atypes.get(0), AClassType.class))
			{
				TypeCheckerErrors.report(3280, "Argument to deploy must be an object", node.getArgs().get(0).getLocation(), node.getArgs().get(0));
			}

			node.setType(AstFactory.newAVoidType(node.getLocation()));
			return node.getType();
		} else if (Settings.dialect == Dialect.VDM_RT
				&& node.getField().getModule().equals("CPU")
				&& node.getField().getName().equals("setPriority"))
		{
			if (!(atypes.get(0) instanceof AOperationType))
			{
				TypeCheckerErrors.report(3290, "Argument to setPriority must be an operation", node.getArgs().get(0).getLocation(), node.getArgs().get(0));
			} else
			{
				// Convert the variable expression to a string...
				AVariableExp a1 = (AVariableExp) node.getArgs().get(0);
				node.getArgs().remove(0);
				node.getArgs().add(0, AstFactory.newAStringLiteralExp(new LexStringToken(a1.getName().getExplicit(true).getFullName(), a1.getLocation())));

				if (a1.getName().getModule().equals(a1.getName().getName())) // it's a
				// constructor
				{
					TypeCheckerErrors.report(3291, "Argument to setPriority cannot be a constructor", node.getArgs().get(0).getLocation(), node.getArgs().get(0));

				}
			}

			node.setType(AstFactory.newAVoidType(node.getLocation()));
			return node.getType();
		} else if (fdef == null)
		{
			TypeCheckerErrors.report(3209, "Member " + node.getField()
					+ " is not in scope", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		} else if (question.assistantFactory.createPDefinitionAssistant().isStatic(fdef)
				&& !question.env.isStatic())
		{
			// warning(5005, "Should invoke member " + field +
			// " from a static context");
		}

		PType type = question.assistantFactory.createPDefinitionAssistant().getType(fdef);

		if (PTypeAssistantTC.isOperation(type))
		{
			AOperationType optype = PTypeAssistantTC.getOperation(type);
			optype.apply(THIS, question);
			node.getField().setTypeQualifier(optype.getParameters());
			ACallObjectStatementAssistantTC.checkArgTypes(type, optype.getParameters(), atypes); // Not necessary?
			node.setType(optype.getResult());
			return node.getType();
		} else if (PTypeAssistantTC.isFunction(type))
		{
			// This is the case where a function is called as an operation
			// without
			// a "return" statement.

			AFunctionType ftype = PTypeAssistantTC.getFunction(type);
			ftype.apply(THIS, question);
			node.getField().setTypeQualifier(ftype.getParameters());
			ACallObjectStatementAssistantTC.checkArgTypes(type, ftype.getParameters(), atypes); // Not necessary?
			node.setType(ftype.getResult());
			return node.getType();
		} else
		{
			TypeCheckerErrors.report(3210, "Object member is neither a function nor an operation", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}
	}

	@Override
	public PType caseACallStm(ACallStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		List<PType> atypes = ACallObjectStatementAssistantTC.getArgTypes(node.getArgs(), THIS, question);

		if (question.env.isVDMPP())
		{
			node.getName().setTypeQualifier(atypes);
		}

		PDefinition opdef = question.env.findName(node.getName(), question.scope);

		if (opdef == null)
		{
			TypeCheckerErrors.report(3213, "Operation " + node.getName()
					+ " is not in scope", node.getLocation(), node);
			question.env.listAlternatives(node.getName());
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}

		if (!question.assistantFactory.createPDefinitionAssistant().isStatic(opdef)
				&& question.env.isStatic())
		{
			TypeCheckerErrors.report(3214, "Cannot call " + node.getName()
					+ " from static context", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}

		PType type = question.assistantFactory.createPDefinitionAssistant().getType(opdef);

		if (PTypeAssistantTC.isOperation(type))
		{
			AOperationType optype = PTypeAssistantTC.getOperation(type);

			question.assistantFactory.createPTypeAssistant().typeResolve(optype, null, THIS, question);
			// Reset the name's qualifier with the actual operation type so
			// that runtime search has a simple TypeComparator call.

			if (question.env.isVDMPP())
			{
				node.getName().setTypeQualifier(optype.getParameters());
			}

			ACallStmAssistantTC.checkArgTypes(node, optype, optype.getParameters(), atypes);
			node.setType(optype.getResult());
			return optype.getResult();
		} else if (PTypeAssistantTC.isFunction(type))
		{
			// This is the case where a function is called as an operation
			// without
			// a "return" statement.

			AFunctionType ftype = PTypeAssistantTC.getFunction(type);
			question.assistantFactory.createPTypeAssistant().typeResolve(ftype, null, THIS, question);

			// Reset the name's qualifier with the actual function type so
			// that runtime search has a simple TypeComparator call.

			if (question.env.isVDMPP())
			{
				node.getName().setTypeQualifier(ftype.getParameters());
			}

			ACallStmAssistantTC.checkArgTypes(node, ftype, ftype.getParameters(), atypes);
			node.setType(ftype.getResult());
			return ftype.getResult();
		} else
		{
			TypeCheckerErrors.report(3210, "Name is neither a function nor an operation", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}
	}

	// TODO correct, possibly wrong typecheck implementation
	@Override
	public PType caseACaseAlternativeStm(ACaseAlternativeStm node,
			TypeCheckInfo question) throws AnalysisException
	{

		if (node.getDefs().size() == 0)
		{
			node.setDefs(new LinkedList<PDefinition>());
			PPatternAssistantTC.typeResolve(node.getPattern(), THIS, question);

			if (node.getPattern() instanceof AExpressionPattern)
			{
				// Only expression patterns need type checking...
				AExpressionPattern ep = (AExpressionPattern) node.getPattern();
				PType ptype = ep.getExp().apply(THIS, question);

				if (!TypeComparator.compatible(ptype, node.getCtype()))
				{
					TypeCheckerErrors.report(3311, "Pattern cannot match", node.getPattern().getLocation(), node.getPattern());
				}
			}

			PPatternAssistantTC.typeResolve(node.getPattern(), THIS, question);

			ACasesStm stm = (ACasesStm) node.parent();
			node.getDefs().addAll(PPatternAssistantTC.getDefinitions(node.getPattern(), stm.getExp().getType(), NameScope.LOCAL));
		}

		PDefinitionListAssistantTC.typeCheck(node.getDefs(), THIS, question);

		if (!PPatternAssistantTC.matches(node.getPattern(), node.getCtype()))
		{
			TypeCheckerErrors.report(3311, "Pattern cannot match", node.getPattern().getLocation(), node.getPattern());
		}

		Environment local = new FlatCheckedEnvironment(question.assistantFactory, node.getDefs(), question.env, question.scope);
		PType r = node.getResult().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck();

		return r;
	}

	@Override
	public PType caseACasesStm(ACasesStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		PType expType = node.getExp().apply(THIS, question);

		PTypeSet rtypes = new PTypeSet();

		for (ACaseAlternativeStm c : node.getCases())
		{
			c.setCtype(expType);
			rtypes.add(c.apply(THIS, question));
		}

		if (node.getOthers() != null)
		{
			rtypes.add(node.getOthers().apply(THIS, question));
		} else
		{
			rtypes.add(AstFactory.newAVoidType(node.getLocation()));
		}

		node.setType(rtypes.getType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAClassInvariantStm(AClassInvariantStm node,
			TypeCheckInfo question)
	{
		// Definitions already checked.
		node.setType(AstFactory.newABooleanBasicType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseACyclesStm(ACyclesStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		if (node.getCycles() instanceof AIntLiteralExp)
		{
			AIntLiteralExp i = (AIntLiteralExp) node.getCycles();

			if (i.getValue().getValue() < 0)
			{
				TypeCheckerErrors.report(3282, "Argument to cycles must be integer >= 0", node.getCycles().getLocation(), node.getCycles());
			}

			node.setValue(i.getValue().getValue());
		} else if (node.getCycles() instanceof ARealLiteralExp)
		{
			ARealLiteralExp i = (ARealLiteralExp) node.getCycles();

			if (i.getValue().getValue() < 0
					|| Math.floor(i.getValue().getValue()) != i.getValue().getValue())
			{
				TypeCheckerErrors.report(3282, "Argument to cycles must be integer >= 0", node.getCycles().getLocation(), node.getCycles());
			}

			node.setValue((long) i.getValue().getValue());
		} else
		{
			TypeCheckerErrors.report(3282, "Argument to cycles must be integer >= 0", node.getCycles().getLocation(), node.getCycles());
		}

		node.setType(node.getStatement().apply(THIS, question));
		return node.getType();
	}

	// TODO: Missing the other DefStatement

	@Override
	public PType caseALetStm(ALetStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		// Each local definition is in scope for later local definitions...

		Environment local = question.env;

		for (PDefinition d : node.getLocalDefs())
		{
			if (d instanceof AExplicitFunctionDefinition)
			{
				// Functions' names are in scope in their bodies, whereas
				// simple variable declarations aren't

				local = new FlatCheckedEnvironment(question.assistantFactory, d, local, question.scope); // cumulative
				PDefinitionAssistantTC.implicitDefinitions(d, local);
				PDefinitionAssistantTC.typeResolve(d, THIS, new TypeCheckInfo(question.assistantFactory, local));

				if (question.env.isVDMPP())
				{
					SClassDefinition cdef = question.env.findClassDefinition();
					d.setClassDefinition(cdef);
					d.setAccess(PAccessSpecifierAssistantTC.getStatic(d, true));
				}

				d.apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
			} else
			{
				PDefinitionAssistantTC.implicitDefinitions(d, local);
				PDefinitionAssistantTC.typeResolve(d, THIS, question);
				d.apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
				local = new FlatCheckedEnvironment(question.assistantFactory, d, local, question.scope); // cumulative
			}
		}

		PType r = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck(question.env);
		node.setType(r);
		return r;
	}

	@Override
	public PType caseADurationStm(ADurationStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		PType argType = node.getDuration().apply(THIS, question);

		if (!TypeComparator.compatible(AstFactory.newANatNumericBasicType(node.getLocation()), argType))
		{
			TypeCheckerErrors.report(3281, "Arguments to duration must be a nat", node.getLocation(), node);
			TypeCheckerErrors.detail("Actual", argType);
		}

		return node.getStatement().apply(THIS, question);
	}

	@Override
	public PType caseAElseIfStm(AElseIfStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		if (!PTypeAssistantTC.isType(node.getElseIf().apply(THIS, question), ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3218, "Expression is not boolean", node.getLocation(), node);
		}

		node.setType(node.getThenStm().apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseAErrorStm(AErrorStm node, TypeCheckInfo question)
	{
		node.setType(AstFactory.newAUnknownType(node.getLocation()));
		return node.getType(); // Because we terminate anyway
	}

	@Override
	public PType caseAExitStm(AExitStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		if (node.getExpression() != null)
		{
			node.setExpType(node.getExpression().apply(THIS, question));
		}

		// This is unknown because the statement doesn't actually return a
		// value - so if this is the only statement in a body, it is not a
		// type error (should return the same type as the definition return
		// type).

		node.setType(AstFactory.newAUnknownType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAForAllStm(AForAllStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		node.setType(node.getSet().apply(THIS, question));
		PPatternAssistantTC.typeResolve(node.getPattern(), THIS, question);

		if (PTypeAssistantTC.isSet(node.getType()))
		{
			ASetType st = PTypeAssistantTC.getSet(node.getType());
			List<PDefinition> defs = PPatternAssistantTC.getDefinitions(node.getPattern(), st.getSetof(), NameScope.LOCAL);

			Environment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
			PType rt = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
			local.unusedCheck();
			node.setType(rt);
			return rt;
		} else
		{
			TypeCheckerErrors.report(3219, "For all statement does not contain a set type", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}
	}

	@Override
	public PType caseAForIndexStm(AForIndexStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		PType ft = node.getFrom().apply(THIS, question);
		PType tt = node.getTo().apply(THIS, question);

		if (!PTypeAssistantTC.isNumeric(ft))
		{
			TypeCheckerErrors.report(3220, "From type is not numeric", node.getLocation(), node);
		}

		if (!PTypeAssistantTC.isNumeric(tt))
		{
			TypeCheckerErrors.report(3221, "To type is not numeric", node.getLocation(), node);
		}

		if (node.getBy() != null)
		{
			PType bt = node.getBy().apply(THIS, question);

			if (!PTypeAssistantTC.isNumeric(bt))
			{
				TypeCheckerErrors.report(3222, "By type is not numeric", node.getLocation(), node);
			}
		}

		PDefinition vardef = AstFactory.newALocalDefinition(node.getVar().getLocation(), node.getVar(), NameScope.LOCAL, ft);
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, vardef, question.env, question.scope);
		PType rt = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck();
		node.setType(rt);
		return rt;
	}

	@Override
	public PType caseAIfStm(AIfStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		PType test = node.getIfExp().apply(THIS, question);

		if (!PTypeAssistantTC.isType(test, ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3224, "If expression is not boolean", node.getIfExp().getLocation(), node.getIfExp());
		}

		PTypeSet rtypes = new PTypeSet();
		rtypes.add(node.getThenStm().apply(THIS, question));

		if (node.getElseIf() != null)
		{
			for (AElseIfStm stmt : node.getElseIf())
			{
				rtypes.add(stmt.apply(THIS, question));
			}
		}

		if (node.getElseStm() != null)
		{
			rtypes.add(node.getElseStm().apply(THIS, question));
		} else
		{
			rtypes.add(AstFactory.newAVoidType(node.getLocation()));
		}

		node.setType(rtypes.getType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseALetBeStStm(ALetBeStStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		node.setDef(AstFactory.newAMultiBindListDefinition(node.getLocation(), PMultipleBindAssistantTC.getMultipleBindList(node.getBind())));
		node.getDef().apply(THIS, question);
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, node.getDef(), question.env, question.scope);

		if (node.getSuchThat() != null
				&& !PTypeAssistantTC.isType(node.getSuchThat().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope)), ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3225, "Such that clause is not boolean", node.getLocation(), node);
		}

		PType r = node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope));
		local.unusedCheck();
		node.setType(r);
		return r;
	}

	@Override
	public PType caseANonDeterministicSimpleBlockStm(
			ANonDeterministicSimpleBlockStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		// PType r = defaultSSimpleBlockStm(node,question);

		PTypeSet rtypes = new PTypeSet();
		int rcount = 0;

		for (PStm stmt : node.getStatements())
		{
			PType stype = stmt.apply(THIS, question);

			if (PTypeAssistantTC.isType(stype, AUnionType.class))
			{
				AUnionType ust = (AUnionType) stype;
				for (PType t : ust.getTypes())
				{
					if (ANonDeterministicSimpleBlockStmAssistantTC.addOne(rtypes, t))
						rcount++;
				}
			} else
			{
				if (ANonDeterministicSimpleBlockStmAssistantTC.addOne(rtypes, stype))
					rcount++;
			}
		}

		if (rcount > 1)
		{
			TypeCheckerErrors.warning(5016, "Some statements will not be reached", node.getLocation(), node);
		}

		node.setType(rtypes.isEmpty() ? AstFactory.newAVoidType(node.getLocation())
				: rtypes.getType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseANotYetSpecifiedStm(ANotYetSpecifiedStm node,
			TypeCheckInfo question)
	{
		node.setType(AstFactory.newAUnknownType(node.getLocation())); // Because
																		// we
																		// terminate
																		// anyway
		return node.getType();
	}

	@Override
	public PType caseAReturnStm(AReturnStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		if (node.getExpression() == null)
		{
			node.setType(AstFactory.newAVoidReturnType(node.getLocation()));
			return node.getType();
		} else
		{
			node.setType(node.getExpression().apply(THIS, question));
			return node.getType();
		}
	}

	@Override
	public PType caseASkipStm(ASkipStm node, TypeCheckInfo question)
	{
		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseASpecificationStm(ASpecificationStm node,
			TypeCheckInfo question) throws AnalysisException
	{

		List<PDefinition> defs = new LinkedList<PDefinition>();

		// Now we build local definitions for each of the externals, so
		// that they can be added to the local environment, while the
		// global state is made inaccessible.

		if (node.getExternals() != null)
		{
			for (AExternalClause clause : node.getExternals())
			{
				for (ILexNameToken name : clause.getIdentifiers())
				{
					if (question.env.findName(name, NameScope.STATE) == null)
					{
						TypeCheckerErrors.report(3274, "External variable is not in scope: "
								+ name, name.getLocation(), name);
					} else
					{
						defs.add(AstFactory.newALocalDefinition(name.getLocation(), name, NameScope.STATE, clause.getType()));
					}
				}
			}
		}

		if (node.getErrors() != null)
		{
			for (AErrorCase err : node.getErrors())
			{
				PType lt = err.getLeft().apply(THIS, question);
				PType rt = err.getRight().apply(THIS, question);

				if (!PTypeAssistantTC.isType(lt, ABooleanBasicType.class))
				{
					TypeCheckerErrors.report(3275, "Error clause must be a boolean", err.getLeft().getLocation(), err.getLeft());
				}

				if (!PTypeAssistantTC.isType(rt, ABooleanBasicType.class))
				{
					TypeCheckerErrors.report(3275, "Error clause must be a boolean", err.getRight().getLocation(), err.getRight());
				}
			}
		}

		PDefinitionListAssistantTC.typeCheck(defs, THIS, question);
		Environment local = new FlatEnvironment(question.assistantFactory, defs, question.env); // NB. No
		// check
		// //Unused

		if (node.getPrecondition() != null
				&& !PTypeAssistantTC.isType(node.getPrecondition().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDSTATE)), ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3233, "Precondition is not a boolean expression", node.getPrecondition().getLocation(), node.getPrecondition());
		}

		if (node.getPostcondition() != null
				&& !PTypeAssistantTC.isType(node.getPostcondition().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, NameScope.NAMESANDANYSTATE)), ABooleanBasicType.class))
		{
			TypeCheckerErrors.report(3234, "Postcondition is not a boolean expression", node.getPostcondition().getLocation(), node.getPostcondition());
		}

		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseATrapStm(ATrapStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		PTypeSet rtypes = new PTypeSet();

		PStm body = node.getBody();

		PType bt = body.apply(THIS, question);
		rtypes.add(bt);

		PTypeSet extype = PStmAssistantTC.exitCheck(body);
		PType ptype = null;

		if (extype.isEmpty())
		{
			TypeCheckerErrors.report(3241, "Body of trap statement does not throw exceptions", node.getLocation(), node);
			ptype = AstFactory.newAUnknownType(body.getLocation());
		} else
		{
			ptype = extype.getType(body.getLocation());
		}
		node.setType(ptype);
		node.getPatternBind().apply(THIS, question);
		// TODO: PatternBind stuff
		List<PDefinition> defs = PPatternBindAssistantTC.getDefinitions(node.getPatternBind());
		PDefinitionListAssistantTC.typeCheck(defs, THIS, question);
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
		rtypes.add(node.getWith().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope, question.qualifiers)));

		node.setType(rtypes.getType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAWhileStm(AWhileStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		question.qualifiers = null;
		node.getExp().apply(THIS, question);
		node.setType(node.getStatement().apply(THIS, question));
		return node.getType();
	}

	@Override
	public PType caseAPeriodicStm(APeriodicStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		int nargs = (Settings.dialect == Dialect.VDM_RT) ? 4 : 1;
		List<PExp> args = node.getArgs();

		if (args.size() != nargs)
		{
			TypeCheckerErrors.report(3287, "Periodic thread must have " + nargs
					+ " argument(s)", node.getLocation(), node);
		} else
		{

			for (PExp arg : args)
			{
				PType type = arg.apply(THIS, question);

				if (!PTypeAssistantTC.isNumeric(type))
				{
					TypeCheckerErrors.report(3316, "Expecting number in periodic argument", arg.getLocation(), arg);
				}
			}
		}

		ILexNameToken opname = node.getOpname();

		opname.setTypeQualifier(new LinkedList<PType>());
		opname.getLocation().hit();
		PDefinition opdef = question.env.findName(opname, NameScope.NAMES);

		if (opdef == null)
		{
			TypeCheckerErrors.report(3228, opname + " is not in scope", node.getLocation(), node);
			node.setType(AstFactory.newAUnknownType(node.getLocation()));
			return node.getType();
		}

		// Operation must be "() ==> ()"

		AOperationType expected = AstFactory.newAOperationType(node.getLocation(), new Vector<PType>(), AstFactory.newAVoidType(node.getLocation()));

		opdef = PDefinitionAssistantTC.deref(opdef);

		if (opdef instanceof AExplicitOperationDefinition)
		{
			AExplicitOperationDefinition def = (AExplicitOperationDefinition) opdef;

			if (!PTypeAssistantTC.equals(def.getType(), expected))
			{
				TypeCheckerErrors.report(3229, opname
						+ " should have no parameters or return type", node.getLocation(), node);
				TypeCheckerErrors.detail("Actual", def.getType());
			}
		} else if (opdef instanceof AImplicitOperationDefinition)
		{
			AImplicitOperationDefinition def = (AImplicitOperationDefinition) opdef;

			if (def.getBody() == null)
			{
				TypeCheckerErrors.report(3230, opname + " is implicit", node.getLocation(), node);
			}

			if (!PTypeAssistantTC.equals(def.getType(), expected))
			{
				TypeCheckerErrors.report(3231, opname
						+ " should have no parameters or return type", node.getLocation(), node);
				TypeCheckerErrors.detail("Actual", def.getType());
			}
		} else
		{
			TypeCheckerErrors.report(3232, opname + " is not an operation name", node.getLocation(), node);
		}

		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseAStartStm(AStartStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		PType type = node.getObj().apply(THIS, question);

		if (PTypeAssistantTC.isSet(type))
		{
			ASetType set = PTypeAssistantTC.getSet(type);

			if (!PTypeAssistantTC.isClass(set.getSetof()))
			{
				TypeCheckerErrors.report(3235, "Expression is not a set of object references", node.getObj().getLocation(), node.getObj());
			} else
			{
				AClassType ctype = PTypeAssistantTC.getClassType(set.getSetof());

				if (SClassDefinitionAssistantTC.findThread(ctype.getClassdef()) == null)
				{
					TypeCheckerErrors.report(3236, "Class does not define a thread", node.getObj().getLocation(), node.getObj());
				}
			}
		} else if (PTypeAssistantTC.isClass(type))
		{
			AClassType ctype = PTypeAssistantTC.getClassType(type);

			if (SClassDefinitionAssistantTC.findThread(ctype.getClassdef()) == null)
			{
				TypeCheckerErrors.report(3237, "Class does not define a thread", node.getObj().getLocation(), node.getObj());
			}
		} else
		{
			TypeCheckerErrors.report(3238, "Expression is not an object reference or set of object references", node.getObj().getLocation(), node.getObj());
		}

		node.setType(AstFactory.newAVoidType(node.getLocation()));
		return node.getType();
	}

	@Override
	public PType caseASubclassResponsibilityStm(
			ASubclassResponsibilityStm node, TypeCheckInfo question)
	{
		node.setType(AstFactory.newAUnknownType(node.getLocation())); // Because
																		// we
																		// terminate
																		// anyway
		return node.getType();
	}

	@Override
	public PType caseATixeStm(ATixeStm node, TypeCheckInfo question)
			throws AnalysisException
	{

		PType rt = node.getBody().apply(THIS, question);
		PTypeSet extypes = PStmAssistantTC.exitCheck(node.getBody());

		if (!extypes.isEmpty())
		{
			PType union = extypes.getType(node.getLocation());

			for (ATixeStmtAlternative tsa : node.getTraps())
			{
				tsa.setExp(union);
				tsa.apply(THIS, question);
			}
		}
		node.setType(rt);
		return rt;
	}

	@Override
	public PType caseATixeStmtAlternative(ATixeStmtAlternative node,
			TypeCheckInfo question) throws AnalysisException
	{

		// TODO fix
		// patternBind.typeCheck(base, scope, ext)
		// PPatternBindAssistant.typeCheck(node.getPatternBind(), null,
		// THIS, question);
		// DefinitionList defs = patternBind.getDefinitions();
		node.getPatternBind().apply(THIS, new TypeCheckInfo(question.assistantFactory, question.env, question.scope));
		List<PDefinition> defs = PPatternBindAssistantTC.getDefinitions(node.getPatternBind());
		PDefinitionListAssistantTC.typeCheck(defs, THIS, question);
		Environment local = new FlatCheckedEnvironment(question.assistantFactory, defs, question.env, question.scope);
		node.getStatement().apply(THIS, new TypeCheckInfo(question.assistantFactory, local, question.scope, question.qualifiers));
		local.unusedCheck();

		return null;
	}

	@Override
	public PType caseADefPatternBind(ADefPatternBind node,
			TypeCheckInfo question) throws AnalysisException
	{

		node.setDefs(null);

		PBind bind = node.getBind();
		PType type = node.getType();

		if (bind != null)
		{
			if (bind instanceof ATypeBind)
			{
				ATypeBind typebind = (ATypeBind) bind;
				ATypeBindAssistantTC.typeResolve(typebind, THIS, question);

				if (!TypeComparator.compatible(typebind.getType(), type))
				{
					TypeCheckerErrors.report(3198, "Type bind not compatible with expression", bind.getLocation(), bind);
					TypeCheckerErrors.detail2("Bind", typebind.getType(), "Exp", type);
				}
			} else
			{
				ASetBind setbind = (ASetBind) bind;
				ASetType settype = PTypeAssistantTC.getSet(setbind.getSet().apply(THIS, question));

				if (!TypeComparator.compatible(type, settype.getSetof()))
				{
					TypeCheckerErrors.report(3199, "Set bind not compatible with expression", bind.getLocation(), bind);
					TypeCheckerErrors.detail2("Bind", settype.getSetof(), "Exp", type);
				}
			}

			PDefinition def = AstFactory.newAMultiBindListDefinition(bind.getLocation(), PBindAssistantTC.getMultipleBindList(bind));

			def.apply(THIS, question);
			List<PDefinition> defs = new LinkedList<PDefinition>();
			defs.add(def);
			node.setDefs(defs);
		} else
		{
			assert (type != null) : "Can't typecheck a pattern without a type";

			PPatternAssistantTC.typeResolve(node.getPattern(), THIS, question);
			node.setDefs(PPatternAssistantTC.getDefinitions(node.getPattern(), type, NameScope.LOCAL));
		}
		return null;
	}

}
