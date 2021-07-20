package com.example.adapters

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap

import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.AsyncListDiffer
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.di.MainActivityContext
import com.example.localDb.Run
import com.example.stepcounterapp.MainActivity
import com.example.stepcounterapp.R
import kotlinx.android.synthetic.main.item_run.view.*
import javax.inject.Inject


class RunAdapter
@Inject
constructor() :
    RecyclerView.Adapter<RunAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        init {
            setImageView(this)
        }
    }

    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    fun setImageView(holder: RunAdapter.ViewHolder) {
        val displaymetrics = DisplayMetrics()
        context.let {
            (it as MainActivity).windowManager.defaultDisplay.getMetrics(displaymetrics)

            // This will get device width and we need half of  th width
            val devicewidth = (displaymetrics.widthPixels / 2)

            //if you need 4-5-6 anything fix imageview in height

            //if you need 4-5-6 anything fix imageview in height
            // val deviceheight = displaymetrics.heightPixels / 3

            holder.itemView.ivRunImage.getLayoutParams().width = devicewidth

            //if you need same height as width you can set devicewidth in holder.image_view.getLayoutParams().height

            //if you need same height as width you can set devicewidth in holder.image_view.getLayoutParams().height
            holder.itemView.ivRunImage.getLayoutParams().height = devicewidth
        }


    }

    val diffCallback = object : DiffUtil.ItemCallback<Run>() {
        override fun areItemsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Run, newItem: Run): Boolean {
            return oldItem.hashCode() == newItem.hashCode()
        }

    }
    val differ = AsyncListDiffer(this, diffCallback)

    fun submitList(list: List<Run>) = differ.submitList(list)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RunAdapter.ViewHolder {
        return ViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.item_run, parent, false)
        )
    }

    override fun onBindViewHolder(holder: RunAdapter.ViewHolder, position: Int) {
        val run: Run = differ.currentList[position]
        holder.itemView.apply {
            Glide.with(this)
                .load(run.icon).into(ivRunImage)
        }

    }

    override fun getItemCount(): Int {
        return differ.currentList.size
    }
}