package com.example.bulletinboardtwo.utils

import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView

class ItemTouchMoveCallback(val adapter: ItemTouchAdapter) : ItemTouchHelper.Callback() {

    override fun getMovementFlags(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder
    ): Int {//Как имет мы хотим взаимодействовать с items
        val dragFlag = ItemTouchHelper.UP or ItemTouchHelper.DOWN
        return makeMovementFlags(dragFlag, 0)
    }

    override fun onMove(
        recyclerView: RecyclerView,
        viewHolder: RecyclerView.ViewHolder,//елемент который мы взяли
        target: RecyclerView.ViewHolder//эелемент на который мы положили
    ): Boolean {
        adapter.onMove(viewHolder.adapterPosition,target.adapterPosition)
       return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

    }

    override fun onSelectedChanged(viewHolder: RecyclerView.ViewHolder?, actionState: Int) {
        if(actionState !=ItemTouchHelper.ACTION_STATE_IDLE) viewHolder?.itemView?.alpha=0.5f
        super.onSelectedChanged(viewHolder, actionState)
    }

    override fun clearView(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder) {
        viewHolder.itemView.alpha=1.0f
        adapter.clearView()
        super.clearView(recyclerView, viewHolder)
    }

    interface ItemTouchAdapter {
        fun onMove(startPos: Int, targetPos: Int)
        fun clearView()
    }
}