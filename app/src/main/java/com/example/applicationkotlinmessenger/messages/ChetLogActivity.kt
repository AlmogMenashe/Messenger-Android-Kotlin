package com.example.applicationkotlinmessenger.messages


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.AbsListView
import android.widget.Toolbar
import com.example.applicationkotlinmessenger.R
import com.example.applicationkotlinmessenger.models.ChatMessage
import com.example.applicationkotlinmessenger.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.ktx.Firebase
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chet_log.*
import kotlinx.android.synthetic.main.chet_from_row.view.*
import kotlinx.android.synthetic.main.chet_to_row.view.*
import kotlinx.android.synthetic.main.user_row_new_massage.view.*

class ChetLogActivity : AppCompatActivity() {

    companion object{
        val TAG = "ChatLog"
    }

    val adapter = GroupAdapter<GroupieViewHolder>()

    var toUser: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chet_log)

        recyclerview_chat_log.adapter = adapter

        toUser = intent.getParcelableExtra<User>(NewMassageActivity.USER_KEY)
        supportActionBar?.title = toUser?.username


        //setupDummyData()
        listenForMessages()

        send_butten_chat_log.setOnClickListener{
            Log.d(TAG,"Attempt to send message....")
            performSendMessage()
        }
    }

    private fun listenForMessages(){
        val fromId = FirebaseAuth.getInstance().uid
        val toId = toUser?.uid
        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage= p0.getValue(ChatMessage :: class.java)

                if (chatMessage != null){
                    Log.d(TAG, chatMessage.text)

                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        val currentUser = LatestMessagesActivity.currentUser ?: return
                        adapter.add(ChetFromItem(chatMessage.text, currentUser))
                    } else {
                       adapter.add(ChetToItem(chatMessage.text, toUser!! )) }
                        //toUser?.let { ChetToItem(chatMessage.text, it) }?.let { adapter.add(it) }


                }

                recyclerview_chat_log.scrollToPosition(adapter.itemCount -1)

            }

            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }


            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })

    }


    private fun performSendMessage(){

        // איך לשלוח הודעה תמיד לדאטהביס הודעה ולהציג אותה במסך
        val text = editText_chat_log.text.toString()

        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(NewMassageActivity.USER_KEY)
        val toId = user?.uid

        if(fromId == null) return
        if(toId == null) return

        //val reference = FirebaseDatabase.getInstance().getReference("/messages").push()
        val reference = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val toReference = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chetMessage = ChatMessage(reference.key!!, text,fromId,toId,System.currentTimeMillis()/1000 )

        reference.setValue(chetMessage)
            .addOnSuccessListener {
                Log.d(TAG, "Save our chat message: ${reference.key}")
                editText_chat_log.text.clear()
                recyclerview_chat_log.scrollToPosition(adapter.itemCount - 1)
            }
        toReference.setValue(chetMessage)

        val latestMessagesRef = FirebaseDatabase.getInstance().getReference("/lates-messages/$fromId/$toId")
        latestMessagesRef.setValue(chetMessage)
        val latestMessagesToRef = FirebaseDatabase.getInstance().getReference("/lates-messages/$toId/$fromId")
        latestMessagesToRef.setValue(chetMessage)


    }
//    private fun setupDummyData(){
//        val adapter = GroupAdapter<GroupieViewHolder>()
//        adapter.add(ChetFromItem("from mmm"))
//        adapter.add(ChetToItem("to aa"))
//        recyclerview_chat_log.adapter = adapter
//    }
}


class ChetFromItem(val text: String,val user: User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.textView_from_row.text = text
        //טוענים תמונה במסך הצאט
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chat_from_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chet_from_row
    }
}


class ChetToItem(val text: String,val user:User): Item<GroupieViewHolder>() {
    override fun bind(viewHolder: GroupieViewHolder, position: Int) {

        viewHolder.itemView.textView_to_row.text = text

        //טוענים תמונה במסך הצאט
        val uri = user.profileImageUrl
        val targetImageView = viewHolder.itemView.imageView_chat_to_row
        Picasso.get().load(uri).into(targetImageView)

    }

    override fun getLayout(): Int {
        return R.layout.chet_to_row
    }
}