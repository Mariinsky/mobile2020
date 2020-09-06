package com.example.threadingnetwork

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers.io
import io.reactivex.rxjava3.subjects.PublishSubject
import kotlinx.android.synthetic.main.fragment_user_details.view.*

class UserDetailsFragment : Fragment() {

    private lateinit var usermodel: UserViewModel
    private val constactsSubject: PublishSubject<String> = PublishSubject.create()
    private val compositeDisposable = CompositeDisposable()

    companion object {
        private lateinit var user: User
        fun newInstance(user: User) : UserDetailsFragment {
            this.user = user
            return UserDetailsFragment()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        usermodel = ViewModelProvider(this).get(UserViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_user_details, container, false)
        view.user_details.text = user.toString()
        usermodel.updateUserContact.onNext(user.uid)

        usermodel.newContactSubject
            .subscribe {
                usermodel.updateUserContact.onNext(user.uid)
            }.addTo(compositeDisposable)

        usermodel
            .userContactsSubject
            .observeOn(io())
            .subscribe {
               var contacts = ""
                view.contacts.text = contacts
                it.contacts?.forEach { contact ->
                    contacts += "${contact}\n"
                }
                constactsSubject.onNext(contacts)
            }.addTo(compositeDisposable)

        constactsSubject
            .subscribe {
                view.contacts.text = it
            }.addTo(compositeDisposable)

        view.add_contact_button.setOnClickListener {
            val contact = ContactInfo(user.uid, view.contact_name.text.toString(), view.contact_value.text.toString())
            usermodel.createContactSubject.onNext(contact)
            view.contact_name.text.clear()
            view.contact_value.text.clear()
        }

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        compositeDisposable.clear()
    }
}

