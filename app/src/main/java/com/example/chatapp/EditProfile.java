package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

public class EditProfile extends AppCompatActivity
{
	String uid;
	EditText username;

	FirebaseDatabase db = FirebaseDatabase.getInstance("https://chat-app-madlab-default-rtdb.asia-southeast1.firebasedatabase.app/");
	DatabaseReference users = db.getReference().child("Users");

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_edit_profile);

		uid = getIntent().getExtras().getString("uid");
		username = findViewById(R.id.username);
	}

	public void saveFunc( View v )
	{
		String name = username.getText().toString();

		if( name.equals("") )
			showToast("Username cannot be empty");

		else if( !validateUsername(name) )
			showToast("Invalid username! Only alphanumeric characters allowed.");

		else
		{
			users.addListenerForSingleValueEvent(new ValueEventListener()
			{
				@Override
				public void onDataChange( @NonNull DataSnapshot dataSnapshot )
				{
					for( DataSnapshot dsp : dataSnapshot.getChildren() )
					{
						if( dsp.getValue(Register.User.class).uid.equals(uid) )
						{
							HashMap map = new HashMap();
							map.put("name", name);
							dsp.getRef().updateChildren(map);
						}
					}

					Intent i = new Intent(EditProfile.this, Success.class);
					i.putExtra("uid", uid);
					i.putExtra("username", name);
					startActivity(i);
				}

				@Override
				public void onCancelled( @NonNull DatabaseError error )
				{ Log.e("onCreate", "SingleValueEvent cancelled."); }
			});
		}
	}

	public void showToast( String errMsg )
	{
		Toast.makeText(this, errMsg, Toast.LENGTH_SHORT).show();
	}

	public boolean validateUsername( String username )
	{
		return username.matches("^[A-Za-z0-9]{1,10}$");
	}
}