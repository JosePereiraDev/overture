package org.overture.interpreter.assistant;

import java.io.Serializable;

import org.overture.ast.analysis.intf.IAnswer;
import org.overture.ast.lex.LexNameList;
import org.overture.interpreter.assistant.definition.AApplyExpressionTraceCoreDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AAssignmentDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ABracketedExpressionTraceCoreDefinitionAssitantInterpreter;
import org.overture.interpreter.assistant.definition.ABusClassDefinitionAssitantInterpreter;
import org.overture.interpreter.assistant.definition.AClassClassDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AConcurrentExpressionTraceCoreDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ACpuClassDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AEqualsDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AErrorCaseAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AExplicitFunctionDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AExplicitOperationDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AImplicitFunctionDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AImplicitOperationDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AImportedDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AInheritedDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AInstanceVariableDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ALetBeStBindingTraceDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ALetDefBindingTraceDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ALocalDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AMutexSyncDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ANamedTraceDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.APerSyncDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ARenamedDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ARepeatTraceDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AStateDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ASystemClassDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AThreadDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ATraceDefinitionTermAssistantInterpreter;
import org.overture.interpreter.assistant.definition.ATypeDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AUntypedDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.AValueDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.PDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.PDefinitionListAssistantInterpreter;
import org.overture.interpreter.assistant.definition.PTraceCoreDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.PTraceDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.definition.SClassDefinitionAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AApplyExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ACaseAlternativeAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ACasesExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ADefExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AElseIfExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AExists1ExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AExistsExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AFieldExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AFieldNumberExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AForAllExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AFuncInstatiationExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AIfExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AIotaExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AIsExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AIsOfBaseClassExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AIsOfClassExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ALambdaExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ALetBeStExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ALetDefExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMapCompMapExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMapEnumMapExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMapletExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMkBasicExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMkTypeExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AMuExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ANarrowExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ANewExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.APostOpExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ARecordModifierAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASameBaseClassExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASameClassExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASeqCompSeqExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASeqEnumSeqExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASetCompSetExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASetEnumSetExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASetRangeSetExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ASubseqExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.ATupleExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.AVariableExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.PExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.SBinaryExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.SMapExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.SSeqExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.SSetExpAssistantInterpreter;
import org.overture.interpreter.assistant.expression.SUnaryExpAssistantInterpreter;
import org.overture.interpreter.assistant.module.AModuleModulesAssistantInterpreter;
import org.overture.interpreter.assistant.module.ModuleListAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ABooleanPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ACharacterPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AConcatenationPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AExpressionPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AIdentifierPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AIgnorePatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AIntegerPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AMapPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AMapPatternMapletAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AMapUnionPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ANilPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AQuotePatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ARealPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ARecordPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ASeqPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ASetBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ASetMultipleBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ASetPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AStringPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ATuplePatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ATypeBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.ATypeMultipleBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.AUnionPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PMultipleBindAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PPatternAssistantInterpreter;
import org.overture.interpreter.assistant.pattern.PPatternListAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AAlwaysStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AAssignmentStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AAtomicStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACallObjectStatementAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACallStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACaseAlternativeStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACasesStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ACyclesStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ADurationStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AElseIfStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AExitStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AForAllStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AForIndexStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AForPatternBindStmAssitantInterpreter;
import org.overture.interpreter.assistant.statement.AIfStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ALetBeStStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AReturnStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AStartStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ATixeStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ATixeStmtAlternativeAssistantInterpreter;
import org.overture.interpreter.assistant.statement.ATrapStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.AWhileStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.PStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.SLetDefStmAssistantInterpreter;
import org.overture.interpreter.assistant.statement.SSimpleBlockStmAssistantInterpreter;
import org.overture.interpreter.assistant.type.ABooleanBasicTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AInMapMapTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.ANamedInvariantTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AOptionalTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AParameterTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AProductTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AQuoteTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.ARecordInvariantTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.ASetTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.AUnionTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.PTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.PTypeListAssistant;
import org.overture.interpreter.assistant.type.SBasicTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.SInvariantTypeAssistantInterpreter;
import org.overture.interpreter.assistant.type.SMapTypeAssistantInterpreter;
import org.overture.interpreter.utilities.OldNameCollector;
import org.overture.typechecker.assistant.TypeCheckerAssistantFactory;

