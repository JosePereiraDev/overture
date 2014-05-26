package org.overture.codegen.transform;

import java.util.List;

import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.declarations.SLocalDeclCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.expressions.PExpCG;
import org.overture.codegen.cgast.pattern.AIdentifierPatternCG;
import org.overture.codegen.cgast.statements.PStmCG;
import org.overture.codegen.constants.TempVarPrefixes;
import org.overture.codegen.transform.iterator.ILanguageIterator;
import org.overture.codegen.utils.ITempVarGen;

public class OrdinaryQuantifierStrategy extends QuantifierBaseStrategy
{
	protected OrdinaryQuantifier quantifier;

	public OrdinaryQuantifierStrategy(
			TransformationAssistantCG transformationAssistant,
			PExpCG predicate, String resultVarName,
			OrdinaryQuantifier quantifier, ILanguageIterator langIterator,
			ITempVarGen tempGen, TempVarPrefixes varPrefixes)
	{
		super(transformationAssistant, predicate, resultVarName, langIterator, tempGen, varPrefixes);
		this.quantifier = quantifier;
	}

	@Override
	public List<? extends SLocalDeclCG> getOuterBlockDecls(
			AIdentifierVarExpCG setVar, List<AIdentifierPatternCG> ids)
			throws AnalysisException
	{
		return firstBind ? packDecl(transformationAssistant.consBoolVarDecl(resultVarName, quantifier == OrdinaryQuantifier.FORALL))
				: null;
	}

	@Override
	public PExpCG getForLoopCond(AIdentifierVarExpCG setVar,
			List<AIdentifierPatternCG> ids, AIdentifierPatternCG id)
			throws AnalysisException
	{
		PExpCG left = langIterator.getForLoopCond(setVar, ids, id);
		PExpCG right = transformationAssistant.consBoolCheck(resultVarName, quantifier == OrdinaryQuantifier.EXISTS);

		return transformationAssistant.consAndExp(left, right);
	}

	@Override
	public List<PStmCG> getForLoopStms(AIdentifierVarExpCG setVar,
			List<AIdentifierPatternCG> ids, AIdentifierPatternCG id)
	{
		return lastBind ? packStm(transformationAssistant.consBoolVarAssignment(predicate, resultVarName))
				: null;
	}
}
