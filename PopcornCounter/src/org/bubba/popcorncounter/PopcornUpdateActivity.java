package org.bubba.popcorncounter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PopcornUpdateActivity extends Activity
{
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		populateScreen();
	}

	private void populateScreen()
	{
		setContentView(R.layout.popcornupdate);

		Intent sender = getIntent();
		final int id = sender.getExtras().getInt("id");
		
		final ArrayList<PopcornSold> list = PopcornCounterActivity.readGsFilex(this);
		
		if(list == null || id < 0 || id > list.size())
		{
			return;
		}
		
		PopcornSold gscs = list.get(id);
		
		TextView titleView = (TextView) findViewById(R.id.popcornfortextview);
		String gsName = gscs.getName();
		final String gsNameFinal = gsName;
		titleView.setText("Popcorn for  " + gsName);
		
		LinearLayout ll = (LinearLayout) findViewById(R.id.mylayoutxxyz);
		if (ll == null)
		{
			return;
		}
		
		Button emailButton = (Button) findViewById(R.id.popcornUpdateEmailButton);
        emailButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
		        Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);
		        emailIntent.setType("plain/text"); 
		        
				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "Popcorn List as of "
						+ (new Date()).toString()); 
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "List of Popcorn\n"
						+ getPopcornListEmailString(id));
		        startActivity(emailIntent); 
            }
        });

		ArrayList<Popcorn> gsList = gscs.getPopcornSoldList();
		int i = 0;
		int totalQuantity = 0;
		BigDecimal totalCost = BigDecimal.ZERO;
		
		for (Iterator<Popcorn> iterator = gsList.iterator(); iterator.hasNext();)
		{
			final Popcorn popcorn = (Popcorn) iterator.next();
			LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		    
			RelativeLayout rl = (RelativeLayout) linflater.inflate(R.layout.popcornlistupdaterow, null);
			
			TextView tvDesc = (TextView) rl.findViewById(R.id.popcornrowdesc);
			tvDesc.setText(popcorn.getName());
			
	        tvDesc.setLongClickable(true);
	        tvDesc.setOnLongClickListener(new OnLongClickListener()
			{
				public boolean onLongClick(View v)
				{
					final View vv = v;
					
			        new AlertDialog.Builder(v.getContext())
			        .setIcon(android.R.drawable.ic_dialog_alert)
			        .setTitle("Delete Item?")
			        .setMessage("Do you want to delete\n\n" + popcorn.getName() + "?")
			        .setPositiveButton("Delete", new DialogInterface.OnClickListener() 
			        {
			            public void onClick(DialogInterface dialog, int which)
			            {	// they have clicked on the description so remove this popcorn
			            	removePopcornFromGsList(list, gsNameFinal, popcorn.getName(), vv.getContext());
			            	populateScreen();
			            }
			        })
			        .setNegativeButton("cancel", null)
			        .show();
					return true;
				}
			});

			TextView tvPrice = (TextView) rl.findViewById(R.id.popcornrowprice);
			tvPrice.setText("" + popcorn.getCost().setScale(2)); 

			TextView tvQuantity = (TextView) rl.findViewById(R.id.popcornrowquantity);
			int quantity = popcorn.getQuantity();
			totalQuantity += quantity;
			totalCost = totalCost.add(popcorn.getTotal());
			
			tvQuantity.setText("" + quantity);
			UpdatePopcornLocator ucl = new UpdatePopcornLocator(id, i, gsList);
			tvQuantity.setTag(ucl);

			TextView tvTotal = (TextView) rl.findViewById(R.id.popcornrowtotalcost);
			tvTotal.setText(popcorn.getCost().multiply(new BigDecimal(popcorn.getQuantity())).setScale(2).toString()); 

			Button plusSign = (Button) rl.findViewById(R.id.popcornrowplus);
			plusSign.setOnClickListener(new View.OnClickListener() 
	        {public void onClick(View v){(new UpdatePopcornTotals()).updateRow(v, 1);}});
			
			Button plusMinus = (Button) rl.findViewById(R.id.popcornrowminus);
			plusMinus.setOnClickListener(new View.OnClickListener()
			{public void onClick(View v){(new UpdatePopcornTotals()).updateRow(v, -1);}});
			
			ll.addView(rl);
			
			LayoutInflater x = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView tvLine = (TextView) x.inflate(R.layout.thelineb, null);
			ll.addView(tvLine);
//			getLineDivider(ll);
			i += 1;
		}

