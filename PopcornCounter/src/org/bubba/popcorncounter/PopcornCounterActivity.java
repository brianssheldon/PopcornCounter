package org.bubba.popcorncounter;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
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
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class PopcornCounterActivity extends Activity {
	public static final String GS_SALES_FILE = "popcornalesfile";
	ArrayList<PopcornSold> arrayList;
	PopcornDao popcornDao = new PopcornDao();
	boolean firstTime = false;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		readGsFile();

		LinearLayout mainLL = (LinearLayout) findViewById(R.id.mylayout1);

		Button addPersonButton = (Button) findViewById(R.id.addPersonButton);
		addPersonButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent myIntentx = new Intent(v.getContext(),
						ContactActivity.class);
				startActivityForResult(myIntentx, 100);
			}
		});

		Button sendEmailButton = (Button) findViewById(R.id.emailButton);
		sendEmailButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				Intent emailIntent = new Intent(
						android.content.Intent.ACTION_SEND);
				emailIntent.setType("plain/text");

				emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT,
						"Popcorn List as of " + (new Date()).toString());
				emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
						"List of Popcorn\n" + getPopcornListEmailString());
				startActivity(emailIntent);
			}
		});

		populateScrollView(mainLL);
		showInitialPopup(this);
	}

	protected String getPopcornListEmailString() {
		StringBuffer sb = new StringBuffer(100);
		readGsFile();
		BigDecimal grandTotal = new BigDecimal("0.00").setScale(2);

		for (Iterator<PopcornSold> iter = arrayList.iterator(); iter.hasNext();) {
			PopcornSold gscs = (PopcornSold) iter.next();

			BigDecimal personTotal = new BigDecimal("0.00").setScale(2);
			sb.append(gscs.getName() + "\n");

			int namelen = 20;
			int quantitylen = 5;
			int costlen = 8;
			int saletotallen = 8;

			for (Iterator<Popcorn> iter2 = gscs.getPopcornSoldList().iterator(); iter2
					.hasNext();) {
				Popcorn gsc = iter2.next();
				namelen = PopcornUtil.whichIsLarger(gsc.getName().length(),
						namelen);
				quantitylen = PopcornUtil.whichIsLarger(gsc.getQuantity(),
						quantitylen);
				costlen = PopcornUtil.whichIsLarger(("" + gsc.getCost())
						.toString().length(), costlen);
				saletotallen = PopcornUtil.whichIsLarger(gsc.getTotal()
						.toString().length(), namelen);
			}

			for (Iterator<Popcorn> iter2 = gscs.getPopcornSoldList().iterator(); iter2
					.hasNext();) {
				Popcorn gsc = iter2.next();
				sb.append(gsc.toStringBuffer(namelen, quantitylen, costlen,
						saletotallen));
				BigDecimal saleTotal = gsc.getTotal();
				personTotal = personTotal.add(saleTotal);
			}
			sb.append("    total = " + personTotal.toString());
			sb.append("\n-------------\n");
			grandTotal = grandTotal.add(personTotal);
		}

		sb.append("grand total = " + grandTotal.toString());

		sb.append(PopcornUtil.getPopcornTotalsForEmail(arrayList));

		return sb.toString();
	}

	void populateScrollView(LinearLayout mainLL)
	{
		LayoutInflater linflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		int i = -1;

		while (mainLL.getChildCount() > 0)
		{
			mainLL.removeViewAt(0);
		}

		readGsFile();
		
		for (Iterator<PopcornSold> iter = arrayList.iterator(); iter.hasNext();)
		{
			i = i + 1;
			PopcornSold gscs = (PopcornSold) iter.next();
			final PopcornSold gscsFinal = gscs;

			View customView = linflater.inflate(R.layout.person, null);
			
			TextView tv = (TextView) customView.findViewById(R.id.textView);
			tv.setId(i);
			tv.setText(gscs.getName());
			tv.setLongClickable(true);
			tv.setOnLongClickListener(new OnLongClickListener() {
				public boolean onLongClick(View v) {
					final View vv = v;
					String name = gscsFinal.getName();

					new AlertDialog.Builder(v.getContext())
							.setIcon(android.R.drawable.ic_dialog_alert)
							.setTitle("Delete Item?")
							.setMessage(
									"Do you want to delete\n\n" + name + "?")
							.setPositiveButton("Delete",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog,
												int which) { // they have
																// clicked on
																// the checkbox
																// so remove
																// this person
											arrayList.remove(vv.getId());
											saveGsFilex();
											populateScrollView((LinearLayout) findViewById(R.id.mylayout1));
										}
									}).setNegativeButton("cancel", null).show();
					return true;
				}
			});

			CheckBox cb = (CheckBox) customView.findViewById(R.id.checkBox);
			cb.setId(i);
			cb.setOnClickListener(new View.OnClickListener() {
				public void onClick(View v) {
					RelativeLayout rl = (RelativeLayout) v.getParent();

					if (rl.getChildCount() < 1
							|| !(rl.getChildAt(0) instanceof TextView)) {
						return;
					}

					TextView tv = (TextView) rl.getChildAt(0);

					Intent myIntent = new Intent(v.getContext(),
							PopcornUpdateActivity.class);
					myIntent.putExtra("name", tv.getText());
					myIntent.putExtra("id", tv.getId());

					startActivityForResult(myIntent, 100);
				}
			});
			
			mainLL.addView(customView);
			
			LayoutInflater x = (LayoutInflater)getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			TextView tvLine = (TextView) x.inflate(R.layout.thelineb, null);
			mainLL.addView(tvLine);
		}
	}

	void readGsFile() {
		try {
			FileInputStream fis = openFileInput(GS_SALES_FILE);
			ObjectInputStream in = new ObjectInputStream(fis);
			arrayList = (ArrayList<PopcornSold>) in.readObject();
		} catch (Exception e) {
			try {
				arrayList = new ArrayList<PopcornSold>();
				arrayList.add(new PopcornSold());

				FileOutputStream fos = openFileOutput(GS_SALES_FILE,
						Context.MODE_PRIVATE);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				out.writeObject(arrayList);
				firstTime = true;
			} catch (Exception e2) {
				e2.printStackTrace();
			}
		}
	}

	public static final ArrayList<PopcornSold> readGsFilex(Context context) {
		ArrayList<PopcornSold> arrayListx;
		try {
			FileInputStream fis = context.openFileInput(GS_SALES_FILE);
			ObjectInputStream in = new ObjectInputStream(fis);
			arrayListx = (ArrayList<PopcornSold>) in.readObject();
		} catch (Exception e) {
			try {
				arrayListx = new ArrayList<PopcornSold>();
				arrayListx.add(new PopcornSold());

				FileOutputStream fos = context.openFileOutput(GS_SALES_FILE,
						Context.MODE_PRIVATE);
				ObjectOutputStream out = new ObjectOutputStream(fos);
				out.writeObject(arrayListx);
			} catch (Exception e2) {
				e2.printStackTrace();
				arrayListx = new ArrayList<PopcornSold>();
			}
		}
		return arrayListx;
	}

	public static final void saveGsFilex(Context context,
			ArrayList<PopcornSold> newArray) {
		try {
			FileOutputStream fos = context.openFileOutput(GS_SALES_FILE,
					Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(newArray);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	public void saveGsFilex() {
		try {
			FileOutputStream fos = openFileOutput(GS_SALES_FILE,
					Context.MODE_PRIVATE);
			ObjectOutputStream out = new ObjectOutputStream(fos);
			out.writeObject(arrayList);
		} catch (Exception e2) {
			e2.printStackTrace();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode == 123) { // back from EditListActivity

			// add/remove popcorn from people
		} else if (requestCode == 144) {
			// back from ContactList
		}
		populateScrollView((LinearLayout) findViewById(R.id.mylayout1));
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) { // only called once -
													// creates the menu
		MenuInflater inflater = getMenuInflater();

		inflater.inflate(R.menu.mainmenu, menu);

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) { // called when they
															// have selected a
															// menu option

		int itemId = item.getItemId();

		switch (itemId) {
		case R.id.exit:
			this.finish(); // quit app. is this good or bad??
			return true;

		case R.id.editPopcornList: // go to screen to edit the list of popcorn
			Intent myIntent = new Intent(this, EditListActivity.class);
			startActivityForResult(myIntent, 123);
			return true;

//		case R.id.editContactList: // go to screen to edit the list of popcorn
//			Intent contactIntent = new Intent(this, ContactActivity.class);
//			startActivityForResult(contactIntent, 144);
//			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	private void showInitialPopup(final PopcornCounterActivity popcornCounterActivity)
	{
		if(!firstTime) return;

		new Handler().postDelayed(new Runnable() {
			public void run() {
				AlertDialog dialog = new AlertDialog.Builder(popcornCounterActivity)
						.setIcon(android.R.drawable.ic_dialog_alert)
						.setTitle("First Time")
						.setMessage(
							"\n\nPlease use the menu option\n\n   'edit popcorn list'\n\nto add sale items first\n\n")
						.setPositiveButton("Ok", null)
						//.setNegativeButton("Ok", null)
						.create();

				dialog.show();
			}
		}, 1000); // starting it in 1 second
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		// super.onRestoreInstanceState(savedInstanceState);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		// super.onSaveInstanceState(outState);
	}
}