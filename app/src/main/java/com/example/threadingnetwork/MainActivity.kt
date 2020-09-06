package com.example.threadingnetwork

import android.annotation.SuppressLint
import android.app.Application
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.reactivex.rxjava3.kotlin.Flowables.create
import io.reactivex.rxjava3.subjects.PublishSubject

import io.reactivex.rxjava3.*
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.annotations.SchedulerSupport.IO
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.schedulers.Schedulers.io
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*


class MainActivity : AppCompatActivity() {

    private val db by lazy { UserDB.get(this) }
    private var users: MutableList<User> = mutableListOf()
    private lateinit var recyclerView: RecyclerView
    private lateinit var viewAdapter: RecyclerView.Adapter<*>
    private lateinit var viewManager: RecyclerView.LayoutManager
    private var userListUserClicked: PublishSubject<User> = PublishSubject.create()
    private val fragmentManager = supportFragmentManager

    @RequiresApi(Build.VERSION_CODES.N)
    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val viewModel = ViewModelProvider(this).get(UserModel::class.java)
        viewManager = LinearLayoutManager(this)
        viewAdapter = UserListAdapter(userListUserClicked, users)
        user_list.adapter = viewAdapter
        user_list.layoutManager = viewManager
        recyclerView = user_list

        viewModel.userContactsSubject
            .subscribe {
                if (it != null) {
                    it.contacts?.forEach {
                        Log.i("XXX", it.value.toString())
                    }
                }
            }

        viewModel.usersSubject
            .subscribe {
                Log.i("XXX", it.size.toString())
                it.forEach {
                    users.add(it)
                }
               viewAdapter.notifyDataSetChanged()
            }

        userListUserClicked
            .subscribe {
                val fragment = UserDetailsFragment()
                val transaction = fragmentManager.beginTransaction()
                
                transaction.commit()
            }

        add_user_button.setOnClickListener {
                this.viewAdapter.notifyDataSetChanged()
            }
    }

    fun insertDB() {
        GlobalScope.launch {
            val user = db.userDao().insert(User(0, "kala", "pelle"))
            db.contactDao().insert(ContactInfo(user, "puh", "215sfafsa136616"))
        }
    }
}



class UserModel(application: Application) : AndroidViewModel(application) {
    private val updateUserContact: PublishSubject<Long> = PublishSubject.create()
    val updateUsers: PublishSubject<Unit> = PublishSubject.create()
    private val disposeBag = CompositeDisposable()
    val userContactsSubject: PublishSubject<UserContact> = PublishSubject.create()
    val usersSubject: PublishSubject<List<User>> = PublishSubject.create()

    init {

        updateUserContact
            .observeOn(io())
            .subscribe {
                val contacts = UserDB.get(getApplication()).userDao().getUserContacts(it)
                userContactsSubject.onNext(contacts)
            }.addTo(disposeBag)

        updateUsers
            .observeOn(io())
            .subscribe {
                val users = UserDB.get(getApplication()).userDao().getAll()
                usersSubject.onNext(users)
            }.addTo(disposeBag)

        updateUsers.onNext(Unit)
    }
}

