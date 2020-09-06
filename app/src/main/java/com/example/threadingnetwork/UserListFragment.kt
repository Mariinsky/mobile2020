package com.example.threadingnetwork

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import kotlinx.android.synthetic.main.fragment_user_list.*
import kotlinx.android.synthetic.main.fragment_user_list.view.*
import kotlinx.android.synthetic.main.user_list_row.view.*

class UserListFragment : Fragment() {
    private val compositeDisposable = CompositeDisposable()
    private var users: MutableList<User> = mutableListOf()
    private lateinit var usermodel: UserViewModel
    private lateinit var adapter: UserListAdapter
    private lateinit var listener : OnUserSelected

    companion object {
        fun newInstance(): UserListFragment {
            Log.i("XXX", "new instance list")
            return UserListFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is OnUserSelected) {
            listener = context
        } else {
            throw ClassCastException(context.toString() + " must implement OnDogSelected.")
        }
        adapter = UserListAdapter()
        usermodel = ViewModelProvider(this).get(UserViewModel::class.java)
        usermodel.usersSubject
            .subscribe {
                users.clear()
                it.forEach {
                    users.add(it)
                }
                adapter.notifyDataSetChanged()
            }.addTo(compositeDisposable)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

        val activity = activity as Context

        view.user_list.layoutManager = LinearLayoutManager(activity)
        view.user_list.adapter = adapter

        view.add_user_action.setOnClickListener {
            add_user_view.visibility = View.VISIBLE
        }

        view.user_add_confirm.setOnClickListener {
            val newUser = User(0, view.edit_text_firstname.text.toString(), view.edit_text_lastname.text.toString() )
            usermodel.createUserSubject.onNext(newUser)
            add_user_view.visibility = View.GONE
        }
        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }


    internal inner class UserListAdapter() : RecyclerView.Adapter<UserListAdapter.UserListViewHolder>() {

        inner class UserListViewHolder(val v: View) : RecyclerView.ViewHolder(v)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserListViewHolder {
            return UserListViewHolder(
                LayoutInflater.from(context).inflate(R.layout.user_list_row, parent, false)
            )
        }
        override fun onBindViewHolder(holder: UserListViewHolder, position: Int) {
            val view = holder.v
            view.user_name.text = users[position].toString()
            view.setOnClickListener {
                listener.onUserSelect(users[position])
            }
        }

        override fun getItemCount() = users.size
    }

    interface OnUserSelected {
        fun onUserSelect(user: User)
    }

}