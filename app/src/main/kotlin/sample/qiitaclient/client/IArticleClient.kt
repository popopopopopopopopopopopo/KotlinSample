package sample.qiitaclient.client

import retrofit2.http.GET
import retrofit2.http.Query
import sample.qiitaclient.models.Article
import rx.Observable

interface IArticleClient {
    @GET("/api/v2/items")
    fun search(@Query("query") query: String) : Observable<List<Article>>
}