//		LayoutInflater x = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//		TextView tvLine = (TextView) x.inflate(R.layout.thelineb, null);
//		ll.addView(tvLine);
		
		writeTotalLine(id, ll, i, totalQuantity, totalCost);
	}
	
	private void removePopcornFromGsList(
			ArrayList<PopcornSold> list,
			String gsNameFinal, String popcornName, Context context)
	{
		ArrayList<PopcornSold> newList = new ArrayList<PopcornSold>();
		PopcornSold popcornSold;
		
		for (Iterator<PopcornSold> iter = list.iterator(); iter.hasNext();)
		{
			popcornSold = iter.next();
			if(gsNameFinal.equals(popcornSold.getName()))
			{
				ArrayList<Popcorn> popcornSoldList = popcornSold.getPopcornSoldList();
				ArrayList<Popcorn> newPopcornSoldList = new ArrayList<Popcorn>();
				Popcorn popcorn;
				
				for (Iterator<Popcorn> iter2 = popcornSoldList.iterator(); iter2.hasNext();)
				{
					popcorn = iter2.next();
					if(!popcorn.getName().equals(popcornName))
					{
						newPopcornSoldList.add(popcorn);						
					}
				}
				popcornSold.setPopcornSoldList(newPopcornSoldList);
			}
			newList.add(popcornSold);
		}
		PopcornCounterActivity.saveGsFilex(context, newList);
	}
	
	void writeTotalLine(int id, LinearLayout ll, int i, int totalQuantity, BigDecimal totalCost)
	{
		LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	    
		RelativeLayout rl = (RelativeLayout) linflater.inflate(R.layout.popcorntotalrow, null);

		TextView tvDesc = (TextView) rl.findViewById(R.id.popcornrowdesc);
		tvDesc.setText("Total");

		TextView tvPrice = (TextView) rl.findViewById(R.id.popcornrowprice);
		tvPrice.setText("    ");//3.50"); 

		TextView tvQuantity = (TextView) rl.findViewById(R.id.popcornrowquantity);
		tvQuantity.setText("" + totalQuantity);

		TextView tvTotal = (TextView) rl.findViewById(R.id.popcornrowtotalcost);
		tvTotal.setText(totalCost.setScale(2).toString()); 
		
		ll.addView(rl);
	}

	void getLineDivider(LinearLayout ll)
	{
		View view = new View(this);
		view.setBackgroundColor(0xFFFFFFFF);
		LayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT, 2);
		view.setLayoutParams(params);
		ll.addView(view);
	}
	
	public class UpdatePopcornTotals
	{
		public void updateRow(View v, int i)
		{
        	RelativeLayout rl = (RelativeLayout) v.getParent();
	    	TextView tvQuantity = (TextView) rl.findViewById(R.id.popcornrowquantity);
	    	int q = (Integer.valueOf(tvQuantity.getText().toString())) + i;
			tvQuantity.setText("" + q);
			
			TextView tvPrice = (TextView) rl.findViewById(R.id.popcornrowprice);
	    	BigDecimal p = new BigDecimal(tvPrice.getText().toString());
	
			TextView tvTotal = (TextView) rl.findViewById(R.id.popcornrowtotalcost);
			tvTotal.setText(p.multiply(new BigDecimal(q)).setScale(2).toString());
			
			UpdatePopcornLocator ucl = (UpdatePopcornLocator) tvQuantity.getTag();
			
			ArrayList<PopcornSold> list = PopcornCounterActivity.readGsFilex(v.getContext());
			PopcornSold gscs = list.get(ucl.personRow);
			Popcorn row = gscs.getPopcornSoldList().get(ucl.getPopcornRow());
			row.setQuantity(q);
    		PopcornCounterActivity.saveGsFilex(v.getContext(), list);
    		
    		LinearLayout ll = (LinearLayout)v.getParent().getParent();
    		
    		RelativeLayout rlTotalRow = (RelativeLayout)ll.findViewById(R.id.rlpopcorntotalrow);
    		TextView tvTotalQuantity = (TextView) rlTotalRow.findViewById(R.id.popcornrowquantity);
    		TextView tvTotalTotal = (TextView) rlTotalRow.findViewById(R.id.popcornrowtotalcost);
	    	
    		tvTotalQuantity.setText("" + (Integer.parseInt(tvTotalQuantity.getText().toString()) + i));
    		
    		BigDecimal bd = new BigDecimal(tvTotalTotal.getText().toString());
    		if(i == 1) bd = bd.add(row.getCost());
    		if(i == -1) bd = bd.subtract(row.getCost());
    		tvTotalTotal.setText(bd.toString());
		}
	}
	
	public class UpdatePopcornLocator
	{
		private int personRow = -1;
		private int popcornRow = -1;
		private ArrayList<Popcorn> gsList;
		
		public UpdatePopcornLocator(int personRow, int popcornRow, ArrayList<Popcorn> gsList)
		{
			super();
			this.personRow = personRow;
			this.popcornRow = popcornRow;
			this.gsList = gsList;
		}
		
		public int getPersonRow()
		{
			return personRow;
		}
		public void setPersonRow(int personRow)
		{
			this.personRow = personRow;
		}
		public int getPopcornRow()
		{
			return popcornRow;
		}
		public void setPopcornRow(int popcornRow)
		{
			this.popcornRow = popcornRow;
		}
		public ArrayList<Popcorn> getGsList()
		{
			return gsList;
		}
		public void setGsList(ArrayList<Popcorn> gsList)
		{
			this.gsList = gsList;
		} 
	}

	protected String getPopcornListEmailString(int id)
	{
		StringBuffer sb = new StringBuffer(100);
		
		ArrayList<PopcornSold> arrayList = PopcornCounterActivity.readGsFilex(this);
		
    	PopcornSold gscs = arrayList.get(id); 
    			//(PopcornSold) iter.next();
    	
    	BigDecimal personTotal = new BigDecimal("0.00").setScale(2);
    	sb.append(gscs.getName() + "\n");

    	int namelen = 20;
    	int quantitylen = 5;
    	int costlen = 8; 
    	int saletotallen = 8;
    	
    	for (Iterator<Popcorn> iter2 = gscs.getPopcornSoldList().iterator(); iter2.hasNext();)
		{
			Popcorn gsc = iter2.next();
			namelen = PopcornUtil.whichIsLarger(gsc.getName().length(), namelen);
			quantitylen = PopcornUtil.whichIsLarger(gsc.getQuantity(), quantitylen);
			costlen = PopcornUtil.whichIsLarger(("" + gsc.getCost()).toString().length(), costlen);
			saletotallen = PopcornUtil.whichIsLarger(gsc.getTotal().toString().length(), namelen);
		}
    	
    	for (Iterator<Popcorn> iter2 = gscs.getPopcornSoldList().iterator(); iter2.hasNext();)
		{
			Popcorn gsc = iter2.next();
			sb.append(gsc.toStringBuffer(namelen, quantitylen, costlen, saletotallen));
			BigDecimal saleTotal = gsc.getTotal();
			personTotal = personTotal.add(saleTotal);
		}
    	sb.append("    total = " + personTotal.toString());
		
		return sb.toString();
	}
}
