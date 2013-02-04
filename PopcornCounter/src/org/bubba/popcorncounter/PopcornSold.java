package org.bubba.popcorncounter;

import java.io.Serializable;
import java.util.ArrayList;

public class PopcornSold implements Serializable
{
	private static final long serialVersionUID = 123L;
	private String name;
	private ArrayList<Popcorn> popcornSoldList;
	private String address1;
	private String address2;
	private String phone;
	
	public PopcornSold()
	{
		popcornSoldList = new ArrayList<Popcorn>();
		name = "";
		address1 = "";
		address2 = "";
		phone = "";
	}

	public PopcornSold(String name, ArrayList<Popcorn> popcornSoldList)
	{
		super();
		this.name = name;
		this.popcornSoldList = popcornSoldList;
		address1 = "";
		address2 = "";
		phone = "";
	}
	
	public String getName()
	{
		return name;
	}
	public void setName(String name)
	{
		this.name = name;
	}
	public ArrayList<Popcorn> getPopcornSoldList()
	{
		return popcornSoldList;
	}
	public void setPopcornSoldList(ArrayList<Popcorn> popcornSoldList)
	{
		this.popcornSoldList = popcornSoldList;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}
}