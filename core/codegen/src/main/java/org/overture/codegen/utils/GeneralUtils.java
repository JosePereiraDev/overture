/*
 * #%~
 * VDM Code Generator
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
package org.overture.codegen.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class GeneralUtils
{
	public static boolean isEscapeSequence(char c)
	{
		return c == '\t' || c == '\b' || c == '\n' || c == '\r' || c == '\f'
				|| c == '\'' || c == '\"' || c == '\\';
	}

	public static StringBuffer readFromFile(String relativepath)
			throws IOException
	{
		InputStream input = GeneralUtils.class.getResourceAsStream('/' + relativepath.replace("\\", "/"));

		return readFromInputStream(input);
	}

	public static StringBuffer readFromInputStream(InputStream input)
			throws IOException
	{
		if (input == null)
		{
			return null;
		}

		StringBuffer data = new StringBuffer();
		int c = 0;
		while ((c = input.read()) != -1)
		{
			data.append((char) c);
		}
		input.close();

		return data;
	}

	public static String readFromFile(File file) throws IOException
	{
		StringBuilder data = new StringBuilder();
		BufferedReader in = null;

		try
		{
			in = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"));

			String str = "";

			while ((str = in.readLine()) != null)
			{
				data.append(str + System.getProperty("line.separator"));
			}

		} finally
		{
			if (in != null)
			{
				in.close();
			}
		}

		return data.toString().trim();
	}

	public static List<File> getFiles(File folder)
	{
		File[] listOfFiles = folder.listFiles();

		List<File> fileList = new LinkedList<File>();

		if (listOfFiles == null || listOfFiles.length == 0)
		{
			return fileList;
		}

		for (File file : listOfFiles)
		{
			if (file.isFile())
			{
				fileList.add(file);
			}
		}

		return fileList;
	}

	public static List<File> getFilesFromPaths(String[] args)
	{
		List<File> files = new LinkedList<File>();

		for (int i = 1; i < args.length; i++)
		{
			String fileName = args[i];
			File file = new File(fileName);
			files.add(file);
		}

		return files;
	}

	public static void deleteFolderContents(File folder)
	{
		deleteFolderContents(folder, new ArrayList<String>());
	}

	public static void deleteFolderContents(File folder,
			List<String> folderNamesToAvoid)
	{
		if (folder == null)
		{
			return;
		}

		File[] files = folder.listFiles();

		if (files == null)
		{
			return;
		}

		for (File f : files)
		{
			if (f.isDirectory())
			{
				if (!folderNamesToAvoid.contains(f.getName()))
				{
					deleteFolderContents(f, folderNamesToAvoid);
				}
			} else
			{
				f.delete();
			}
		}
	}

	public static String[] concat(String[] left, String[] right)
	{
		int leftLength = left.length;
		int rightLeft = right.length;

		String[] result = new String[leftLength + rightLeft];

		System.arraycopy(left, 0, result, 0, leftLength);
		System.arraycopy(right, 0, result, leftLength, rightLeft);

		return result;
	}
	
	public static String cleanupWhiteSpaces(String str)
	{
		return str.replaceAll("\\s+", " ").trim();
	}
}
