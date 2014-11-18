/*
 * #%~
 * VDM Code Generator Runtime
 * %%
 * Copyright (C) 2008 - 2014 Overture
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/gpl-3.0.html>.
 * #~%
 */
package org.overture.codegen.runtime;

public class Utils
{
	public static int hashCode(Object... fields)
	{
		if(fields == null)
			throw new IllegalArgumentException("Fields cannot be null");

		int hashcode = 0;
		
		for(int i = 0; i < fields.length; i++)
		{
			Object currentField = fields[i];
			hashcode += currentField != null ? currentField.hashCode() : 0;
		}
		
		return hashcode;
	}
	
	public static int index(Number value)
	{
		if(value.longValue() < 1)
			throw new IllegalArgumentException("VDM subscripts must be >= 1");
		
		return toInt(value) - 1;
	}
	
	public static int toInt(Number value) {
		
		long valueLong = value.longValue();
		
	    if (valueLong < Integer.MIN_VALUE || valueLong > Integer.MAX_VALUE) {
	        throw new IllegalArgumentException
	            (valueLong + " Casting the long to an int will change its value");
	    }
	    return (int) valueLong;
	}
		
	public static String recordToString(Record record, Object... fields)
	{
		if(record == null)
			throw new IllegalArgumentException("Record cannot be null in recordToString");
		
		StringBuilder str = formatFields(", %s", fields);

		return "mk_" + record.getClass().getSimpleName() + "(" + str + ")";
	}
	
	private static StringBuilder formatFields(String format, Object... fields)
	{
		if(fields == null)
			throw new IllegalArgumentException("Fields cannot be null in formatFields");
		
		StringBuilder str = new StringBuilder();

		if (fields.length > 0)
		{
			str.append(fields[0]);

			for (int i = 1; i < fields.length; i++)
			{
				str.append(String.format(format, Utils.toString(fields[i])));
			}
		}
		return str;
	}
	
	@SuppressWarnings("unchecked")
	public static <T extends ValueType> T clone(T t)
	{
		return (T) (t != null ? t.clone() : t);
	}
	
	public static String toString(Object obj)
	{
		if(obj == null)
		{
			return "nil";
		}
		else if(obj instanceof Number)
		{
			Number n = (Number) obj;
			
			if(n.doubleValue() % 1 == 0)
			{
				return Long.toString(n.longValue());
			}
			else 
			{
				return Double.toString(n.doubleValue());
			}
		}
		else if(obj instanceof String)
		{
			return "\"" + obj.toString() + "\"";
		}
		
		return obj.toString();
	}
	
	public static boolean equals(Object left, Object right)
	{
		return left != null ? left.equals(right) : right == null; 
	}
	
	public static <T> T postCheck(T returnValue, boolean postResult, String name)
	{
		if(postResult)
		{
			return returnValue;
		}
		
		throw new RuntimeException("Postcondition failure: post_" + name);
	}
	
	public static boolean is_bool(Object value)
	{
		return value instanceof Boolean;
	}
	
	public static boolean is_nat(Object value)
	{
		return isIntWithinRange(value, 0);
	}	
	
	public static boolean is_nat1(Object value)
	{
		return isIntWithinRange(value, 1);
	}

	public static boolean is_int(Object value)
	{
		Double doubleValue = getDoubleValue(value);
		
		return is_int(doubleValue);
	}

	public static boolean is_rat(Object value)
	{
		return value instanceof Number;
	}
	
	public static boolean is_real(Object value)
	{
		return value instanceof Number;
	}
	
	public static boolean is_char(Object value)
	{
		return value instanceof Character;
	}
	
	public static boolean is_token(Object value)
	{
		return value instanceof Token;
	}

	@SuppressWarnings("rawtypes")
	public static boolean is_Tuple(Object exp, Class... types)
	{
		return exp instanceof Tuple && ((Tuple) exp).compatible(types);
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean is_(Object exp, Class type)
	{
		return exp != null && exp.getClass() == type;
	}

	private static boolean is_int(Double doubleValue)
	{
		return doubleValue != null && (doubleValue == Math.floor(doubleValue)) && !Double.isInfinite(doubleValue);
	}
	
	private static boolean isIntWithinRange(Object value, int lowerLimit)
	{
		Double doubleValue = getDoubleValue(value);
		
		if(!is_int(doubleValue))
		{
			return false;
		}
		
		return doubleValue >= lowerLimit;
	}
	
	private static Double getDoubleValue(Object value)
	{
		if(!(value instanceof Number))
		{
			return null;
		}
		
		Double doubleValue = ((Number) value).doubleValue();
		
		return doubleValue;
	}
}
