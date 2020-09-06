package com.example.threadingnetwork

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity(), UserListFragment.OnUserSelected {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "roomDB"

        if (savedInstanceState == null ) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.root_layout, UserListFragment.newInstance())
                .commit()
        }
    }

    override fun onUserSelect(user: User) {
        val fragment = UserDetailsFragment.newInstance(user)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.root_layout, fragment)
            .addToBackStack(null)
            .commit()
    }
}



