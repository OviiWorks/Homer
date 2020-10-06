package com.example.homer

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.renderscript.Sampler
import android.renderscript.Script
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.homer.AdapterClasses.ChatAdapter
import com.example.homer.ModelClasses.Chat
import com.example.homer.ModelClasses.Users
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message_chat.*

class MessageChatActivity : AppCompatActivity()
{
        var userIdVisit: String = ""
        var firebaseUser: FirebaseUser? = null
        var chatAdapter: ChatAdapter? = null
        var mChatList: List<Chat>? = null
        lateinit var recycler_view_chats: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message_chat)


        intent = intent
        userIdVisit = intent.getStringExtra("visit_id").toString()
        firebaseUser = FirebaseAuth.getInstance().currentUser

        recycler_view_chats = findViewById(R.id.recycler_view_chats)
        recycler_view_chats.setHasFixedSize(true)
        var linearLayoutManager = LinearLayoutManager(applicationContext)
        linearLayoutManager.stackFromEnd
        recycler_view_chats.layoutManager = linearLayoutManager

        val reference = FirebaseDatabase.getInstance()
            .reference
            .child("Users")
            .child(userIdVisit)
        reference.addValueEventListener(object : ValueEventListener {

            override fun onDataChange(p0: DataSnapshot)
            {
                val user: Users? = p0.getValue(Users::class.java)
                username_mchat.text = user!!.getUserName()
                Picasso.get().load(user.getProfile()).into(profile_image_mchat)

                retrieveMessages(firebaseUser!!.uid,userIdVisit,user.getProfile())
            }

            override fun onCancelled(p0: DatabaseError)
            {

            }

        })


        send_message_button.setOnClickListener {
            val message = text_message.text.toString()
            if (message == "")
            {
                Toast.makeText(this@MessageChatActivity,"DOH !!! You didn't write anything... :(  " , Toast.LENGTH_LONG).show()
            }
            else
            {
                sendMessageToUser(firebaseUser!!.uid, userIdVisit, message)
            }
            text_message.setText("")
        }

        add_picture_button.setOnClickListener {
            val intent = Intent()
            intent.action = Intent.ACTION_GET_CONTENT
            intent.type = "image/*"
            startActivityForResult(Intent.createChooser(intent,"YES YES YES Show me something !"), 438)
        }
    }



    private fun sendMessageToUser(senderID: String, receiverID: String?, message: String)
    {
        val reference = FirebaseDatabase.getInstance().reference
        val messageKey = reference.push().key

        val messageHashMap = HashMap<String,Any?>()
        messageHashMap["sender"] = senderID
        messageHashMap["message"] = message
        messageHashMap["receiver"] = receiverID
        messageHashMap["isseen"] = false
        messageHashMap["url"] = ""
        messageHashMap["messageID"] = messageKey
        reference.child("Chats")
                 .child(messageKey!!)
                 .setValue(messageHashMap)
                 .addOnCompleteListener { task ->
                    if (task.isSuccessful)
                    {
                        val chatsListReference = FirebaseDatabase.getInstance()
                            .reference
                            .child("ChatList")
                            .child(firebaseUser!!.uid)
                            .child(userIdVisit)

                        chatsListReference.addListenerForSingleValueEvent(object : ValueEventListener{

                            override fun onDataChange(p0: DataSnapshot) {
                                if (!p0.exists())
                                {
                                    chatsListReference.child("id").setValue(userIdVisit)
                                }
                                val chatsListReceiverRef = FirebaseDatabase.getInstance()
                                    .reference
                                    .child("ChatList")
                                    .child(userIdVisit)
                                    .child(firebaseUser!!.uid)
                                chatsListReceiverRef.child("id").setValue(firebaseUser!!.uid)
                            }

                            override fun onCancelled(p0: DatabaseError)
                            {

                            }
                        })



                        val reference = FirebaseDatabase.getInstance()
                            .reference
                            .child("Users")
                            .child(firebaseUser!!.uid)
                        //implement the push notifications
                    }
                 }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode== 438 && resultCode==RESULT_OK && data!=null && data!!.data!=null)
        {
            val progressBar = ProgressDialog(this)
            progressBar.setMessage("UUU i almost see the picture...")
            progressBar.show()

            val fileUri = data.data
            val storageReference = FirebaseStorage.getInstance().reference.child("Chat Images")
            val ref = FirebaseDatabase.getInstance().reference
            val messageID = ref.push().key
            val filePath = storageReference.child("$messageID.jpg")

            var uploadTask: StorageTask<*>
            uploadTask = filePath.putFile(fileUri!!)
            uploadTask.continueWithTask(Continuation <UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {

                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation filePath.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful)
                {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()
                    val messageHashMap = HashMap<String,Any?>()
                    messageHashMap["sender"] = firebaseUser!!.uid
                    messageHashMap["message"] = "sent you an image."
                    messageHashMap["receiver"] = userIdVisit
                    messageHashMap["isseen"] = false
                    messageHashMap["url"] = url
                    messageHashMap["messageID"] = messageID

                    ref.child("Chats").child(messageID!!).setValue(messageHashMap)
                    progressBar.dismiss()

                }
            }
        }
    }
    private fun retrieveMessages(senderID: String, receiverID: String?, receiverImageUrl: String?)
    {
        mChatList = ArrayList()
        val reference = FirebaseDatabase.getInstance().reference.child("Chats")

        reference.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                (mChatList as ArrayList<Chat>).clear()
                for (snapshot in p0.children)
                {
                    val chat = snapshot.getValue(Chat::class.java)
                    if (chat!!.getReceiver().equals(senderID) && chat.getSender().equals(receiverID)
                        || chat.getReceiver().equals(receiverID) && chat.getSender().equals(senderID))
                    {
                        (mChatList as ArrayList<Chat>).add(chat)
                    }
                    chatAdapter = ChatAdapter(this@MessageChatActivity,(mChatList as ArrayList<Chat>),receiverImageUrl!!)
                    //show messages on recycle view
                    recycler_view_chats.adapter = chatAdapter
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })
    }
}
