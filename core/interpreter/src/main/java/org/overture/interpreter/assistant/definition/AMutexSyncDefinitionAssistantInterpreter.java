package org.overture.interpreter.assistant.definition;

import org.overture.ast.assistant.IAstAssistant;
import org.overture.ast.definitions.AMutexSyncDefinition;
import org.overture.ast.expressions.PExp;
import org.overture.ast.factory.AstFactory;
import org.overture.ast.intf.lex.ILexNameToken;
import org.overture.ast.lex.LexIntegerToken;
import org.overture.ast.lex.LexKeywordToken;
import org.overture.ast.lex.LexNameList;
import org.overture.ast.lex.LexToken;
import org.overture.ast.lex.VDMToken;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;

public class AMutexSyncDefinitionAssistantInterpreter implements IAstAssistant
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public AMutexSyncDefinitionAssistantInterpreter(
			IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

	//FIXME: only used once. Inline it.
	public PExp getExpression(AMutexSyncDefinition sync, ILexNameToken excluding)
	{
		LexNameList list = null;

		if (sync.getOperations().size() == 1)
		{
			list = new LexNameList();
			list.addAll(sync.getOperations());
		} else
		{
			list = new LexNameList();
			list.addAll(sync.getOperations());
			list.remove(excluding);
		}

		return AstFactory.newAEqualsBinaryExp(AstFactory.newAHistoryExp(sync.getLocation(), new LexToken(sync.getLocation(), VDMToken.ACTIVE), list), new LexKeywordToken(VDMToken.EQUALS, sync.getLocation()), AstFactory.newAIntLiteralExp(new LexIntegerToken(0, sync.getLocation())));
	}

}
