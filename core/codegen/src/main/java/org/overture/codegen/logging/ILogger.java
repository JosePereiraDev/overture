package org.overture.codegen.logging;

public interface ILogger
{	
	public void setSilent(boolean silent);
	
	public void println(String msg);
	
	public void print(String msg);
	
	public void printErrorln(String msg);
	
	public void printError(String msg);
	
}
