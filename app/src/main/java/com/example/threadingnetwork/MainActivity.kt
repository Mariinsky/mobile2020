package com.example.threadingnetwork

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.fragment_save.*
import kotlinx.android.synthetic.main.fragment_save.view.*
import kotlinx.android.synthetic.main.frament_read.view.*

internal const val FILENAME = "myFile.txt"

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        title = "internalStorage"

        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .add(R.id.root_layout, SaveFragment.newInstance(), "save")
                .commit()
        }

        save_button.setOnClickListener {
            val frag = supportFragmentManager.findFragmentByTag("save")
            val manager = supportFragmentManager.beginTransaction()
            if (frag != null && frag.isVisible) {
                manager.replace(R.id.root_layout, frag, "save").commit()
            } else {
                manager
                    .replace(R.id.root_layout, SaveFragment.newInstance(), "save")
                    .addToBackStack(null)
                    .commit()
            }
        }

        read_button.setOnClickListener {
            val frag = supportFragmentManager.findFragmentByTag("read")
            val manager = supportFragmentManager  .beginTransaction()
            if (frag != null && frag.isVisible) {
                manager.replace(R.id.root_layout, frag, "read").commit()
            } else {
                manager
                    .replace(R.id.root_layout, ReadFragment.newInstance(), "read")
                    .addToBackStack(null)
                    .commit()
            }
        }

    }
}

class SaveFragment : Fragment() {

    companion object {
        fun newInstance(): SaveFragment {
            return SaveFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_save, container, false)

        view.save_to_file_button.setOnClickListener {
            context?.openFileOutput(FILENAME, Context.MODE_APPEND).use {
                it?.write("${text_input.text}\n".toByteArray())
            }
            text_input.text.clear()
            text_feedback.text = "File Saved"
            hideKeyboard()
        }
        return view
    }

    private fun hideKeyboard() {
        val view = activity!!.currentFocus
        if (view != null) {
            val inputManager: InputMethodManager =
                activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputManager.hideSoftInputFromWindow(
                view.windowToken,
                InputMethodManager.HIDE_NOT_ALWAYS
            )
        }
    }
}

class ReadFragment : Fragment() {

    companion object {
        fun newInstance(): ReadFragment {
            return ReadFragment()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.frament_read, container, false)
        view.text_from_file.text = context?.openFileInput(FILENAME)?.bufferedReader().use {
                it?.readText() ?: getString(R.string.read_file_failed)
        }
        return view
    }

}




