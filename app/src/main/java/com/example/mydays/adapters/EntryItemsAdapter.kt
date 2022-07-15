package com.example.mydays.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.mydays.R
import com.example.mydays.models.Entry
import kotlinx.android.synthetic.main.item_row.view.*

open class EntryItemsAdapter(
    private val context: Context,
    private var list: ArrayList<Entry>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var onClickListener: OnClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return MyViewHolder(
            LayoutInflater.from(context)
                .inflate(
                    R.layout.item_row,
                    parent,
                    false
                )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val model = list[position]
        if (holder is MyViewHolder) {
            Glide
                .with(context)
                .load(model.image)
                .centerCrop()
                .into(holder.itemView.iv_item_row)

        }
        holder.itemView.tv_entry_title.text = model.title
        holder.itemView.tv_entry_summary.text = model.entry
        holder.itemView.tv_date.text = model.date
        holder.itemView.indicatorRatingBar.rating = model.rating.toFloat()

        holder.itemView.setOnClickListener {
            if (onClickListener != null) {
                onClickListener!!.onClick(position, model)
            }
        }


    }

    interface OnClickListener {
        fun onClick(position: Int, model: Entry) {
        }
    }

    fun setOnClickListener(onClickListener: OnClickListener) {
        this.onClickListener = onClickListener
    }

    override fun getItemCount(): Int {
        return list.size
    }

    private class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    }


}