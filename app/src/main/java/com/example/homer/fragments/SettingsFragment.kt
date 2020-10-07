package com.example.homer.fragments

import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.renderscript.Sampler
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.Toast
import com.example.homer.ModelClasses.Users
import com.example.homer.R
import com.example.homer.RegisterActivity
import com.example.homer.WelcomeActivity
import com.google.android.gms.tasks.Continuation
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.StorageTask
import com.google.firebase.storage.UploadTask
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.fragment_settings.view.*

class SettingsFragment : Fragment() {
    var usersReference: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null
    private val RequestCode = 438
    private var imageUri: Uri? = null
    private var storageRef: StorageReference? = null
    private var coverChecker: String? = ""
    private var socialChecker: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        firebaseUser = FirebaseAuth.getInstance().currentUser
        usersReference =
            FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)
        storageRef = FirebaseStorage.getInstance().reference.child("User Images")
        usersReference!!.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user: Users? = p0.getValue(Users::class.java)

                    if (context != null) {
                        view.username_settings.text = user!!.getUserName()
                        Picasso.get().load(user.getProfile()).into(view.profile_image_settings)
                        Picasso.get().load(user.getCover()).into(view.cover_image_settings)
                    }
                }
            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })

        view.profile_image_settings.setOnClickListener {
            pickImage()
        }

        view.cover_image_settings.setOnClickListener {
            coverChecker = "cover"
            pickImage()
        }
        view.set_facebook.setOnClickListener {
            socialChecker = "facebook"
            setSocialLinks()
        }
        view.set_instagram.setOnClickListener {
            socialChecker = "instagram"
            setSocialLinks()
        }
        view.set_webpage.setOnClickListener {
            socialChecker = "webpage"
            setSocialLinks()
        }
        view.logout.setOnClickListener {
           FirebaseAuth.getInstance().signOut()

            val myIntent = Intent(activity,WelcomeActivity::class.java)
            myIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK)
            startActivity(myIntent)


// japabeidz logout
        }

        return view
    }


    private fun setSocialLinks() {
        val builder: AlertDialog.Builder =
            AlertDialog.Builder(context!!,R.style.Theme_AppCompat_DayNight_Dialog_Alert)
        if (socialChecker == "website")
        {
            builder.setTitle("Write Url:")
        }
        else
        {
            builder.setTitle("Write Username")
        }

        val editText= EditText(context)

        if (socialChecker == "website")
        {
            editText.hint = "e.g www.google.lv"
        }
        else
        {
            editText.hint = "e.g Romeo12"
        }
        builder.setView(editText)
        builder.setPositiveButton("Create", DialogInterface.OnClickListener {
            dialog, witch ->
            val str = editText.text.toString()

            if (str == "")
            {
                Toast.makeText(context, " DOH !!! Please write something...", Toast.LENGTH_LONG).show()
            }
            else
            {
                saveSocialLink(str)
            }
        })
        builder.setNegativeButton("Cancel", DialogInterface.OnClickListener {
                dialog, witch ->
                dialog.cancel()
            })
          builder.show()
        }

    private fun saveSocialLink(str: String) {

        val mapSocial = HashMap<String, Any>()

        when(socialChecker)
        {
            "facebook" ->
            {
                mapSocial["facebook"] = "https://m.facebook.com/$str"
            }
            "instagram" ->
            {
                mapSocial["instagram"] = "https://m.instagram.com/$str"
            }
            "website" -> {
                mapSocial["instagram"] = "https://$str"
            }
        }
        usersReference!!.updateChildren(mapSocial).addOnCompleteListener {
            task ->
            if (task.isSuccessful)
            {
                Toast.makeText(context, " Now i know your social profile !", Toast.LENGTH_LONG).show()
            }
        }
    }


    private fun pickImage() {
        val intent = Intent()
        intent.type = "image/*"
        intent.action = Intent.ACTION_GET_CONTENT
        startActivityForResult(intent, RequestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RequestCode && resultCode == Activity.RESULT_OK && data!!.data != null) {
            imageUri = data!!.data
            Toast.makeText(context, "Uploading...", Toast.LENGTH_LONG).show()
            uploadImageToDatabase()
        }
    }

    private fun uploadImageToDatabase() {
        val progressBar = ProgressDialog(context)
        progressBar.setMessage("OH FINALY !!! , please wait...")
        progressBar.show()

        if (imageUri != null) {
            val fileRef = storageRef!!.child(System.currentTimeMillis().toString() + ".jpg")

            var uploadTask: StorageTask<*>
            uploadTask = fileRef.putFile(imageUri!!)
            uploadTask.continueWithTask(Continuation<UploadTask.TaskSnapshot, Task<Uri>> { task ->
                if (!task.isSuccessful) {

                    task.exception?.let {
                        throw it
                    }
                }
                return@Continuation fileRef.downloadUrl
            }).addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val downloadUrl = task.result
                    val url = downloadUrl.toString()

                    if (coverChecker == "cover") {
                        val mapCoverImage = HashMap<String, Any>()
                        mapCoverImage["cover"] = url
                        usersReference!!.updateChildren(mapCoverImage)
                        coverChecker = ""
                    } else {
                        val mapProfileImg = HashMap<String, Any>()
                        mapProfileImg["profile"] = url
                        usersReference!!.updateChildren(mapProfileImg)
                        coverChecker = ""
                    }
                    progressBar.dismiss()
                }
            }
        }
    }
}