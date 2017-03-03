package com.rubengees.ktask.sample.android

import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.bindView
import com.rubengees.ktask.android.AndroidLifecycleTask
import com.rubengees.ktask.operation.CacheTask
import com.rubengees.ktask.retrofit.RetrofitTask
import com.rubengees.ktask.sample.RepositoryInfo
import com.rubengees.ktask.sample.Utils
import retrofit2.Call

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MainActivity : AppCompatActivity() {

    private companion object {
        private const val TASK_TAG = "tag_activity_main"
    }

    private lateinit var adapter: RepositoryAdapter
    private lateinit var task: AndroidLifecycleTask<Call<RepositoryInfo>, RepositoryInfo>

    private val progress: SwipeRefreshLayout by bindView(R.id.progress)
    private val content: RecyclerView by bindView(R.id.content)

    private val errorContainer: ViewGroup by bindView(R.id.errorContainer)
    private val errorText: TextView by bindView(R.id.errorText)
    private val errorButton: Button by bindView(R.id.errorButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        adapter = RepositoryAdapter()
        task = AndroidLifecycleTask(this, CacheTask(RetrofitTask()), TASK_TAG)

        initViews()

        task.onInnerStart {
            content.visibility = View.INVISIBLE
            errorContainer.visibility = View.INVISIBLE
            progress.isRefreshing = true
        }.onSuccess {
            content.visibility = View.VISIBLE

            adapter.replace(it.repositories)
        }.onError {
            errorContainer.visibility = View.VISIBLE
            errorText.text = it.message ?: getString(R.string.error_unknown)
        }.onFinish {
            progress.isRefreshing = false
        }.execute(MainApplication.api.mostStarredRepositories(Utils.query()))
    }

    private fun initViews() {
        progress.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        progress.setOnRefreshListener {
            task.freshExecute(MainApplication.api.mostStarredRepositories(Utils.query()))
        }

        content.adapter = adapter
        content.setHasFixedSize(true)
        content.layoutManager = LinearLayoutManager(this)
        content.addItemDecoration(MarginDecoration(marginDp = 8, columns = 1))

        content.visibility = View.INVISIBLE
        errorContainer.visibility = View.INVISIBLE

        errorButton.setOnClickListener {
            task.freshExecute(MainApplication.api.mostStarredRepositories(Utils.query()))
        }
    }
}
