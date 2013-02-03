package org.bubba.popcorncounter;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class AddPersonActivity extends Activity
{
	@Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.addperson);

        Button addPersonButton = (Button)findViewById(R.id.addpersonbutton);
        addPersonButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View v)
            {
        		ArrayList<PopcornSold> list = PopcornCounterActivity.readGsFilex(v.getContext());
        		TextView tv = (TextView) findViewById(R.id.addpersonedittext);
        		
        		if(list == null || tv.getText() == null || tv.getText().length() == 0)
        		{
        			return;
        		}
        		
        		PopcornSold gscs = new PopcornSold();
        		gscs.setName(tv.getText().toString());
        		PopcornDao popcornDao = new PopcornDao();
        		ArrayList<Popcorn> listOfPopcorn = popcornDao.readFile(v.getContext());
        		if(listOfPopcorn != null && listOfPopcorn.size() > 0)
        		{
        			gscs.setPopcornSoldList(listOfPopcorn);
        		}
        		
        		list.add(gscs);
        		tv.setText("");
        		
        		PopcornCounterActivity.saveGsFilex(v.getContext(), list);
            }
        });

        Button cancelPersonButton = (Button)findViewById(R.id.cancelpersonbutton);
        cancelPersonButton.setOnClickListener(new View.OnClickListener() 
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
