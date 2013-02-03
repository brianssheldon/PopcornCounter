package org.bubba.popcorncounter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;

import android.content.Context;

public class PopcornDao implements Serializable
{
	private static final String LIST_OF_POPCORN = "listOfPopcorn";
	private static final long serialVersionUID = 197245L;
	private ArrayList<Popcorn> list;

	public PopcornDao()
	{
		list = new ArrayList<Popcorn>();
	}
	
	public ArrayList<Popcorn> readFile(Context context)
	{
		ArrayList<Popcorn> list;
		try
		{
			FileInputStream fis = context.openFileInput(LIST_OF_POPCORN);
	    	ObjectInputStream in = new ObjectInputStream(fis);
	    	list = (ArrayList<Popcorn>) in.readObject();
	    	in.close();
	    	fis.close();
		}
		catch (Exception e)
		{
			try
			{
				list = new ArrayList<Popcorn>();
				list.add(new Popcorn("Long press to delete"));
				
				FileOutputStream fos = context.openFileOutput(LIST_OF_POPCORN, Context.MODE_PRIVATE);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				out.writeObject(list);
				out.close();
				fos.close();
			}
			catch (Exception e2)
			{
				e2.printStackTrace();
				list = new ArrayList<Popcorn>();
			}
		}
		return list;
	}
	
	public void writeFile(ArrayList<Popcorn> list, Context context)
	{
		try
		{
			FileOutputStream fos = context.openFileOutput(LIST_OF_POPCORN, Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(list);
			out.close();
			fos.close();
		}
		catch (Exception e2)
		{
			e2.printStackTrace();
		}
	}
	
	public ArrayList<Popcorn> getList()
	{
		return list;
	}

	public void setList(ArrayList<Popcorn> list)
	{
		this.list = list;
	}

	public void remove(String nameFinal, Context context)
	{
		ArrayList<Popcorn> popcornList = readFile(context);
		ArrayList<Popcorn> newPopcornList = new ArrayList<Popcorn>();
		Popcorn popcorn;
		
		for (Iterator<Popcorn> iter = popcornList.iterator(); iter.hasNext();)
		{
			popcorn = iter.next();
			if(!nameFinal.equals(popcorn.getName()))
			{
				newPopcornList.add(popcorn);
			}
		}
		writeFile(newPopcornList, context);
	}
}