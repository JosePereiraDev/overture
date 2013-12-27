package org.overture.typechecker;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;

import org.overture.ast.definitions.PDefinition;
import org.overture.ast.typechecker.NameScope;
import org.overture.ast.types.PType;
import org.overture.typechecker.assistant.ITypeCheckerAssistantFactory;

public class TypeCheckInfo
{
	public final ITypeCheckerAssistantFactory assistantFactory;
	/*
	 * Added by RWL The Context allows us to resolve the following: (AnotherChecker,AnotherQuestion) ->
	 * (OvertureChecker,OvertureQuestion) -> (AnotherChecker, OvertureQuestion) Now the AnotherChecker is aware that
	 * only a OvertureQuestion is available here, however it may need its AnotherQuestion, right, to resolve that we
	 * have this context: (AnotherChecker,AnotherQuestion) -> Create Overture Question with AnotherQuestion in its
	 * context -> (OverutreChecker,OvertureQuestion) work as it should (AnotherChecker,OverTureQuestion) -> Get from the
	 * context the earlier AnotherQuestion. Other things can be added to the context and it may be used for any purpose.
	 * It is not used by Overture. (Remove this comment it is starts getting used by overture !)
	 */
	private static final Map<Class<?>, Object> context = new HashMap<Class<?>, Object>();

	public static void clearContext()
	{
		context.clear();
	}

	@SuppressWarnings("unchecked")
	private static <T> Stack<T> lookupListForType(Class<T> clz)
	{
		Object o = context.get(clz);
		if (o instanceof List<?>)
		{
			List<?> list = List.class.cast(o);
			if (list.size() > 0)
			{
				Object first = list.get(0);
				if (first != null && clz.isInstance(first))
				{
					return (Stack<T>) list;
				}
			}
		}
		return null;
	}

	/**
	 * Returns the value associated with key. Implementation detail: Notice the map is shared between all instances.
	 * 
	 * @param key
	 */
	public <T> T contextGet(Class<T> key)
	{
		synchronized (TypeCheckInfo.class)
		{
			Stack<T> contextStack = lookupListForType(key);
			if (contextStack != null)
			{
				return contextStack.peek();
			}

		}
		return null;
	}

	/**
	 * Associates the given key with the given value.
	 * 
	 * @param key
	 * @param value
	 */
	public <T> void contextSet(Class<T> key, T value)
	{
		synchronized (TypeCheckInfo.class)
		{
			Stack<T> contextStack = lookupListForType(key);
			if (contextStack == null)
			{
				contextStack = new Stack<T>();
				context.put(key, contextStack);
			}
			contextStack.push(value);
		}
	}

	/**
	 * Returns the value associated with key, and removes that binding from the context.
	 * 
	 * @param key
	 * @return value
	 */
	public <T> T contextRem(Class<T> key)
	{
		synchronized (TypeCheckInfo.class)
		{
			Stack<T> contextStack = lookupListForType(key);
			if (contextStack != null)
			{
				return contextStack.pop();
			}
		}
		return null;
	}

	final public Environment env;
	public NameScope scope;
	public LinkedList<PType> qualifiers;

	public TypeCheckInfo(ITypeCheckerAssistantFactory assistantFactory,
			Environment env, NameScope scope, LinkedList<PType> qualifiers)
	{
		this.assistantFactory = assistantFactory;
		this.env = env;
		this.scope = scope;
		this.qualifiers = qualifiers;
	}

	public TypeCheckInfo(ITypeCheckerAssistantFactory assistantFactory,
			Environment env, NameScope scope)
	{
		this.assistantFactory = assistantFactory;
		this.env = env;
		this.scope = scope;
	}

	public TypeCheckInfo(ITypeCheckerAssistantFactory assistantFactory,
			Environment env)
	{
		this.assistantFactory = assistantFactory;
		this.env = env;
	}

	public TypeCheckInfo()
	{
		this.assistantFactory = null;
		env = null;
	}

	@Override
	public String toString()
	{
		return "Scope: " + scope + "\n" + env;
	}

	public TypeCheckInfo newScope(NameScope newScope)
	{
		TypeCheckInfo info = new TypeCheckInfo(assistantFactory, env, newScope, qualifiers);
		return info;
	}

	public TypeCheckInfo newScope(List<PDefinition> definitions)
	{
		return newScope(definitions, scope);
	}

	public TypeCheckInfo newInfo(Environment newEnv)
	{
		TypeCheckInfo info = new TypeCheckInfo(assistantFactory, newEnv, scope, qualifiers);
		return info;
	}

	public TypeCheckInfo newInfo(Environment newEnv, NameScope newScope)
	{
		TypeCheckInfo info = new TypeCheckInfo(assistantFactory, newEnv, newScope, qualifiers);
		return info;
	}

	public TypeCheckInfo newScope(List<PDefinition> definitions,
			NameScope newScope)
	{
		Environment newEnv = new FlatCheckedEnvironment(assistantFactory, definitions, env, newScope);
		TypeCheckInfo info = new TypeCheckInfo(assistantFactory, newEnv, newScope);
		return info;
	}
}
