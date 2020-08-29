package com.example.threadingnetwork

import android.content.Context
import android.net.ConnectivityManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import androidx.lifecycle.*
import kotlinx.android.synthetic.main.activity_president.*
import kotlinx.android.synthetic.main.item_president.view.*
import kotlinx.coroutines.Dispatchers
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : AppCompatActivity() {
    lateinit var viewModel: WikiViewModel
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_president)
        viewModel = ViewModelProvider(this).get(WikiViewModel::class.java)
        val adapter = PresidentListAdapter(this, GlobalModel.presidents)
        lista.adapter = adapter

        viewModel.hits.observe(this, {
            spinner.visibility = View.GONE
            hits.visibility = View.VISIBLE
            hits.text = "hits: ${it.query?.searchinfo?.totalhits.toString()}"
        })
    }

    private inner class PresidentListAdapter(
        context: Context,
        private val presidents: MutableList<President>
    ) : BaseAdapter() {
        private val inflater =
            context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

        override fun getView(p0: Int, p1: View?, p2: ViewGroup?): View {
            val row = inflater.inflate(R.layout.item_president, p2, false)
            val president = presidents[p0]

            row.name.text = president.name
            row.start.text = president.startDuty.toString()
            row.end.text = president.endDuty.toString()

            row.setOnClickListener {
                tekstiii.text = president.toString()
                if (isNetworkAvailable()) {
                    spinner.visibility = View.VISIBLE
                    hits.visibility = View.GONE
                    viewModel.setQuery(president.name)
                }
            }

            return row
        }

        override fun getCount(): Int {
            return presidents.size
        }

        override fun getItem(p0: Int): Any {
            return presidents[p0]
        }

        override fun getItemId(p0: Int): Long {
            return p0.toLong()
        }
    }

    private fun isNetworkAvailable(): Boolean =
        (this.getSystemService(Context.CONNECTIVITY_SERVICE)
                as ConnectivityManager).isDefaultNetworkActive
}

object Api {
    data class WikiQuery(val query: Info?)
    data class Info(val searchinfo: SearchInfo?)
    data class SearchInfo(val totalhits: Int?)

    const val URL = "https://en.wikipedia.org/w/"

    interface Service {
        @GET("api.php?action=query&format=json&list=search&")
        suspend fun presidentQuery(@Query("srsearch") action: String): WikiQuery
    }

    private val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val service = retrofit.create(Service::class.java)
}

class WikiRepo() {
    private val call = Api.service
    suspend fun getHitsCount(name: String) = call.presidentQuery(name)
}

class WikiViewModel : ViewModel() {
    private val presidentQuery = MutableLiveData<String>()
    private val repo = WikiRepo()

    fun setQuery(name: String) {
        presidentQuery.value = name
    }

    val hits = presidentQuery.switchMap {
        liveData(Dispatchers.IO) {
            emit(repo.getHitsCount(it))
        }
    }

}


