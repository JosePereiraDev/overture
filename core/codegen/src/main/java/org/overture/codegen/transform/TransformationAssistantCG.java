package org.overture.codegen.transform;

import java.util.List;

import org.overture.codegen.cgast.INode;
import org.overture.codegen.cgast.analysis.AnalysisException;
import org.overture.codegen.cgast.declarations.AVarLocalDeclCG;
import org.overture.codegen.cgast.declarations.SLocalDeclCG;
import org.overture.codegen.cgast.expressions.AAndBoolBinaryExpCG;
import org.overture.codegen.cgast.expressions.AApplyExpCG;
import org.overture.codegen.cgast.expressions.ACastUnaryExpCG;
import org.overture.codegen.cgast.expressions.ACompSeqExpCG;
import org.overture.codegen.cgast.expressions.AFieldExpCG;
import org.overture.codegen.cgast.expressions.AIdentifierVarExpCG;
import org.overture.codegen.cgast.expressions.AIntLiteralExpCG;
import org.overture.codegen.cgast.expressions.ALessNumericBinaryExpCG;
import org.overture.codegen.cgast.expressions.ANotUnaryExpCG;
import org.overture.codegen.cgast.expressions.ANullExpCG;
import org.overture.codegen.cgast.expressions.PExpCG;
import org.overture.codegen.cgast.pattern.AIdentifierPatternCG;
import org.overture.codegen.cgast.patterns.ASetMultipleBindCG;
import org.overture.codegen.cgast.statements.AAssignmentStmCG;
import org.overture.codegen.cgast.statements.ABlockStmCG;
import org.overture.codegen.cgast.statements.AForLoopStmCG;
import org.overture.codegen.cgast.statements.AIdentifierStateDesignatorCG;
import org.overture.codegen.cgast.statements.AIfStmCG;
import org.overture.codegen.cgast.statements.AIncrementStmCG;
import org.overture.codegen.cgast.statements.PStmCG;
import org.overture.codegen.cgast.types.ABoolBasicTypeCG;
import org.overture.codegen.cgast.types.AClassTypeCG;
import org.overture.codegen.cgast.types.AIntNumericBasicTypeCG;
import org.overture.codegen.cgast.types.AMethodTypeCG;
import org.overture.codegen.cgast.types.PTypeCG;
import org.overture.codegen.cgast.types.SMapTypeCG;
import org.overture.codegen.cgast.types.SSeqTypeCG;
import org.overture.codegen.cgast.types.SSetTypeCG;
import org.overture.codegen.constants.TempVarPrefixes;
import org.overture.codegen.ooast.OoAstInfo;
import org.overture.codegen.utils.ITempVarGen;

public class TransformationAssistantCG
{
	protected OoAstInfo info;
	protected TempVarPrefixes varPrefixes;

	public TransformationAssistantCG(OoAstInfo info, TempVarPrefixes varPrefixes)
	{
		this.info = info;
		this.varPrefixes = varPrefixes;
	}

	public OoAstInfo getInfo()
	{
		return info;
	}

	public TempVarPrefixes getVarPrefixes()
	{
		return varPrefixes;
	}

	public void replaceNodeWith(INode original, INode replacement)
	{
		INode parent = original.parent();
		parent.replaceChild(original, replacement);
		original.parent(null);
	}

	public SSetTypeCG getSetTypeCloned(PExpCG set) throws AnalysisException
	{
		PTypeCG typeCg = set.getType();

		return getSetTypeCloned(typeCg);
	}

	public SSetTypeCG getSetTypeCloned(PTypeCG typeCg) throws AnalysisException
	{
		if (!(typeCg instanceof SSetTypeCG))
			throw new AnalysisException("Exptected set type. Got: " + typeCg);

		SSetTypeCG setTypeCg = (SSetTypeCG) typeCg;

		return setTypeCg.clone();
	}

	public SSeqTypeCG getSeqTypeCloned(PExpCG seq) throws AnalysisException
	{
		PTypeCG typeCg = seq.getType();

		return getSeqTypeCloned(typeCg);
	}

