// this file is automatically generated by treegen. do not modify!

package org.overture.tools.treegen.ast.imp;

// import the abstract tree interfaces
import org.overture.tools.treegen.ast.itf.*;

public class TreeGenAstValueDefinition extends TreeGenAstDefinitions implements ITreeGenAstValueDefinition
{
	// private member variable (key)
	private String m_key = new String();

	// public operation to retrieve the embedded private field value
	public String getKey()
	{
		return m_key;
	}

	// public operation to set the embedded private field value
	public void setKey(String p_key)
	{
		// consistency check (field must be non null!)
		assert(p_key != null);

		// instantiate the member variable
		m_key = p_key;
	}

	// private member variable (value)
	private String m_value = new String();

	// public operation to retrieve the embedded private field value
	public String getValue()
	{
		return m_value;
	}

	// public operation to set the embedded private field value
	public void setValue(String p_value)
	{
		// consistency check (field must be non null!)
		assert(p_value != null);

		// instantiate the member variable
		m_value = p_value;
	}

	// default constructor
	public TreeGenAstValueDefinition()
	{
		super();
		m_key = null;
		m_value = null;
	}

	// auxiliary constructor
	public TreeGenAstValueDefinition(
		String p_key,
		String p_value
	) {
		super();
		setKey(p_key);
		setValue(p_value);
	}

	// visitor support
	public void accept(ITreeGenAstVisitor pVisitor) { pVisitor.visitValueDefinition(this); }

	// the identity function
	public String identify() { return "TreeGenAstValueDefinition"; }
}
