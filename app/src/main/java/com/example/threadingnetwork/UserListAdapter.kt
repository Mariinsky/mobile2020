package com.example.threadingnetwork

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.user_list_row.view.*


class UserListAdapter(private val userClickSubject: PublishSubject<User>, private val users: MutableList<User>) :
    RecyclerView.Adapter<UserListAdapter.UserListHolder>() {

    class UserListHolder(val v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListHolder {

        val textView = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_list_row, parent, false) as View
        return  UserListHolder(textView)
    }

    override fun onBindViewHolder(holder: UserListHolder, position: Int) {
        val user = users[position]
        holder.v.user_name.text = "${user.firstname} ${user.lastname}"
        holder.v.setOnClickListener {
            userClickSubject.onNext(user)
        }
    }

    override fun getItemCount() = users.size
}