	public SSeqTypeCG getSeqTypeCloned(PTypeCG typeCg) throws AnalysisException
	{
		if (!(typeCg instanceof SSeqTypeCG))
			throw new AnalysisException("Exptected sequence type. Got: "
					+ typeCg);

		SSeqTypeCG seqTypeCg = (SSeqTypeCG) typeCg;

		return seqTypeCg.clone();
	}

	public SMapTypeCG getMapTypeCloned(PExpCG map) throws AnalysisException
	{
		PTypeCG typeCg = map.getType();

		return getMapTypeCloned(typeCg);
	}

	public SMapTypeCG getMapTypeCloned(PTypeCG typeCg) throws AnalysisException
	{
		if (!(typeCg instanceof SMapTypeCG))
			throw new AnalysisException("Exptected map type. Got: " + typeCg);

		SMapTypeCG mapTypeCg = (SMapTypeCG) typeCg;

		return mapTypeCg.clone();
	}

	public AVarLocalDeclCG consBoolVarDecl(String boolVarName, boolean initValue)
	{
		AVarLocalDeclCG boolVarDecl = new AVarLocalDeclCG();

		boolVarDecl.setType(new ABoolBasicTypeCG());
		boolVarDecl.setName(boolVarName);
		boolVarDecl.setExp(info.getExpAssistant().consBoolLiteral(initValue));

		return boolVarDecl;
	}

	public PExpCG consAndExp(PExpCG left, PExpCG right)
	{
		AAndBoolBinaryExpCG andExp = new AAndBoolBinaryExpCG();
		andExp.setType(new ABoolBasicTypeCG());
		andExp.setLeft(left);
		andExp.setRight(right);

		return andExp;
	}

	public PExpCG consLessThanCheck(String varName, long value)
	{
		AIdentifierVarExpCG left = new AIdentifierVarExpCG();
		left.setType(new AIntNumericBasicTypeCG());
		left.setOriginal(varName);

		AIntLiteralExpCG right = info.getExpAssistant().consIntLiteral(value);

		ALessNumericBinaryExpCG less = new ALessNumericBinaryExpCG();
		less.setType(new ABoolBasicTypeCG());
		less.setLeft(left);
		less.setRight(right);

		return less;
	}

	protected PExpCG consBoolCheck(String boolVarName, boolean negate)
	{
		AIdentifierVarExpCG boolVarExp = new AIdentifierVarExpCG();
		boolVarExp.setType(new ABoolBasicTypeCG());
		boolVarExp.setOriginal(boolVarName);

		if (negate)
		{
			ANotUnaryExpCG negated = new ANotUnaryExpCG();
			negated.setType(new ABoolBasicTypeCG());
			negated.setExp(boolVarExp);

			return negated;
		} else
		{
			return boolVarExp;
		}
	}

	public AAssignmentStmCG consBoolVarAssignment(PExpCG predicate,
			String boolVarName)
	{
		AAssignmentStmCG boolVarAssignment = new AAssignmentStmCG();

		boolVarAssignment.setTarget(consIdentifier(boolVarName));
		boolVarAssignment.setExp(predicate != null ? predicate.clone()
				: info.getExpAssistant().consBoolLiteral(true));

		return boolVarAssignment;
	}

	public AVarLocalDeclCG consSetBindDecl(String setBindName, PExpCG set)
			throws AnalysisException
	{
		AVarLocalDeclCG setBindDecl = new AVarLocalDeclCG();

		setBindDecl.setType(getSetTypeCloned(set));
		setBindDecl.setName(setBindName);
		setBindDecl.setExp(set.clone());

		return setBindDecl;
	}

	public AVarLocalDeclCG consIdDecl(PTypeCG setType, String id)
			throws AnalysisException
	{
		AVarLocalDeclCG idDecl = new AVarLocalDeclCG();

		idDecl.setType(getSetTypeCloned(setType).getSetOf());
		idDecl.setName(id);
		idDecl.setExp(new ANullExpCG());

		return idDecl;
	}

	public AVarLocalDeclCG consDecl(String varName, PExpCG exp)
	{
		AVarLocalDeclCG resultDecl = new AVarLocalDeclCG();

		resultDecl.setType(exp.getType().clone());
		resultDecl.setName(varName);
		resultDecl.setExp(exp);

		return resultDecl;
	}

	public AIdentifierStateDesignatorCG consIdentifier(String name)
	{
		AIdentifierStateDesignatorCG identifier = new AIdentifierStateDesignatorCG();
		identifier.setName(name);

		return identifier;
	}

