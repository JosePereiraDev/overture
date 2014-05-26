package org.overture.interpreter.assistant.definition;

import org.overture.ast.definitions.ABusClassDefinition;
import org.overture.ast.definitions.PDefinition;
import org.overture.ast.factory.AstFactoryTC;
import org.overture.ast.types.AClassType;
import org.overture.interpreter.assistant.IInterpreterAssistantFactory;
import org.overture.interpreter.runtime.Context;
import org.overture.interpreter.values.BUSValue;
import org.overture.interpreter.values.NameValuePairList;
import org.overture.interpreter.values.NameValuePairMap;
import org.overture.interpreter.values.ObjectValue;
import org.overture.interpreter.values.ValueList;
import org.overture.interpreter.values.ValueSet;
import org.overture.parser.lex.LexException;
import org.overture.parser.syntax.ParserException;

public class ABusClassDefinitionAssitantInterpreter
{
	protected static IInterpreterAssistantFactory af;

	@SuppressWarnings("static-access")
	public ABusClassDefinitionAssitantInterpreter(
			IInterpreterAssistantFactory af)
	{
		this.af = af;
	}

	public static BUSValue makeVirtualBUS(ValueSet cpus)
	{
		try
		{
			return new BUSValue((AClassType) AstFactoryTC.newABusClassDefinition(af).getType(), cpus);
		} catch (ParserException e)
		{

		} catch (LexException e)
		{

		}
		return null;
	}

	public static ObjectValue newInstance(ABusClassDefinition node,
			PDefinition ctorDefinition, ValueList argvals, Context ctxt)
	{
		NameValuePairList nvpl = af.createPDefinitionListAssistant().getNamedValues(node.getDefinitions(), ctxt);
		NameValuePairMap map = new NameValuePairMap();
		map.putAll(nvpl);

		return new BUSValue((AClassType) node.getClasstype(), map, argvals);
	}

}
