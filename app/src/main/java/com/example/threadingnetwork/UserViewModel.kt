package com.example.threadingnetwork

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.kotlin.addTo
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.PublishSubject

class UserViewModel(application: Application) : AndroidViewModel(application) {
    private val db by lazy { UserDB.get(application) }
    val updateUserContact: PublishSubject<Long> = PublishSubject.create()
    val updateUsers: PublishSubject<Unit> = PublishSubject.create()
    val createContactSubject: PublishSubject<ContactInfo> = PublishSubject.create()
    private val disposeBag = CompositeDisposable()
    val newContactSubject: PublishSubject<Unit> = PublishSubject.create()
    val userContactsSubject: PublishSubject<UserContact> = PublishSubject.create()
    val usersSubject: PublishSubject<List<User>> = PublishSubject.create()
    val createUserSubject: PublishSubject<User> = PublishSubject.create()

    init {
        createUserSubject
            .observeOn(Schedulers.io())
            .subscribe {
                db.userDao().insert(it)
                updateUsers.onNext(Unit)
            }.addTo(disposeBag)

        updateUserContact
            .observeOn(Schedulers.io())
            .subscribe {
                val contacts = db.userDao().getUserContacts(it)
                userContactsSubject.onNext(contacts)
            }.addTo(disposeBag)

        updateUsers
            .observeOn(Schedulers.io())
            .subscribe {
                val users = db.userDao().getAll()
                usersSubject.onNext(users)
            }.addTo(disposeBag)

        createContactSubject
            .observeOn(Schedulers.io())
            .subscribe {
                db.contactDao().insert(it)
                newContactSubject.onNext(Unit)
            }
            .addTo(disposeBag)
        updateUsers.onNext(Unit)
    }
}