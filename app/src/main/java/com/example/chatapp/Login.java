package com.example.chatapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class Login extends AppCompatActivity
{
	EditText email, password;
	private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

	FirebaseDatabase db = FirebaseDatabase.getInstance("https://chat-app-madlab-default-rtdb.asia-southeast1.firebasedatabase.app/");
	DatabaseReference users = db.getReference().child("Users");

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		if( mAuth.getCurrentUser() != null )
		{
			final String[] info = new String[2];

			users.addListenerForSingleValueEvent( new ValueEventListener()
			{
				@Override
				public void onDataChange( @NonNull DataSnapshot dataSnapshot )
				{
					for( DataSnapshot dsp : dataSnapshot.getChildren() )
					{
						Register.User u = dsp.getValue(Register.User.class);
						if( u.uid.equals(mAuth.getCurrentUser().getUid()) )
						{
							info[0] = u.name;
							info[1] = u.uid;
							launchActivity(Success.class, info[0], info[1]);
							return;
						}
					}
				}

				@Override
				public void onCancelled( @NonNull DatabaseError error )
				{ Log.e("onCreate", "SingleValueEvent cancelled."); }
			});
		}

		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);

		email = findViewById(R.id.email);
		password = findViewById(R.id.password);
	}

	public void loginFunc( View v )
	{
		final String[] emailText = { email.getText().toString() };
		final String[] username = new String[1];
		String passText = password.getText().toString();

		if( emailText[0].equals("") )
		{
			Toast.makeText(this, "Email cannot be empty", Toast.LENGTH_SHORT).show();
			return;
		}

		else if( passText.equals("") )
		{
			Toast.makeText(this, "Password cannot be empty", Toast.LENGTH_SHORT).show();
			return;
		}

		users.addListenerForSingleValueEvent( new ValueEventListener()
		{
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot )
			{
				for( DataSnapshot dsp : dataSnapshot.getChildren() )
				{
					Register.User u = dsp.getValue(Register.User.class);
					if( u.name.equals(emailText[0]) )
					{
						username[0] = emailText[0];
						emailText[0] = u.email;

						break;
					}

					else if( u.email != null && u.email.equals(emailText[0]) )
					{
						username[0] = u.name;
						break;
					}
				}

				firebaseLogin(username[0], emailText[0], passText);
			}

			@Override
			public void onCancelled( @NonNull DatabaseError error )
			{ Log.e("onCreate", "SingleValueEvent cancelled."); }
		});
	}

	public void firebaseLogin( String username, String email, String password )
	{
		mAuth.signInWithEmailAndPassword(email, password)
				.addOnCompleteListener(this, task -> {
					if (task.isSuccessful())
					{
						Log.d("loginFunc", "signInWithEmail:success");
						FirebaseUser user = mAuth.getCurrentUser();

						assert user != null;
						Toast.makeText(Login.this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();

						launchActivity(Success.class, username, user.getUid());
					}

					else
					{
						Log.w("loginFunc", "signInWithEmail:failure", task.getException());
						Toast.makeText(Login.this, "Authentication failed.", Toast.LENGTH_SHORT).show();
					}
				});
	}

	public void launchActivity( Class<? extends AppCompatActivity> Activity, String username, String uid )
	{
		Intent i = new Intent(this, Activity);
		i.putExtra("username", username);
		i.putExtra("uid", uid);
		startActivity(i);
	}

	public void launchRegister( View v )
	{
		Intent i = new Intent(this, Register.class);;
		startActivity(i);
	}
}