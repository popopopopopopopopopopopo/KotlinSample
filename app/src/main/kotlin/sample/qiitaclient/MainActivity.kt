package sample.qiitaclient

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.*
import com.google.gson.FieldNamingPolicy
import com.google.gson.GsonBuilder
import com.trello.rxlifecycle.components.support.RxAppCompatActivity
import com.trello.rxlifecycle.kotlin.bindToLifecycle
import retrofit2.Retrofit
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import sample.qiitaclient.client.IArticleClient
import sample.qiitaclient.extensions.toast
import sample.qiitaclient.models.Article
import sample.qiitaclient.models.User
import sample.qiitaclient.views.ArticleView

class MainActivity : RxAppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val gson = GsonBuilder()
                .setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES)
                .create()

        val retrofit = Retrofit.Builder()
                .baseUrl("https://qiita.com")
                .addConverterFactory(GsonConverterFactory.create(gson))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .build()

        val articleClient = retrofit.create(IArticleClient::class.java)

        setContentView(R.layout.activity_main)

        val listAdapter = ArticleListAdapter(applicationContext)

        val listView : ListView = findViewById(R.id.list_view) as ListView
        listView.adapter = listAdapter

        listView.setOnItemClickListener{ parent, view, position, id ->
            val article = listAdapter.articles[position]
            ArticleActivity.intent(this, article).let { startActivity(it) }
        }

        InitOnClickSearchButton(articleClient,listAdapter)
    }

    private fun InitOnClickSearchButton(articleClient: IArticleClient, listAdapter:ArticleListAdapter){
        val queryEditText = findViewById(R.id.query_edit_text) as EditText
        val searchButton = findViewById(R.id.search_button) as Button
        val progressBar = findViewById(R.id.progress_bar)

        searchButton.setOnClickListener {
            progressBar.visibility = View.VISIBLE
            articleClient.search(queryEditText.text.toString())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .doAfterTerminate({progressBar.visibility = View.GONE})
                    .bindToLifecycle(this)
                    .subscribe({
                        queryEditText.text.clear()
                        listAdapter.articles = it
                        listAdapter.notifyDataSetChanged()
                    }, {
                                toast("エラー：$it")
                    })
        }
    }

    private fun getDummyArticle(title:String,userName:String) = Article(id="",title = title,url="https://kotlinlang.org/",
            user = User(id="",name = userName, profileImageUrl = ""))
}
