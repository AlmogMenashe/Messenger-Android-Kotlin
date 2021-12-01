package com.example.applicationkotlinmessenger.messages

import android.annotation.SuppressLint
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.applicationkotlinmessenger.R
import com.example.applicationkotlinmessenger.models.User
import com.example.applicationkotlinmessenger.models.ChatMessage
import com.example.applicationkotlinmessenger.registerlogin.RegisterActivity
import com.example.applicationkotlinmessenger.views.LatestMessageRow
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_latest_messages.*

class LatestMessagesActivity : AppCompatActivity() {

    companion object{
        var currentUser: User? = null
        val TAG = "LatestMessages"
    }


    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_latest_messages)
        recyclerview_latest_messages.adapter = adapter
        recyclerview_latest_messages.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        supportActionBar?.setIcon(getDrawable(R.drawable.ic_launcher_foreground))

        //set item click listener on your adapter
        adapter.setOnItemClickListener { item, view ->
            Log.d(TAG, "123")
            val intent = Intent(this, ChetLogActivity::class.java)

            //we are missing the chat partner user

            val row = item as LatestMessageRow //casting

            intent.putExtra(NewMassageActivity.USER_KEY, row.chatPartnerUser)
            startActivity(intent)
        }

        //setupDummyRows()
        listenForLatestMessages()
        fetchCurrentUser()
        verifyUserIsLoggedIn()

    }

    val latestMessagesMap = HashMap<String,ChatMessage>()

    private fun refreshRecyclerViewMessage(){
        adapter.clear()
        latestMessagesMap.values.forEach{
            adapter.add(LatestMessageRow(it))
        }
    }

    private fun listenForLatestMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/lates-messages/$fromId")
        ref.addChildEventListener(object : ChildEventListener{
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?:return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java) ?:return
                latestMessagesMap[p0.key!!] = chatMessage
                refreshRecyclerViewMessage()
            }
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {

            }
            override fun onChildRemoved(p0: DataSnapshot) {

            }
            override fun onCancelled(p0: DatabaseError) {
            }
        })
    }


    val adapter = GroupAdapter<GroupieViewHolder>()

//    private fun setupDummyRows(){
//        //val adapter = GroupAdapter<GroupieViewHolder>()
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        adapter.add(LatestMessagesRow())
//        //recyclerview_latest_messages.adapter = adapter
//    }

    private fun fetchCurrentUser(){
        val uid = FirebaseAuth.getInstance().uid
        val ref = FirebaseDatabase.getInstance().getReference("/users/$uid")
        ref.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                currentUser = p0.getValue(User::class.java)
                Log.d("LatestMessages", "Current user ${currentUser?.profileImageUrl}")
            }
            override fun onCancelled(p0: DatabaseError) {

            }
        })
        supportActionBar?.title = "Hello " + currentUser?.username

    }


    private fun verifyUserIsLoggedIn(){
        val uid = FirebaseAuth.getInstance().uid
        if(uid == null) {
            val intent = Intent(this, RegisterActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(intent)
        }
    }

   override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item?.itemId){
            R.id.menu_new_message -> {
                val intent = Intent(this, NewMassageActivity::class.java)
                startActivity(intent)
            }
            R.id.menu_sing_out -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this, RegisterActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK.or(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(intent)
            }
        }

        return super.onOptionsItemSelected(item)
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.nav_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }
}