	public AClassTypeCG consClassType(String classTypeName)
	{
		AClassTypeCG iteratorType = new AClassTypeCG();
		iteratorType.setName(classTypeName);

		return iteratorType;
	}

	public PExpCG consInstanceCall(PTypeCG instanceType, String instanceName,
			PTypeCG returnType, String memberName, PExpCG arg)
	{
		AIdentifierVarExpCG instance = new AIdentifierVarExpCG();
		instance.setOriginal(instanceName);
		instance.setType(instanceType.clone());

		AFieldExpCG fieldExp = new AFieldExpCG();
		fieldExp.setMemberName(memberName);
		fieldExp.setObject(instance);
		
		AMethodTypeCG methodType = new AMethodTypeCG();
		methodType.setResult(returnType.clone());

		AApplyExpCG instanceCall = new AApplyExpCG();
		
		instanceCall.setType(returnType.clone());

		if (arg != null)
		{
			methodType.getParams().add(arg.getType().clone());
			instanceCall.getArgs().add(arg);
		}
		
		fieldExp.setType(methodType.clone());
		
		instanceCall.setRoot(fieldExp);

		return instanceCall;
	}

	public AVarLocalDeclCG consNextElementDeclared(String iteratorTypeName,
			PTypeCG elementType, String id, String iteratorName,
			String nextElementMethod) throws AnalysisException
	{
		ACastUnaryExpCG cast = consNextElementCall(iteratorTypeName, iteratorName, elementType, nextElementMethod);
		AVarLocalDeclCG decl = new AVarLocalDeclCG();

		decl.setType(elementType);
		decl.setName(id);
		decl.setExp(cast);
		;

		return decl;
	}

	public AAssignmentStmCG consNextElementAssignment(String iteratorTypeName,
			PTypeCG elementType, String id, String iteratorName,
			String nextElementMethod) throws AnalysisException
	{
		ACastUnaryExpCG cast = consNextElementCall(iteratorTypeName, iteratorName, elementType, nextElementMethod);

		AAssignmentStmCG assignment = new AAssignmentStmCG();
		assignment.setTarget(consIdentifier(id));
		assignment.setExp(cast);

		return assignment;
	}

	public ACastUnaryExpCG consNextElementCall(String iteratorType,
			String iteratorName, PTypeCG elementType, String nextElementMethod)
	{
		ACastUnaryExpCG cast = new ACastUnaryExpCG();
		cast.setType(elementType.clone());
		cast.setExp(consInstanceCall(consClassType(iteratorType), iteratorName, elementType.clone(), nextElementMethod, null));
		return cast;
	}

	public PStmCG consConditionalIncrement(String counterName, PExpCG predicate)
	{
		AIdentifierVarExpCG col = new AIdentifierVarExpCG();
		col.setType(new AIntNumericBasicTypeCG());
		col.setOriginal(counterName);

		AIncrementStmCG inc = new AIncrementStmCG();
		inc.setVar(col);

		AIfStmCG ifStm = new AIfStmCG();
		ifStm.setIfExp(predicate);
		ifStm.setThenStm(inc);

		return ifStm;
	}

	public ABlockStmCG consIterationBlock(List<AIdentifierPatternCG> ids,
			PExpCG set, ITempVarGen tempGen, IIterationStrategy strategy)
			throws AnalysisException
	{
		ABlockStmCG outerBlock = new ABlockStmCG();

		consIterationBlock(outerBlock, ids, set, tempGen, strategy);

		return outerBlock;
	}

	public AIdentifierVarExpCG consSetVar(String setName, PExpCG set)
	{
		if (set == null)
			return null;

		AIdentifierVarExpCG setVar = new AIdentifierVarExpCG();

		PTypeCG setType = set.getType().clone();

		setVar.setOriginal(setName);
		setVar.setType(setType);

		return setVar;
	}

