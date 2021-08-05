package com.example.chatapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class Success extends AppCompatActivity
{
	EditText messageEdit;
	LinearLayout messageScreen;
	String uid, username;

	LayoutInflater inflater;

	FirebaseDatabase db = FirebaseDatabase.getInstance("https://chat-app-madlab-default-rtdb.asia-southeast1.firebasedatabase.app/");
	DatabaseReference groupChat = db.getReference().child("groupChat");
	DatabaseReference users = db.getReference().child("Users");

	@Override
	protected void onCreate( Bundle savedInstanceState )
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_success);

		messageEdit = findViewById(R.id.messageEdit);
		messageScreen = findViewById(R.id.messageScreen);
		inflater = LayoutInflater.from(this);


		uid = getIntent().getExtras().getString("uid");
		username = getIntent().getExtras().getString("username");

		Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
		toolbar.setTitle(username);
		setSupportActionBar(toolbar);


		ArrayList<Message> messageList = new ArrayList();

		groupChat.addChildEventListener(new ChildEventListener()
		{
			@Override
			public void onChildAdded( @NonNull DataSnapshot snapshot, @Nullable String previousChildName )
			{ Message temp = snapshot.getValue(Message.class);
				pushMessage(temp); }

			@Override
			public void onChildChanged( @NonNull DataSnapshot snapshot, @Nullable String previousChildName ) {}

			@Override
			public void onChildRemoved( @NonNull DataSnapshot snapshot ) {}

			@Override
			public void onChildMoved( @NonNull DataSnapshot snapshot, @Nullable String previousChildName ) {}

			@Override
			public void onCancelled( @NonNull DatabaseError error ) {}
		});


		groupChat.addListenerForSingleValueEvent(new ValueEventListener()
		{
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot )
			{
				for( DataSnapshot dsp : dataSnapshot.getChildren() )
					messageList.add(dsp.getValue(Message.class));
			}

			@Override
			public void onCancelled( @NonNull DatabaseError error )
			{ Log.e("onCreate", "SingleValueEvent cancelled."); }
		});

		for( Message message: messageList )
			pushMessage(message);
	}


	@Override
	public boolean onCreateOptionsMenu( Menu menu ) {
		getMenuInflater().inflate(R.menu.action_bar_menu,menu);
		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected( MenuItem item) {
		switch (item.getItemId()) {
			case R.id.logout:
				logoutFunc();
				return true;

			case R.id.editprofile:
				Intent i = new Intent(this, EditProfile.class);
				i.putExtra("uid", uid);
				startActivity(i);
				return true;

			case R.id.deleteaccount:
				deleteAccount();
				return true;

			default:
				return super.onOptionsItemSelected(item);

		}
	}


	static class Message
	{
		public String sender, content;

		public Message() { sender = null; content = null; }

		public Message( String sender, String content )
		{
			this.sender = sender;
			this.content = content;
		}
	}


	private void pushMessage( Message m )
	{
		int layout;

		if( uid.equals(m.sender) ) layout = R.layout.message_self;
		else layout = R.layout.message_general;

		LinearLayout toPush = (LinearLayout) inflater.inflate(layout, messageScreen, false);

		users.addListenerForSingleValueEvent( new ValueEventListener()
		{
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot )
			{
				for( DataSnapshot dsp : dataSnapshot.getChildren() )
				{
					Register.User u = dsp.getValue(Register.User.class);
					if( u.uid.equals(m.sender) )
					{
						TextView temp = (TextView) toPush.getChildAt(0);
						temp.setText(u.name);

						temp = (TextView) toPush.getChildAt(1);
						temp.setText(m.content);

						messageScreen.addView(toPush);

						ScrollView scrollView = (ScrollView) messageScreen.getParent();

						scrollView.postDelayed(() -> scrollView.fullScroll(ScrollView.FOCUS_DOWN), 100);
						break;
					}
				}
			}

			@Override
			public void onCancelled( @NonNull DatabaseError error )
			{ Log.e("onCreate", "SingleValueEvent cancelled."); }
		});
	}

	public void sendMessageFunc( View v )
	{
		String content = messageEdit.getText().toString();

		if( content.equals("") ) return;

		Message m = new Message(uid, content);
		messageEdit.setText("");

		groupChat.push().setValue(m);
	}

	public void logoutFunc()
	{
		FirebaseAuth.getInstance().signOut();

		Intent i = new Intent(this, Login.class);
		startActivity(i);
	}

	public void deleteAccount()
	{
		FirebaseAuth.getInstance().getCurrentUser().delete();

		users.addListenerForSingleValueEvent( new ValueEventListener()
		{
			@Override
			public void onDataChange( @NonNull DataSnapshot dataSnapshot )
			{
				for( DataSnapshot dsp : dataSnapshot.getChildren() )
				{
					if( dsp.getValue(Register.User.class).uid.equals(uid) )
					{
						HashMap map = new HashMap();
						map.put("email", null);
						map.put("name", "[deleted user]");
						dsp.getRef().updateChildren(map);
						break;
					}
				}

				logoutFunc();
			}

			@Override
			public void onCancelled( @NonNull DatabaseError error )
			{ Log.e("onCreate", "SingleValueEvent cancelled."); }
		});
	}
}