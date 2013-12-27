package org.overture.typechecker.visitor;

import org.overture.ast.analysis.AnalysisException;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.definitions.traces.PTraceCoreDefinition;
import org.overture.ast.definitions.traces.PTraceDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.modules.AModuleModules;
import org.overture.ast.modules.PImport;
import org.overture.ast.node.INode;
import org.overture.ast.patterns.PMultipleBind;
import org.overture.ast.patterns.PPatternBind;
import org.overture.ast.statements.PAlternativeStm;
import org.overture.ast.statements.PObjectDesignator;
import org.overture.ast.statements.PStateDesignator;
import org.overture.ast.statements.PStm;
import org.overture.ast.statements.PStmtAlternative;
import org.overture.ast.types.PType;
import org.overture.typechecker.TypeCheckInfo;
import org.overture.typechecker.TypeCheckerErrors;

public class TypeCheckVisitor extends AbstractTypeCheckVisitor
{

	private AbstractTypeCheckVisitor tcStm = new TypeCheckerStmVisitor(this);
	private AbstractTypeCheckVisitor tcExp = new TypeCheckerExpVisitor(this);
	private AbstractTypeCheckVisitor tcDefinition = new TypeCheckerDefinitionVisitor(this);
	private AbstractTypeCheckVisitor patternDefinition = new TypeCheckerPatternVisitor(this);
	private AbstractTypeCheckVisitor tcImports = new TypeCheckerImportsVisitor(this);
	private AbstractTypeCheckVisitor tcOthers = new TypeCheckerOthersVisitor(this);

	public TypeCheckerErrors tcErrors = new TypeCheckerErrors();

	@Override
	public PType defaultPPatternBind(PPatternBind node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(tcOthers, question);
	}

	@Override
	public PType defaultPStateDesignator(PStateDesignator node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcOthers, question);
	}

	@Override
	public PType defaultPObjectDesignator(PObjectDesignator node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcOthers, question);
	}

	@Override
	public PType defaultPImport(PImport node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(tcImports, question);
	}

	@Override
	public PType defaultPStm(PStm node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(tcStm, question);
	}

	@Override
	public PType defaultPAlternativeStm(PAlternativeStm node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcStm, question);
	}

	@Override
	public PType defaultPExp(PExp node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(tcExp, question);
	}

	@Override
	public PType defaultPDefinition(PDefinition node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(tcDefinition, question);
	}

	@Override
	public PType caseAModuleModules(AModuleModules node, TypeCheckInfo question)
			throws AnalysisException
	{
		for (PDefinition def : node.getDefs())
		{
			def.apply(this, question);
		}

		return null;
	}

	@Override
	public PType defaultPMultipleBind(PMultipleBind node, TypeCheckInfo question)
			throws AnalysisException
	{
		return node.apply(patternDefinition, question);
	}

	@Override
	public PType defaultPStmtAlternative(PStmtAlternative node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcStm, question);
	}

	public PType defaultPTraceDefinition(PTraceDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcDefinition, question);
	}

	public PType defaultPTraceCoreDefinition(PTraceCoreDefinition node,
			TypeCheckInfo question) throws AnalysisException
	{
		return node.apply(tcDefinition, question);
	}

	@Override
	public PType defaultINode(INode node, TypeCheckInfo question)
			throws AnalysisException
	{
		return null;// we dont want an infinit loop
	}
}