	protected ABlockStmCG consIterationBlock(ABlockStmCG outerBlock,
			List<AIdentifierPatternCG> ids, PExpCG set, ITempVarGen tempGen,
			IIterationStrategy strategy) throws AnalysisException
	{
		// Variable names
		String setName = tempGen.nextVarName(varPrefixes.getSetNamePrefix());
		AIdentifierVarExpCG setVar = consSetVar(setName, set);

		ABlockStmCG forBody = null;
		List<? extends SLocalDeclCG> extraDecls = strategy.getOuterBlockDecls(setVar, ids);

		if (extraDecls != null)
		{
			outerBlock.getLocalDefs().addAll(extraDecls);
		}

		if (setVar != null)
		{
			outerBlock.getLocalDefs().add(consSetBindDecl(setName, set));

			ABlockStmCG nextBlock = outerBlock;

			for (int i = 0;;)
			{
				AIdentifierPatternCG id = ids.get(i);

				// Construct next for loop
				AForLoopStmCG forLoop = new AForLoopStmCG();

				forLoop.setInit(strategy.getForLoopInit(setVar, ids, id));
				forLoop.setCond(strategy.getForLoopCond(setVar, ids, id));
				forLoop.setInc(strategy.getForLoopInc(setVar, ids, id));

				ABlockStmCG stmCollector = new ABlockStmCG();

				AVarLocalDeclCG nextElementDeclared = strategy.getNextElementDeclared(setVar, ids, id);

				if (nextElementDeclared != null)
					stmCollector.getLocalDefs().add(nextElementDeclared);

				AAssignmentStmCG assignment = strategy.getNextElementAssigned(setVar, ids, id);

				if (assignment != null)
					stmCollector.getStatements().add(assignment);

				forBody = stmCollector;

				forLoop.setBody(forBody);

				nextBlock.getStatements().add(forLoop);

				if (++i < ids.size())
				{
					nextBlock = forBody;
				} else
				{
					List<PStmCG> extraForLoopStatements = strategy.getForLoopStms(setVar, ids, id);

					if (extraForLoopStatements != null)
					{
						forBody.getStatements().addAll(extraForLoopStatements);
					}

					break;
				}
			}
		}

		List<PStmCG> extraOuterBlockStms = strategy.getOuterBlockStms(setVar, ids);

		if (extraOuterBlockStms != null)
		{
			outerBlock.getStatements().addAll(extraOuterBlockStms);
		}

		return forBody;
	}

	public ABlockStmCG consComplexCompIterationBlock(
			List<ASetMultipleBindCG> multipleSetBinds, ITempVarGen tempGen,
			IIterationStrategy strategy) throws AnalysisException
	{
		ABlockStmCG outerBlock = new ABlockStmCG();

		ABlockStmCG nextMultiBindBlock = outerBlock;

		for (ASetMultipleBindCG bind : multipleSetBinds)
		{
			SSetTypeCG setType = getSetTypeCloned(bind.getSet());

			if (setType.getEmpty())
			{
				multipleSetBinds.clear();
				return outerBlock;
			}
		}

		strategy.setFirstBind(true);

		for (int i = 0; i < multipleSetBinds.size(); i++)
		{
			strategy.setLastBind(i == multipleSetBinds.size() - 1);

			ASetMultipleBindCG mb = multipleSetBinds.get(i);
			nextMultiBindBlock = consIterationBlock(nextMultiBindBlock, mb.getPatterns(), mb.getSet(), tempGen, strategy);

			strategy.setFirstBind(false);
		}

		return outerBlock;
	}

	public ACastUnaryExpCG consNextElementCall(String iteratorTypeName,
			String instance, String member, ACompSeqExpCG seqComp)
			throws AnalysisException
	{

		PTypeCG elementType = getSeqTypeCloned(seqComp).getSeqOf();

		PExpCG nextCall = consInstanceCall(consClassType(iteratorTypeName), instance, elementType.clone(), member, null);
		ACastUnaryExpCG cast = new ACastUnaryExpCG();
		cast.setType(elementType.clone());
		cast.setExp(nextCall);

		return cast;
	}

	public Boolean hasEmptySet(ASetMultipleBindCG binding)
			throws AnalysisException
	{
		return isEmptySet(binding.getSet());
	}

	public Boolean isEmptySet(PExpCG set) throws AnalysisException
	{
		return getSetTypeCloned(set).getEmpty();
	}

	public void cleanUpBinding(ASetMultipleBindCG binding)
	{
		binding.setSet(null);
		binding.getPatterns().clear();
	}
}
