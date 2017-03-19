package com.rubengees.ktask.sample.android

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.support.v4.content.ContextCompat.startActivity
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import butterknife.bindView
import com.rubengees.ktask.sample.RepositoryInfo

/**
 * TODO: Describe class
 *
 * @author Ruben Gees
 */
class RepositoryAdapter(items: Collection<RepositoryInfo.Repository>? = null) :
        RecyclerView.Adapter<RepositoryAdapter.ViewHolder>() {

    val items: ArrayList<RepositoryInfo.Repository> = ArrayList()

    init {
        setHasStableIds(true)

        items?.let {
            this.items += it
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_repository, parent, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) = holder.bind(items[position])
    override fun getItemCount() = items.size
    override fun getItemId(position: Int) = items[position].id

    fun replace(newItems: Collection<RepositoryInfo.Repository>) {
        this.items.clear()
        this.items += newItems

        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val name: TextView by bindView(R.id.name)
        private val stars: TextView by bindView(R.id.stars)
        private val owner: TextView by bindView(R.id.owner)

        init {
            itemView.setOnClickListener {
                if (adapterPosition != RecyclerView.NO_POSITION) {
                    startActivity(it.context, Intent(Intent.ACTION_VIEW).apply {
                        data = Uri.parse(items[adapterPosition].url)
                    }, Bundle.EMPTY)
                }
            }
        }

        fun bind(item: RepositoryInfo.Repository) {
            name.text = item.name
            stars.text = item.stars.toString()
            owner.text = owner.context.getString(R.string.owner, item.owner.name)
        }
    }
}
