package com.example.homer.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.homer.AdapterClasses.UserAdapter
import com.example.homer.ModelClasses.ChatList
import com.example.homer.ModelClasses.Users
import com.example.homer.R


class ChatsFragment : Fragment() {

    private  var userAdapter: UserAdapter? = null
    private  var mUsers: List<Users>? = null
    private  var usersChatList: List<ChatList>? = null


    
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    private fun retrieveChatList(){

    }
}