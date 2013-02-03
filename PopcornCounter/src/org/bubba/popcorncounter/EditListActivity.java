package org.bubba.popcorncounter;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EditListActivity extends Activity
{
	PopcornDao popcornDao = new PopcornDao();
	
    /** Called when the activity is first created. */
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        drawScreen();
    }

	private void drawScreen() 
	{
		setContentView(R.layout.popcornlistupdate);
        
        ArrayList<Popcorn> list = popcornDao.readFile(this);
        
        LinearLayout ll = (LinearLayout) findViewById(R.id.updatePopcornListList);
        
        for (Iterator<Popcorn> iterator = list.iterator(); iterator.hasNext();)
		{
			Popcorn popcorn = (Popcorn) iterator.next();
			populateRow(ll, popcorn);
		}
        
        populateRow(ll, new Popcorn());
        
        makeSaveButtonListener();
        makeExitButtonListener();
	}
	
	void populateRow(LinearLayout ll, Popcorn popcorn)
	{
		LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		RelativeLayout rl = (RelativeLayout) linflater.inflate(R.layout.popcornupdaterow, null);
		
		TextView tvDesc = (TextView) rl.findViewById(R.id.popcornupdaterowdesc);
		String name = popcorn.getName();
		if(name == null) name = "";
		tvDesc.setText(name);

		TextView tvPrice = (TextView) rl.findViewById(R.id.popcornupdaterowprice);
		BigDecimal cost = popcorn.getCost();
		if(cost == null) cost = new BigDecimal("0.00");
		tvPrice.setText("" + cost.setScale(2, BigDecimal.ROUND_HALF_UP));
		
		final String nameFinal = name;
        tvDesc.setLongClickable(true);
        tvDesc.setOnLongClickListener(new OnLongClickListener()
		{
			public boolean onLongClick(View v)
			{
				final View vv = v;
				
		        new AlertDialog.Builder(v.getContext())
		        .setIcon(android.R.drawable.ic_dialog_alert)
		        .setTitle("Delete Item?")
		        .setMessage("Do you want to delete\n\n" + nameFinal + "?")
		        .setPositiveButton("Delete", new DialogInterface.OnClickListener() 
		        {
		            public void onClick(DialogInterface dialog, int which)
		            {	// they have clicked on the description so remove this popcorn
		            	popcornDao.remove(nameFinal, vv.getContext());
		            	
		            	drawScreen();
		            }
		        })
		        .setNegativeButton("cancel", null)
		        .show();
				return true;
			}
		});
		
		ll.addView(rl);
	}

	void makeSaveButtonListener()
	{
		Button saveButton = (Button)findViewById(R.id.savepopcornlist);
        saveButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
        		PopcornDao popcornDao = new PopcornDao();
                ArrayList<Popcorn> list = new ArrayList<Popcorn>();
                LinearLayout ll = (LinearLayout) findViewById(R.id.updatePopcornListList);
                int x = ll.getChildCount();
                
                for (int i = 0; i < x; i++)
				{
                	RelativeLayout rl = (RelativeLayout) ll.getChildAt(i);
					EditText descView = (EditText)rl.getChildAt(0);
					String name = descView.getText().toString();
					if(name == null || "".equals(name))continue;
					
					EditText priceView = (EditText)rl.getChildAt(1);
					String price = priceView.getText().toString();
					if(price == null || "".equals(price)) continue;
					
					Popcorn popcorn = new Popcorn();
					popcorn.setName(name);
					popcorn.setCost(new BigDecimal(price));
					list.add(popcorn);
				}
                
                popcornDao.writeFile(list, v.getContext());
                
        		ll.removeAllViews();	// remove all views
                
                for (Iterator<Popcorn> iterator = list.iterator(); iterator.hasNext();)
        		{
        			Popcorn popcorn = (Popcorn) iterator.next();
        			populateRow(ll, popcorn);
        		}
                populateRow(ll, new Popcorn());
                
                mergePopcornList(v, popcornDao);
            }

			private void mergePopcornList(View v, PopcornDao popcornDao)
			{
				ArrayList<PopcornSold> gsList = PopcornCounterActivity.readGsFilex(v.getContext());
                ArrayList<Popcorn> popcornList = popcornDao.readFile(v.getContext());
        		
                if(gsList.size() < 1 || popcornList.size() < 1) return;
                
                PopcornSold aGirlScout;
                ArrayList<Popcorn> listOfGirlsPopcorn;
                Popcorn gsPopcorn;
                Popcorn popcornListPopcorn;
                
                for (Iterator<PopcornSold> iterator = gsList.iterator(); iterator.hasNext();)
                {
					aGirlScout = iterator.next();
					listOfGirlsPopcorn = aGirlScout.getPopcornSoldList();
					
					for (Iterator<Popcorn> iter2 = listOfGirlsPopcorn.iterator(); iter2.hasNext();)
					{
						gsPopcorn = iter2.next();
					
						for (Iterator<Popcorn> iter3 = popcornList.iterator(); iter3.hasNext();) 
						{
							popcornListPopcorn = iter3.next();
							
							if(gsPopcorn.getName().equals(popcornListPopcorn.getName()))
							{
								if(!gsPopcorn.getCost().equals(popcornListPopcorn.getCost()))
								{
									gsPopcorn.setCost(popcornListPopcorn.getCost());
								}
							}
						}
					}
					
					for (Iterator<Popcorn> iter3 = popcornList.iterator(); iter3.hasNext();) 
					{
						popcornListPopcorn = iter3.next();
						boolean found = false;
						
						for (Iterator<Popcorn> iter2 = listOfGirlsPopcorn.iterator(); iter2.hasNext();)
						{
							gsPopcorn = iter2.next();
							
							if(gsPopcorn.getName().equals(popcornListPopcorn.getName()))
							{
								found = true;
								break;
							}
						}
						
						if(!found)
						{
							Popcorn newPopcorn = new Popcorn(popcornListPopcorn.getName(),
														  popcornListPopcorn.getCost(),
														  popcornListPopcorn.getQuantity()); 
							listOfGirlsPopcorn.add(newPopcorn);
						}
					}
				}

                PopcornCounterActivity.saveGsFilex(v.getContext(), gsList);
        		popcornDao.writeFile(popcornList, v.getContext());
			}
        });
	}

	private void makeExitButtonListener()
	{
		Button exitButton = (Button)findViewById(R.id.exitPopcornList);
        exitButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
		    	Intent intent = new Intent();
                setResult(RESULT_OK, intent);
                finish();
            }
        });
	}
}
