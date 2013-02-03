package org.bubba.popcorncounter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Iterator;

public class PopcornUtil
{
	public static ArrayList<Popcorn> getPopcornList()
	{
		ArrayList<Popcorn> list = new ArrayList<Popcorn>();
		
		list.add(new Popcorn("Do-si-dos", new BigDecimal("3.50"), 0));
		list.add(new Popcorn("Samoas", new BigDecimal("3.50"), 0));
		list.add(new Popcorn("Savannah Smiles", new BigDecimal("3.50"), 0));
		list.add(new Popcorn("Tagalongs", new BigDecimal("3.50"), 0));
		list.add(new Popcorn("Thin Mints", new BigDecimal("3.50"), 0));
		list.add(new Popcorn("Trefoils", new BigDecimal("3.50"), 0));
		
		return list;
	}

	public static String getPopcornTotalsForEmail(ArrayList<PopcornSold> arrayList)
	{
		StringBuffer sb = new StringBuffer(100);
		sb.append("\n\nPopcorn Totals\n\n");
		BigDecimal grandTotal = new BigDecimal("0.00").setScale(2);
		int grandTotalQuantity = 0;
		ArrayList<Popcorn> gtList = new ArrayList<Popcorn>();

    	int namelen = 20;
    	int quantitylen = 5;
    	int saletotallen = 8;
    	
		for (Iterator<PopcornSold> iter = arrayList.iterator(); iter.hasNext();)
		{
        	PopcornSold gscs = (PopcornSold) iter.next();
        	
        	for (Iterator<Popcorn> iter2 = gscs.getPopcornSoldList().iterator(); iter2.hasNext();)
			{
				Popcorn gsc = iter2.next();
				boolean found = false;
				
				for(Iterator<Popcorn> iter3 = gtList.iterator(); iter3.hasNext();)
				{
					Popcorn gtPopcorn = iter3.next();
					if(gtPopcorn.getName().equals(gsc.getName()))
					{
						gtPopcorn.setQuantity(gtPopcorn.getQuantity() + gsc.getQuantity());
						found = true;
						break;
					}
				}
				
				if(!found)
				{
					Popcorn newPopcorn = new Popcorn();
					newPopcorn.setName(gsc.getName());
					newPopcorn.setQuantity(gsc.getQuantity());
					newPopcorn.setCost(gsc.getCost());
					gtList.add(newPopcorn);
				}
			}
		}
		

		for(Iterator<Popcorn> iter3 = gtList.iterator(); iter3.hasNext();)
		{
			Popcorn gtPopcorn = iter3.next();

	    	namelen = whichIsLarger(gtPopcorn.getName().length(), namelen);
	    	quantitylen = whichIsLarger(("" + gtPopcorn.getQuantity()).length(), quantitylen);
	    	saletotallen = whichIsLarger(gtPopcorn.getTotal().toString().length(), saletotallen);
		}
		for(Iterator<Popcorn> iter3 = gtList.iterator(); iter3.hasNext();)
		{
			Popcorn gtPopcorn = iter3.next();
			sb.append(padWithSpaces(gtPopcorn.getName(), namelen) + " ");
			sb.append(padWithSpaces("" + gtPopcorn.getQuantity(), quantitylen) + " ");
			sb.append(padWithSpaces("" + gtPopcorn.getTotal(), saletotallen) + " ");
			sb.append("\n");
			
			grandTotal = grandTotal.add(gtPopcorn.getTotal());
			grandTotalQuantity += gtPopcorn.getQuantity();
		}
		
		sb.append("\n");
		sb.append(padWithSpaces("Total", 15) + " ");
		sb.append(padWithSpaces("" + grandTotalQuantity, 3) + " ");
		sb.append(padWithSpaces(grandTotal.toString(), 6) + " ");
		sb.append("\n");
		
		return sb.toString();
	}

	public static int whichIsLarger(int length, int namelen)
	{
		if(length > namelen) return length;
		return namelen;
	}

	private static String padWithSpaces(String name, int i)
	{
		return (name + "                                         ").substring(0, i);
	}
}