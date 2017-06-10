package com.rubengees.ktask.sample.android

import android.os.Bundle
import android.os.Parcelable
import android.support.v4.app.Fragment
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import butterknife.bindView
import com.rubengees.ktask.android.AndroidLifecycleTask
import com.rubengees.ktask.android.bindToLifecycle
import com.rubengees.ktask.retrofit.asyncRetrofitTask
import com.rubengees.ktask.sample.RepositoryInfo
import com.rubengees.ktask.sample.Utils
import com.rubengees.ktask.util.TaskBuilder
import retrofit2.Call

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class MainFragment : Fragment() {

    companion object {
        private const val LIST_STATE = "list_state"

        fun newInstance(): MainFragment {
            return MainFragment()
        }
    }

    private lateinit var task: AndroidLifecycleTask<Call<RepositoryInfo>, RepositoryInfo>
    private lateinit var adapter: RepositoryAdapter

    private var listState: Parcelable? = null

    private val progress: SwipeRefreshLayout by bindView(R.id.progress)
    private val content: RecyclerView by bindView(R.id.content)

    private val errorContainer: ViewGroup by bindView(R.id.errorContainer)
    private val errorText: TextView by bindView(R.id.errorText)
    private val errorButton: Button by bindView(R.id.errorButton)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        task = TaskBuilder.asyncRetrofitTask<RepositoryInfo>()
                .cache()
                .bindToLifecycle(this)
                .onInnerStart {
                    content.visibility = View.INVISIBLE
                    errorContainer.visibility = View.INVISIBLE
                    progress.isRefreshing = true
                }
                .onSuccess {
                    content.visibility = View.VISIBLE

                    adapter.replace(it.repositories)
                }
                .onError {
                    errorContainer.visibility = View.VISIBLE
                    errorText.text = it.message ?: getString(R.string.error_unknown)
                }
                .onFinish {
                    progress.isRefreshing = false

                    if (listState != null) {
                        content.layoutManager.onRestoreInstanceState(listState)

                        listState = null
                    }
                }
                .build()

        adapter = RepositoryAdapter()
        listState = savedInstanceState?.getParcelable(LIST_STATE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_main, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progress.setColorSchemeResources(R.color.colorPrimary, R.color.colorPrimaryDark)
        progress.isRefreshing = task.isWorking
        progress.setOnRefreshListener {
            task.freshExecute(MainApplication.api.mostStarredRepositories(Utils.query()))
        }

        content.setHasFixedSize(true)
        content.adapter = adapter
        content.layoutManager = LinearLayoutManager(context)
        content.addItemDecoration(MarginDecoration(marginDp = 8, columns = 1))

        content.visibility = View.INVISIBLE
        errorContainer.visibility = View.INVISIBLE

        errorButton.setOnClickListener {
            task.freshExecute(MainApplication.api.mostStarredRepositories(Utils.query()))
        }
    }

    override fun onResume() {
        super.onResume()

        task.execute(MainApplication.api.mostStarredRepositories(Utils.query()))
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)

        outState.putParcelable(LIST_STATE, content.layoutManager.onSaveInstanceState())
    }

    override fun onDestroy() {
        super.onDestroy()

        MainApplication.refWatcher.watch(this)
    }
}