public class InterpreterAssistantFactory extends TypeCheckerAssistantFactory
		implements IInterpreterAssistantFactory, Serializable
{
	/**
	 * serial id
	 */
	private static final long serialVersionUID = 9172962921215037477L;

	static
	{
		// FIXME: remove this when conversion to factory obtained assistants are completed.
		// init(new AstAssistantFactory());
		init(new InterpreterAssistantFactory());
	}

	// definition

	public AApplyExpressionTraceCoreDefinitionAssistantInterpreter createAApplyExpressionTraceCoreDefinitionAssistant()
	{
		return new AApplyExpressionTraceCoreDefinitionAssistantInterpreter(this);
	}

	public AAssignmentDefinitionAssistantInterpreter createAAssignmentDefinitionAssistant()
	{
		return new AAssignmentDefinitionAssistantInterpreter(this);
	}

	public ABracketedExpressionTraceCoreDefinitionAssitantInterpreter createABracketedExpressionTraceCoreDefinitionAssitant()
	{
		return new ABracketedExpressionTraceCoreDefinitionAssitantInterpreter(this);
	}

	public ABusClassDefinitionAssitantInterpreter createABusClassDefinitionAssitant()
	{
		return new ABusClassDefinitionAssitantInterpreter(this);
	}

	public AClassClassDefinitionAssistantInterpreter createAClassClassDefinitionAssistant()
	{
		return new AClassClassDefinitionAssistantInterpreter(this);
	}

	public AConcurrentExpressionTraceCoreDefinitionAssistantInterpreter createAConcurrentExpressionTraceCoreDefinitionAssistant()
	{
		return new AConcurrentExpressionTraceCoreDefinitionAssistantInterpreter(this);
	}

	public ACpuClassDefinitionAssistantInterpreter createACpuClassDefinitionAssistant()
	{
		return new ACpuClassDefinitionAssistantInterpreter(this);
	}

	public AEqualsDefinitionAssistantInterpreter createAEqualsDefinitionAssistant()
	{
		return new AEqualsDefinitionAssistantInterpreter(this);
	}

	public AErrorCaseAssistantInterpreter createAErrorCaseAssistant()
	{
		return new AErrorCaseAssistantInterpreter(this);
	}

	public AExplicitFunctionDefinitionAssistantInterpreter createAExplicitFunctionDefinitionAssistant()
	{
		return new AExplicitFunctionDefinitionAssistantInterpreter(this);
	}

	public AExplicitOperationDefinitionAssistantInterpreter createAExplicitOperationDefinitionAssistant()
	{
		return new AExplicitOperationDefinitionAssistantInterpreter(this);
	}

	public AImplicitFunctionDefinitionAssistantInterpreter createAImplicitFunctionDefinitionAssistant()
	{
		return new AImplicitFunctionDefinitionAssistantInterpreter(this);
	}

	public AImplicitOperationDefinitionAssistantInterpreter createAImplicitOperationDefinitionAssistant()
	{
		return new AImplicitOperationDefinitionAssistantInterpreter(this);
	}

	public AImportedDefinitionAssistantInterpreter createAImportedDefinitionAssistant()
	{
		return new AImportedDefinitionAssistantInterpreter(this);
	}

	public AInheritedDefinitionAssistantInterpreter createAInheritedDefinitionAssistant()
	{
		return new AInheritedDefinitionAssistantInterpreter(this);
	}

	public AInstanceVariableDefinitionAssistantInterpreter createAInstanceVariableDefinitionAssistant()
	{
		return new AInstanceVariableDefinitionAssistantInterpreter(this);
	}

	public ALetBeStBindingTraceDefinitionAssistantInterpreter createALetBeStBindingTraceDefinitionAssistant()
	{
		return new ALetBeStBindingTraceDefinitionAssistantInterpreter(this);
	}

	public ALetDefBindingTraceDefinitionAssistantInterpreter createALetDefBindingTraceDefinitionAssistant()
	{
		return new ALetDefBindingTraceDefinitionAssistantInterpreter(this);
	}

	public ALocalDefinitionAssistantInterpreter createALocalDefinitionAssistant()
	{
		return new ALocalDefinitionAssistantInterpreter(this);
	}

	public AMutexSyncDefinitionAssistantInterpreter createAMutexSyncDefinitionAssistant()
	{
		return new AMutexSyncDefinitionAssistantInterpreter(this);
	}

	public ANamedTraceDefinitionAssistantInterpreter createANamedTraceDefinitionAssistant()
	{
		return new ANamedTraceDefinitionAssistantInterpreter(this);
	}

	public APerSyncDefinitionAssistantInterpreter createAPerSyncDefinitionAssistant()
	{
		return new APerSyncDefinitionAssistantInterpreter(this);
	}

	public ARenamedDefinitionAssistantInterpreter createARenamedDefinitionAssistant()
	{
		return new ARenamedDefinitionAssistantInterpreter(this);
	}

	public ARepeatTraceDefinitionAssistantInterpreter createARepeatTraceDefinitionAssistant()
	{
		return new ARepeatTraceDefinitionAssistantInterpreter(this);
	}

	public AStateDefinitionAssistantInterpreter createAStateDefinitionAssistant()
	{
		return new AStateDefinitionAssistantInterpreter(this);
	}

	public ASystemClassDefinitionAssistantInterpreter createASystemClassDefinitionAssistant()
	{
		return new ASystemClassDefinitionAssistantInterpreter(this);
	}

	public AThreadDefinitionAssistantInterpreter createAThreadDefinitionAssistant()
	{
		return new AThreadDefinitionAssistantInterpreter(this);
	}

	public ATraceDefinitionTermAssistantInterpreter createATraceDefinitionTermAssistant()
	{
		return new ATraceDefinitionTermAssistantInterpreter(this);
	}

	public ATypeDefinitionAssistantInterpreter createATypeDefinitionAssistant()
	{
		return new ATypeDefinitionAssistantInterpreter(this);
	}

	public AUntypedDefinitionAssistantInterpreter createAUntypedDefinitionAssistant()
	{
		return new AUntypedDefinitionAssistantInterpreter(this);
	}

	public AValueDefinitionAssistantInterpreter createAValueDefinitionAssistant()
	{
		return new AValueDefinitionAssistantInterpreter(this);
	}

	public PDefinitionAssistantInterpreter createPDefinitionAssistant()
	{
		return new PDefinitionAssistantInterpreter(this);
	}

	public PDefinitionListAssistantInterpreter createPDefinitionListAssistant()
	{
		return new PDefinitionListAssistantInterpreter(this);
	}

	public PTraceCoreDefinitionAssistantInterpreter createPTraceCoreDefinitionAssistant()
	{
		return new PTraceCoreDefinitionAssistantInterpreter(this);
	}

	public PTraceDefinitionAssistantInterpreter createPTraceDefinitionAssistant()
	{
		return new PTraceDefinitionAssistantInterpreter(this);
	}

	public SClassDefinitionAssistantInterpreter createSClassDefinitionAssistant()
	{
		return new SClassDefinitionAssistantInterpreter(this);
	}

	// expression

	public AApplyExpAssistantInterpreter createAApplyExpAssistant()
	{
		return new AApplyExpAssistantInterpreter(this);
	}

	public ACaseAlternativeAssistantInterpreter createACaseAlternativeAssistant()
	{
		return new ACaseAlternativeAssistantInterpreter(this);
	}

	public ACasesExpAssistantInterpreter createACasesExpAssistant()
	{
		return new ACasesExpAssistantInterpreter(this);
	}

	public ADefExpAssistantInterpreter createADefExpAssistant()
	{
		return new ADefExpAssistantInterpreter(this);
	}

	public AElseIfExpAssistantInterpreter createAElseIfExpAssistant()
	{
		return new AElseIfExpAssistantInterpreter(this);
	}

	public AExists1ExpAssistantInterpreter createAExists1ExpAssistant()
	{
		return new AExists1ExpAssistantInterpreter(this);
	}

	public AExistsExpAssistantInterpreter createAExistsExpAssistant()
	{
		return new AExistsExpAssistantInterpreter(this);
	}

	public AFieldExpAssistantInterpreter createAFieldExpAssistant()
	{
		return new AFieldExpAssistantInterpreter(this);
	}

	public AFieldNumberExpAssistantInterpreter createAFieldNumberExpAssistant()
	{
		return new AFieldNumberExpAssistantInterpreter(this);
	}

	public AForAllExpAssistantInterpreter createAForAllExpAssistant()
	{
		return new AForAllExpAssistantInterpreter(this);
	}

	public AFuncInstatiationExpAssistantInterpreter createAFuncInstatiationExpAssistant()
	{
		return new AFuncInstatiationExpAssistantInterpreter(this);
	}

	public AIfExpAssistantInterpreter createAIfExpAssistant()
	{
		return new AIfExpAssistantInterpreter(this);
	}

	public AIotaExpAssistantInterpreter createAIotaExpAssistant()
	{
		return new AIotaExpAssistantInterpreter(this);
	}

	public AIsExpAssistantInterpreter createAIsExpAssistant()
	{
		return new AIsExpAssistantInterpreter(this);
	}

	public AIsOfBaseClassExpAssistantInterpreter createAIsOfBaseClassExpAssistant()
	{
		return new AIsOfBaseClassExpAssistantInterpreter(this);
	}

	public AIsOfClassExpAssistantInterpreter createAIsOfClassExpAssistant()
	{
		return new AIsOfClassExpAssistantInterpreter(this);
	}

	public ALambdaExpAssistantInterpreter createALambdaExpAssistant()
	{
		return new ALambdaExpAssistantInterpreter(this);
	}

	public ALetBeStExpAssistantInterpreter createALetBeStExpAssistant()
	{
		return new ALetBeStExpAssistantInterpreter(this);
	}

	public ALetDefExpAssistantInterpreter createALetDefExpAssistant()
	{
		return new ALetDefExpAssistantInterpreter(this);
	}

	public AMapCompMapExpAssistantInterpreter createAMapCompMapExpAssistant()
	{
		return new AMapCompMapExpAssistantInterpreter(this);
	}

	public AMapEnumMapExpAssistantInterpreter createAMapEnumMapExpAssistant()
	{
		return new AMapEnumMapExpAssistantInterpreter(this);
	}

	public AMapletExpAssistantInterpreter createAMapletExpAssistant()
	{
		return new AMapletExpAssistantInterpreter(this);
	}

	public AMkBasicExpAssistantInterpreter createAMkBasicExpAssistant()
	{
		return new AMkBasicExpAssistantInterpreter(this);
	}

	public AMkTypeExpAssistantInterpreter createAMkTypeExpAssistant()
	{
		return new AMkTypeExpAssistantInterpreter(this);
	}

	public AMuExpAssistantInterpreter createAMuExpAssistant()
	{
		return new AMuExpAssistantInterpreter(this);
	}

	public ANarrowExpAssistantInterpreter createANarrowExpAssistant()
	{
		return new ANarrowExpAssistantInterpreter(this);
	}

	public ANewExpAssistantInterpreter createANewExpAssistant()
	{
		return new ANewExpAssistantInterpreter(this);
	}

	public APostOpExpAssistantInterpreter createAPostOpExpAssistant()
	{
		return new APostOpExpAssistantInterpreter(this);
	}

	public ARecordModifierAssistantInterpreter createARecordModifierAssistant()
	{
		return new ARecordModifierAssistantInterpreter(this);
	}

	public ASameBaseClassExpAssistantInterpreter createASameBaseClassExpAssistant()
	{
		return new ASameBaseClassExpAssistantInterpreter(this);
	}

	public ASameClassExpAssistantInterpreter createASameClassExpAssistant()
	{
		return new ASameClassExpAssistantInterpreter(this);
	}

	public ASeqCompSeqExpAssistantInterpreter createASeqCompSeqExpAssistant()
	{
		return new ASeqCompSeqExpAssistantInterpreter(this);
	}

	public ASeqEnumSeqExpAssistantInterpreter createASeqEnumSeqExpAssistant()
	{
		return new ASeqEnumSeqExpAssistantInterpreter(this);
	}

	public ASetCompSetExpAssistantInterpreter createASetCompSetExpAssistant()
	{
		return new ASetCompSetExpAssistantInterpreter(this);
	}

	public ASetEnumSetExpAssistantInterpreter createASetEnumSetExpAssistant()
	{
		return new ASetEnumSetExpAssistantInterpreter(this);
	}

	public ASetRangeSetExpAssistantInterpreter createASetRangeSetExpAssistant()
	{
		return new ASetRangeSetExpAssistantInterpreter(this);
	}

	public ASubseqExpAssistantInterpreter createASubseqExpAssistant()
	{
		return new ASubseqExpAssistantInterpreter(this);
	}

	public ATupleExpAssistantInterpreter createATupleExpAssistant()
	{
		return new ATupleExpAssistantInterpreter(this);
	}

	public AVariableExpAssistantInterpreter createAVariableExpAssistant()
	{
		return new AVariableExpAssistantInterpreter(this);
	}

	public PExpAssistantInterpreter createPExpAssistant()
	{
		return new PExpAssistantInterpreter(this);
	}

	public SBinaryExpAssistantInterpreter createSBinaryExpAssistant()
	{
		return new SBinaryExpAssistantInterpreter(this);
	}

	public SMapExpAssistantInterpreter createSMapExpAssistant()
	{
		return new SMapExpAssistantInterpreter(this);
	}

	public SSeqExpAssistantInterpreter createSSeqExpAssistant()
	{
		return new SSeqExpAssistantInterpreter(this);
	}

	public SSetExpAssistantInterpreter createSSetExpAssistant()
	{
		return new SSetExpAssistantInterpreter(this);
	}

	public SUnaryExpAssistantInterpreter createSUnaryExpAssistant()
	{
		return new SUnaryExpAssistantInterpreter(this);
	}

	// module

	public AModuleModulesAssistantInterpreter createAModuleModulesAssistant()
	{
		return new AModuleModulesAssistantInterpreter(this);
	}

	public ModuleListAssistantInterpreter createModuleListAssistant()
	{
		return new ModuleListAssistantInterpreter(this);
	}

	// pattern

	public ABooleanPatternAssistantInterpreter createABooleanPatternAssistant()
	{
		return new ABooleanPatternAssistantInterpreter(this);
	}

	public ACharacterPatternAssistantInterpreter createACharacterPatternAssistant()
	{
		return new ACharacterPatternAssistantInterpreter(this);
	}

	public AConcatenationPatternAssistantInterpreter createAConcatenationPatternAssistant()
	{
		return new AConcatenationPatternAssistantInterpreter(this);
	}

	public AExpressionPatternAssistantInterpreter createAExpressionPatternAssistant()
	{
		return new AExpressionPatternAssistantInterpreter(this);
	}

	public AIdentifierPatternAssistantInterpreter createAIdentifierPatternAssistant()
	{
		return new AIdentifierPatternAssistantInterpreter(this);
	}

	public AIgnorePatternAssistantInterpreter createAIgnorePatternAssistant()
	{
		return new AIgnorePatternAssistantInterpreter(this);
	}

	public AIntegerPatternAssistantInterpreter createAIntegerPatternAssistant()
	{
		return new AIntegerPatternAssistantInterpreter(this);
	}

	public AMapPatternAssistantInterpreter createAMapPatternAssistant()
	{
		return new AMapPatternAssistantInterpreter(this);
	}

	public AMapPatternMapletAssistantInterpreter createAMapPatternMapletAssistant()
	{
		return new AMapPatternMapletAssistantInterpreter(this);
	}

	public AMapUnionPatternAssistantInterpreter createAMapUnionPatternAssistant()
	{
		return new AMapUnionPatternAssistantInterpreter(this);
	}

	public ANilPatternAssistantInterpreter createANilPatternAssistant()
	{
		return new ANilPatternAssistantInterpreter(this);
	}

	public AQuotePatternAssistantInterpreter createAQuotePatternAssistant()
	{
		return new AQuotePatternAssistantInterpreter(this);
	}

	public ARealPatternAssistantInterpreter createARealPatternAssistant()
	{
		return new ARealPatternAssistantInterpreter(this);
	}

	public ARecordPatternAssistantInterpreter createARecordPatternAssistant()
	{
		return new ARecordPatternAssistantInterpreter(this);
	}

	public ASeqPatternAssistantInterpreter createASeqPatternAssistant()
	{
		return new ASeqPatternAssistantInterpreter(this);
	}

	public ASetBindAssistantInterpreter createASetBindAssistant()
	{
		return new ASetBindAssistantInterpreter(this);
	}

	public ASetMultipleBindAssistantInterpreter createASetMultipleBindAssistant()
	{
		return new ASetMultipleBindAssistantInterpreter(this);
	}

	public ASetPatternAssistantInterpreter createASetPatternAssistant()
	{
		return new ASetPatternAssistantInterpreter(this);
	}

	public AStringPatternAssistantInterpreter createAStringPatternAssistant()
	{
		return new AStringPatternAssistantInterpreter(this);
	}

	public ATuplePatternAssistantInterpreter createATuplePatternAssistant()
	{
		return new ATuplePatternAssistantInterpreter(this);
	}

	public ATypeBindAssistantInterpreter createATypeBindAssistant()
	{
		return new ATypeBindAssistantInterpreter(this);
	}

	public ATypeMultipleBindAssistantInterpreter createATypeMultipleBindAssistant()
	{
		return new ATypeMultipleBindAssistantInterpreter(this);
	}

	public AUnionPatternAssistantInterpreter createAUnionPatternAssistant()
	{
		return new AUnionPatternAssistantInterpreter(this);
	}

	public PBindAssistantInterpreter createPBindAssistant()
	{
		return new PBindAssistantInterpreter(this);
	}

	public PMultipleBindAssistantInterpreter createPMultipleBindAssistant()
	{
		return new PMultipleBindAssistantInterpreter(this);
	}

	public PPatternAssistantInterpreter createPPatternAssistant()
	{
		return new PPatternAssistantInterpreter(this);
	}

	public PPatternListAssistantInterpreter createPPatternListAssistant()
	{
		return new PPatternListAssistantInterpreter(this);
	}

	// statement

	public AAlwaysStmAssistantInterpreter createAAlwaysStmAssistant()
	{
		return new AAlwaysStmAssistantInterpreter(this);
	}

	public AAssignmentStmAssistantInterpreter createAAssignmentStmAssistant()
	{
		return new AAssignmentStmAssistantInterpreter(this);
	}

	public AAtomicStmAssistantInterpreter createAAtomicStmAssistant()
	{
		return new AAtomicStmAssistantInterpreter(this);
	}

	public ACallObjectStatementAssistantInterpreter createACallObjectStatementAssistant()
	{
		return new ACallObjectStatementAssistantInterpreter(this);
	}

	public ACallStmAssistantInterpreter createACallStmAssistant()
	{
		return new ACallStmAssistantInterpreter(this);
	}

	public ACaseAlternativeStmAssistantInterpreter createACaseAlternativeStmAssistant()
	{
		return new ACaseAlternativeStmAssistantInterpreter(this);
	}

	public ACasesStmAssistantInterpreter createACasesStmAssistant()
	{
		return new ACasesStmAssistantInterpreter(this);
	}

	public ACyclesStmAssistantInterpreter createACyclesStmAssistant()
	{
		return new ACyclesStmAssistantInterpreter(this);
	}

	public ADurationStmAssistantInterpreter createADurationStmAssistant()
	{
		return new ADurationStmAssistantInterpreter(this);
	}

	public AElseIfStmAssistantInterpreter createAElseIfStmAssistant()
	{
		return new AElseIfStmAssistantInterpreter(this);
	}

	public AExitStmAssistantInterpreter createAExitStmAssistant()
	{
		return new AExitStmAssistantInterpreter(this);
	}

	public AForAllStmAssistantInterpreter createAForAllStmAssistant()
	{
		return new AForAllStmAssistantInterpreter(this);
	}

	public AForIndexStmAssistantInterpreter createAForIndexStmAssistant()
	{
		return new AForIndexStmAssistantInterpreter(this);
	}

	public AForPatternBindStmAssitantInterpreter createAForPatternBindStmAssitant()
	{
		return new AForPatternBindStmAssitantInterpreter(this);
	}

	public AIfStmAssistantInterpreter createAIfStmAssistant()
	{
		return new AIfStmAssistantInterpreter(this);
	}

	public ALetBeStStmAssistantInterpreter createALetBeStStmAssistant()
	{
		return new ALetBeStStmAssistantInterpreter(this);
	}

	public AReturnStmAssistantInterpreter createAReturnStmAssistant()
	{
		return new AReturnStmAssistantInterpreter(this);
	}

	public AStartStmAssistantInterpreter createAStartStmAssistant()
	{
		return new AStartStmAssistantInterpreter(this);
	}

	public ATixeStmAssistantInterpreter createATixeStmAssistant()
	{
		return new ATixeStmAssistantInterpreter(this);
	}

	public ATixeStmtAlternativeAssistantInterpreter createATixeStmtAlternativeAssistant()
	{
		return new ATixeStmtAlternativeAssistantInterpreter(this);
	}

	public ATrapStmAssistantInterpreter createATrapStmAssistant()
	{
		return new ATrapStmAssistantInterpreter(this);
	}

	public AWhileStmAssistantInterpreter createAWhileStmAssistant()
	{
		return new AWhileStmAssistantInterpreter(this);
	}

	public PStmAssistantInterpreter createPStmAssistant()
	{
		return new PStmAssistantInterpreter(this);
	}

	public SLetDefStmAssistantInterpreter createSLetDefStmAssistant()
	{
		return new SLetDefStmAssistantInterpreter(this);
	}

	public SSimpleBlockStmAssistantInterpreter createSSimpleBlockStmAssistant()
	{
		return new SSimpleBlockStmAssistantInterpreter(this);
	}

	// type

	public ABooleanBasicTypeAssistantInterpreter createABooleanBasicTypeAssistant()
	{
		return new ABooleanBasicTypeAssistantInterpreter(this);
	}

	public AInMapMapTypeAssistantInterpreter createAInMapMapTypeAssistant()
	{
		return new AInMapMapTypeAssistantInterpreter(this);
	}

	public ANamedInvariantTypeAssistantInterpreter createANamedInvariantTypeAssistant()
	{
		return new ANamedInvariantTypeAssistantInterpreter(this);
	}

	public AOptionalTypeAssistantInterpreter createAOptionalTypeAssistant()
	{
		return new AOptionalTypeAssistantInterpreter(this);
	}

	public AParameterTypeAssistantInterpreter createAParameterTypeAssistant()
	{
		return new AParameterTypeAssistantInterpreter(this);
	}

	public AProductTypeAssistantInterpreter createAProductTypeAssistant()
	{
		return new AProductTypeAssistantInterpreter(this);
	}

	public AQuoteTypeAssistantInterpreter createAQuoteTypeAssistant()
	{
		return new AQuoteTypeAssistantInterpreter(this);
	}

	public ARecordInvariantTypeAssistantInterpreter createARecordInvariantTypeAssistant()
	{
		return new ARecordInvariantTypeAssistantInterpreter(this);
	}

	public ASetTypeAssistantInterpreter createASetTypeAssistant()
	{
		return new ASetTypeAssistantInterpreter(this);
	}

	public AUnionTypeAssistantInterpreter createAUnionTypeAssistant()
	{
		return new AUnionTypeAssistantInterpreter(this);
	}

	public PTypeAssistantInterpreter createPTypeAssistant()
	{
		return new PTypeAssistantInterpreter(this);
	}

	public PTypeListAssistant createPTypeListAssistant()
	{
		return new PTypeListAssistant(this);
	}

	public SBasicTypeAssistantInterpreter createSBasicTypeAssistant()
	{
		return new SBasicTypeAssistantInterpreter(this);
	}

	public SInvariantTypeAssistantInterpreter createSInvariantTypeAssistant()
	{
		return new SInvariantTypeAssistantInterpreter(this);
	}

	public SMapTypeAssistantInterpreter createSMapTypeAssistant()
	{
		return new SMapTypeAssistantInterpreter(this);
	}

	@Override
	public IAnswer<LexNameList> getOldNameCollector()
	{
		return new OldNameCollector(this);
	}

}
