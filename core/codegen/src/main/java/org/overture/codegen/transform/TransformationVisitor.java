package org.overture.codegen.transform;

import java.util.LinkedList;

import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.analysis.DepthFirstAnalysisAdaptor;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.expressions.ABoolLiteralExpCG;
import org.overture.codegen.cgast.expressions.ACompMapExpCG;
import org.overture.codegen.cgast.expressions.ACompSeqExpCG;
import org.overture.codegen.cgast.expressions.ACompSetExpCG;
import org.overture.codegen.cgast.expressions.AEnumMapExpCG;
import org.overture.codegen.cgast.expressions.AEnumSeqExpCG;
import org.overture.codegen.cgast.expressions.AEnumSetExpCG;
import org.overture.codegen.cgast.expressions.AEqualsBinaryExpCG;
import org.overture.codegen.cgast.expressions.AExists1QuantifierExpCG;
import org.overture.codegen.cgast.expressions.AExistsQuantifierExpCG;
import org.overture.codegen.cgast.expressions.AForAllQuantifierExpCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.expressions.ALetBeStExpCG;
import org.overture.codegen.cgast.expressions.AMapletExpCG;
import org.overture.codegen.cgast.expressions.ANullExpCG;
import org.overture.codegen.cgast.expressions.PExpCG;
import org.overture.codegen.cgast.pattern.AIdentifierPatternCG;
import org.overture.codegen.cgast.patterns.ASetMultipleBindCG;
import org.overture.codegen.cgast.statements.ABlockStmCG;
import org.overture.codegen.cgast.statements.ALetBeStStmCG;
import org.overture.codegen.cgast.statements.PStmCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.PTypeCG;
import org.overture.codegen.cgast.types.SSetTypeCG;
import org.overture.codegen.cgast.utils.AHeaderLetBeStCG;
import org.overture.codegen.constants.IOoAstConstants;
import org.overture.codegen.constants.TempVarPrefixes;
import org.overture.codegen.ooast.OoAstInfo;
import org.overture.codegen.transform.iterator.ILanguageIterator;
import org.overture.codegen.utils.ITempVarGen;

public class TransformationVisitor extends DepthFirstAnalysisAdaptor
{
	private OoAstInfo info;
	
	private TransformationAssistantCG transformationAssistant;
	
	private ILanguageIterator langIterator;
	
	public TransformationVisitor(OoAstInfo info, TempVarPrefixes varPrefixes, TransformationAssistantCG transformationAssistant, ILanguageIterator langIterator)
	{
		this.info = info;
		this.transformationAssistant = transformationAssistant;
		this.langIterator = langIterator;
	}
	
	@Override
	public void caseALetBeStStmCG(ALetBeStStmCG node) throws AnalysisException
	{
		AHeaderLetBeStCG header = node.getHeader();
		PExpCG suchThat = header.getSuchThat();
		SSetTypeCG setType = transformationAssistant.getSetTypeCloned(header.getBinding().getSet());
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		LetBeStStrategy strategy = new LetBeStStrategy(transformationAssistant, suchThat, setType, langIterator, tempVarNameGen, varPrefixes);
		
		ASetMultipleBindCG binding = header.getBinding();
		
		if (transformationAssistant.hasEmptySet(binding))
		{
			transformationAssistant.cleanUpBinding(binding);
			node.setStatement(new ABlockStmCG());
		}
		
		LinkedList<AIdentifierPatternCG> patterns = binding.getPatterns();
		ABlockStmCG outerBlock = transformationAssistant.consIterationBlock(patterns, binding.getSet(), tempVarNameGen, strategy);
		
		//Only the statement of the let be st statement is added to the outer block statements.
		//We obtain the equivalent functionality of the remaining part of the let be st statement
		//from the transformation in the outer block
		outerBlock.getStatements().add(node.getStatement());
		
		//Replace the let be st statement with the transformation
		transformationAssistant.replaceNodeWith(node, outerBlock);
		outerBlock.apply(this);
	}

	@Override
	public void caseALetBeStExpCG(ALetBeStExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "let be st expressions");

		AHeaderLetBeStCG header = node.getHeader();
		ASetMultipleBindCG binding = header.getBinding();
		PExpCG suchThat = header.getSuchThat();
		SSetTypeCG setType = transformationAssistant.getSetTypeCloned(binding.getSet());
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		LetBeStStrategy strategy = new LetBeStStrategy(transformationAssistant, suchThat, setType, langIterator, tempVarNameGen, varPrefixes);

		ABlockStmCG outerBlock = new ABlockStmCG();

		PExpCG letBeStResult = null;
		
		if (transformationAssistant.hasEmptySet(binding))
		{
			transformationAssistant.cleanUpBinding(binding);
			letBeStResult = new ANullExpCG();
		}
		else
		{
			String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_LET_BE_ST_EXP_NAME_PREFIX);
			PExpCG value = node.getValue();
			
