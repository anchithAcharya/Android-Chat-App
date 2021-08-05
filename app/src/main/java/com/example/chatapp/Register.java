package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class Register extends AppCompatActivity
{
	EditText email, username, password, password2;
	final FirebaseAuth mAuth = FirebaseAuth.getInstance();

	FirebaseDatabase db = FirebaseDatabase.getInstance("https://chat-app-madlab-default-rtdb.asia-southeast1.firebasedatabase.app/");
	DatabaseReference users = db.getReference().child("Users");

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_register);

		email = findViewById(R.id.email);
		username = findViewById(R.id.username);
		password = findViewById(R.id.password);
		password2 = findViewById(R.id.password2);
	}

	static class User
	{
		public String uid, email, name;

		public User() { uid = null; email = null; name = null; }

		public User( String uid, String email, String name )
		{
			this.uid = uid;
			this.email = email;
			this.name = name;
		}
	}

	public void registerFunc( View v )
	{
		String mail = email.getText().toString();
		String pass = password.getText().toString();
		String name = username.getText().toString();

		if( mail.equals("") )
			showToast("Email address cannot be empty");

		else if( !validateEmail(mail) )
			showToast("Invalid email address!");

		else if( name.equals("") )
			showToast("Username cannot be empty");

		else if( !validateUsername(name) )
			showToast("Invalid username! Only alphanumeric characters allowed.");

		else if( pass.equals("") )
			showToast("Please enter a password");

		else if( !validatePassword(pass) )
		{
			showToast("Password is too weak.");
			password.setText("");
			password2.setText("");
		}

		else if( !pass.equals(password2.getText().toString()) )
		{
			showToast("Make sure you enter the same password in both fields!");
			password2.setText("");
		}

		else
		{
			users.addListenerForSingleValueEvent( new ValueEventListener()
			{
				@Override
				public void onDataChange( @NonNull DataSnapshot dataSnapshot )
				{
					for( DataSnapshot dsp : dataSnapshot.getChildren() )
						if( dsp.getValue(User.class).name.equals(name) )
						{
							showToast("Username already taken! Please try another one.");
							username.setText("");
							return;
						}

						else if( dsp.getValue(User.class).email != null && dsp.getValue(User.class).email.equals(mail) )
						{
							showToast("There is already an account registered under this email ID!");
							email.setText("");
							return;
						}

					firebaseLogin(username.getText().toString(), mail, pass);
				}

				@Override
				public void onCancelled( @NonNull DatabaseError error )
				{ Log.e("onCreate", "SingleValueEvent cancelled."); }
			});
		}
	}

	public void firebaseLogin( String name, String mail, String pass )
	{
		mAuth.createUserWithEmailAndPassword(mail, pass).addOnCompleteListener(this, task -> {
			if( task.isSuccessful() )
			{
				Log.d("registerFunc", "createUserWithEmail:success");
				FirebaseUser user = mAuth.getCurrentUser();

				User u = new User(user.getUid(), mail, name);
				users.push().setValue(u);

				FirebaseAuth.getInstance().signOut();

				showToast("Registration successful!");

				Intent i = new Intent(Register.this, Login.class);
				startActivity(i);
			}

			else
			{
				Log.w("registerFunc", "createUserWithEmail:failure", task.getException());
				showToast("Authentication Failed!");
			}
		});
	}

	public void showToast( String errMsg )
	{
		Toast.makeText(Register.this, errMsg, Toast.LENGTH_SHORT).show();
	}

	public void launchLogin( View v )
	{
		Intent i = new Intent(this, Login.class);
		startActivity(i);
	}

	public boolean validateEmail( String email )
	{
		return email.matches("^[A-Za-z0-9]+@[A-Za-z0-9]+\\.[A-Za-z0-9.]+$");
	}

	public boolean validatePassword( String password )
	{
		return password.matches("^(?=.*[A-Z])(?=.*[a-z])(?=.*[0-9])(?=.*[<>/!@#$%^&])(?=\\S+$).{8,32}$");
	}

	public boolean validateUsername( String username )
	{
		return username.matches("^[A-Za-z0-9]{1,10}$");
	}
}