			AVarLocalDeclCG resultDecl = transformationAssistant.consDecl(var, value);
			info.getStmAssistant().injectDeclAsStm(outerBlock, resultDecl);
			
			AIdentifierVarExpCG varExpResult = new AIdentifierVarExpCG();
			varExpResult.setType(value.getType().clone());
			varExpResult.setOriginal(var);
			letBeStResult = varExpResult;
		}
		
		//Replace the let be st expression with the result expression
		transformationAssistant.replaceNodeWith(node, letBeStResult);
		
		LinkedList<AIdentifierPatternCG> patterns = binding.getPatterns();
		ABlockStmCG block = transformationAssistant.consIterationBlock(patterns, binding.getSet(), tempVarNameGen, strategy);
		outerBlock.getStatements().addFirst(block);

		//Replace the enclosing statement with the transformation
		transformationAssistant.replaceNodeWith(enclosingStm, outerBlock);
		
		//And make sure to have the enclosing statement in the transformed tree
		outerBlock.getStatements().add(enclosingStm);
		outerBlock.apply(this);
	}

	@Override
	public void caseACompMapExpCG(ACompMapExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "map comprehension");
		
		AMapletExpCG first = node.getFirst();
		PExpCG predicate = node.getPredicate();
		PTypeCG type = node.getType();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_MAP_COMP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		ComplexCompStrategy strategy = new MapCompStrategy(transformationAssistant, first, predicate, var, type, langIterator, tempVarNameGen, varPrefixes);
		
		LinkedList<ASetMultipleBindCG> bindings = node.getBindings();
		ABlockStmCG block = transformationAssistant.consComplexCompIterationBlock(bindings, tempVarNameGen, strategy);
		
		if(block.getStatements().isEmpty())
		{
			//In case the block has no statements the result of the map comprehension is the empty map
			AEnumMapExpCG emptyMap = new AEnumMapExpCG();
			emptyMap.setType(type.clone());
			
			//Replace the map comprehension with the empty map
			transformationAssistant.replaceNodeWith(node, emptyMap);
		}
		else
		{
			replaceCompWithTransformation(enclosingStm, block, type, var, node);
		}
		
		block.apply(this);
	}
	
	@Override
	public void caseACompSetExpCG(ACompSetExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "set comprehension");
		
		PExpCG first = node.getFirst();
		PExpCG predicate = node.getPredicate();
		PTypeCG type = node.getType();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_SET_COMP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		ComplexCompStrategy strategy = new SetCompStrategy(transformationAssistant, first, predicate, var, type, langIterator, tempVarNameGen, varPrefixes);
		
		LinkedList<ASetMultipleBindCG> bindings = node.getBindings();
		ABlockStmCG block = transformationAssistant.consComplexCompIterationBlock(bindings, tempVarNameGen, strategy);

		if(block.getStatements().isEmpty())
		{
			//In case the block has no statements the result of the set comprehension is the empty set
			AEnumSetExpCG emptySet = new AEnumSetExpCG();
			emptySet.setType(type.clone());
			
			//Replace the set comprehension with the empty set
			transformationAssistant.replaceNodeWith(node, emptySet);
		}
		else
		{
			replaceCompWithTransformation(enclosingStm, block, type, var, node);
		}
		
		block.apply(this);
	}
	
	@Override
	public void caseACompSeqExpCG(ACompSeqExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "sequence comprehension");

		PExpCG first = node.getFirst();
		PExpCG predicate = node.getPredicate();
		PTypeCG type = node.getType();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();		
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_SEQ_COMP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		SeqCompStrategy strategy = new SeqCompStrategy(transformationAssistant, first, predicate, var, type, langIterator, tempVarNameGen, varPrefixes);

		if (transformationAssistant.isEmptySet(node.getSet()))
		{
			//In case the block has no statements the result of the sequence comprehension is the empty sequence
			AEnumSeqExpCG emptySeq = new AEnumSeqExpCG();
			emptySeq.setType(type.clone());
			
			//Replace the sequence comprehension with the empty sequence
			transformationAssistant.replaceNodeWith(node, emptySeq);
		}
		else
		{
			LinkedList<AIdentifierPatternCG> ids = new LinkedList<AIdentifierPatternCG>();
			ids.add(node.getId());

			ABlockStmCG block = transformationAssistant.consIterationBlock(ids, node.getSet(), info.getTempVarNameGen(), strategy);

			replaceCompWithTransformation(enclosingStm, block, type, var, node);
			
			block.apply(this);
		}
	}
	
	@Override
	public void caseAForAllQuantifierExpCG(AForAllQuantifierExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "forall expression");
		
		PExpCG predicate = node.getPredicate();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_FORALL_EXP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		OrdinaryQuantifierStrategy strategy = new OrdinaryQuantifierStrategy(transformationAssistant, predicate, var, OrdinaryQuantifier.FORALL, langIterator, tempVarNameGen, varPrefixes);
		
		ABlockStmCG block = transformationAssistant.consComplexCompIterationBlock(node.getBindList(), tempVarNameGen, strategy);

		if(node.getBindList().isEmpty())
		{
			ABoolLiteralExpCG forAllResult = info.getExpAssistant().consBoolLiteral(true);
			transformationAssistant.replaceNodeWith(node, forAllResult);
		}
		else
		{
			AIdentifierVarExpCG forAllResult = new AIdentifierVarExpCG();
			forAllResult.setType(new ABoolBasicTypeCG());
			forAllResult.setOriginal(var);
			
			transform(enclosingStm, block, forAllResult, node);
			block.apply(this);
		}
	}
	
	@Override
	public void caseAExistsQuantifierExpCG(
			AExistsQuantifierExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "exists expression");
		
		PExpCG predicate = node.getPredicate();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_EXISTS_EXP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		OrdinaryQuantifierStrategy strategy = new OrdinaryQuantifierStrategy(transformationAssistant, predicate, var, OrdinaryQuantifier.EXISTS, langIterator, tempVarNameGen, varPrefixes);
		
		ABlockStmCG block = transformationAssistant.consComplexCompIterationBlock(node.getBindList(), tempVarNameGen, strategy);

		if(node.getBindList().isEmpty())
		{
			ABoolLiteralExpCG existsResult = info.getExpAssistant().consBoolLiteral(false);
			transformationAssistant.replaceNodeWith(node, existsResult);
		}
		else
		{
			AIdentifierVarExpCG existsResult = new AIdentifierVarExpCG();
			existsResult.setType(new ABoolBasicTypeCG());
			existsResult.setOriginal(var);
			
			transform(enclosingStm, block, existsResult, node);
			block.apply(this);
		}
	}
	
	@Override
	public void caseAExists1QuantifierExpCG(
			AExists1QuantifierExpCG node) throws AnalysisException
	{
		PStmCG enclosingStm = getEnclosingStm(node, "exists1 expression");
		
		PExpCG predicate = node.getPredicate();
		ITempVarGen tempVarNameGen = info.getTempVarNameGen();
		String var = tempVarNameGen.nextVarName(IOoAstConstants.GENERATED_TEMP_EXISTS1_EXP_NAME_PREFIX);
		TempVarPrefixes varPrefixes = transformationAssistant.getVarPrefixes();
		
		Exists1QuantifierStrategy strategy = new Exists1QuantifierStrategy(transformationAssistant, predicate, var, langIterator, tempVarNameGen, varPrefixes);
		
		ABlockStmCG block = transformationAssistant.consComplexCompIterationBlock(node.getBindList(), tempVarNameGen, strategy);

		if(node.getBindList().isEmpty())
		{
			ABoolLiteralExpCG exists1Result = info.getExpAssistant().consBoolLiteral(false);
			transformationAssistant.replaceNodeWith(node, exists1Result);
		}
		else
		{
			AIdentifierVarExpCG counter = new AIdentifierVarExpCG();
			counter.setType(new AIntNumericBasicTypeCG());
			counter.setOriginal(var);
			
			AEqualsBinaryExpCG exists1Result = new AEqualsBinaryExpCG();
			exists1Result.setType(new ABoolBasicTypeCG());
			exists1Result.setLeft(counter);
			exists1Result.setRight(info.getExpAssistant().consIntLiteral(1));
			
			transform(enclosingStm, block, exists1Result, node);
			block.apply(this);
		}
	}

	private void replaceCompWithTransformation(PStmCG enclosingStm, ABlockStmCG block,
			PTypeCG type, String var, PExpCG comp)
	{
		AIdentifierVarExpCG compResult = new AIdentifierVarExpCG();
		compResult.setType(type.clone());
		compResult.setOriginal(var);
		
		transform(enclosingStm, block, compResult, comp);
	}

	private void transform(PStmCG enclosingStm, ABlockStmCG block,
			PExpCG nodeResult, PExpCG node)
	{
		//Replace the node with the node result
		transformationAssistant.replaceNodeWith(node, nodeResult);
		
		//Replace the enclosing statement with the transformation
		transformationAssistant.replaceNodeWith(enclosingStm, block);
		
		//And make sure to have the enclosing statement in the transformed tree
		block.getStatements().add(enclosingStm);
	}
	
	private PStmCG getEnclosingStm(PExpCG node, String nodeStr) throws AnalysisException
	{
		PStmCG enclosingStm = node.getAncestor(PStmCG.class);

		//This case should never occur as it must be checked for during the construction of the OO AST
		if (enclosingStm == null)
			throw new AnalysisException(String.format("Generation of a %s is only supported within operations/functions", node));
			
		return enclosingStm;
	}
}