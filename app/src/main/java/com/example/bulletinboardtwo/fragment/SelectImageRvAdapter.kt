package com.example.bulletinboardtwo.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.bulletinboardtwo.R
import com.example.bulletinboardtwo.act.EditAdsAct
import com.example.bulletinboardtwo.databinding.SelectImageFragItemBinding
import com.example.bulletinboardtwo.utils.AdapterCallback
import com.example.bulletinboardtwo.utils.ImageManager
import com.example.bulletinboardtwo.utils.ImagePicker
import com.example.bulletinboardtwo.utils.ItemTouchMoveCallback
import com.google.android.gms.common.config.GservicesValue.value

class SelectImageRvAdapter(val adapterCallback: AdapterCallback) : RecyclerView.Adapter<SelectImageRvAdapter.ImageHolder>(), ItemTouchMoveCallback.ItemTouchAdapter {

    val mainArray = ArrayList<Bitmap>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageHolder {
        val viewBinding = SelectImageFragItemBinding.inflate(LayoutInflater.from(parent.context),parent, false)
        return ImageHolder(viewBinding, parent.context/*Это для R*/,this)
    }

    override fun onBindViewHolder(holder: ImageHolder, position: Int) {
        holder.setData(mainArray[position])
    }

    override fun getItemCount(): Int {
        return mainArray.size
    }

    override fun onMove(startPos: Int, targetPos: Int) {
        val targetItem = mainArray[targetPos]//ЕЛЕМЕНТ НА КОТОРЫЙ ПАДАЮТ
        mainArray[targetPos] = mainArray[startPos]
        //  val titleStart=mainList[targetPos].title
        // mainList[targetPos].title=targetItem.title
        mainArray[startPos] = targetItem
        // mainList[startPos].title=titleStart
        notifyItemMoved(startPos, targetPos)
    }

    @SuppressLint("NotifyDataSetChanged")
    override fun clearView() {
        notifyDataSetChanged()
    }

    class ImageHolder(
        val viewBinding: SelectImageFragItemBinding,
        val context: Context,
        val adapter:SelectImageRvAdapter) : RecyclerView.ViewHolder(viewBinding.root) {

        fun setData(bitMap: Bitmap) {

            viewBinding.imEditImage.setOnClickListener {
              ImagePicker.getSingleImage(context as EditAdsAct)
                    context.editImagePos=adapterPosition
            }

            viewBinding.imDeleteImage.setOnClickListener {
                adapter.mainArray.removeAt(adapterPosition)
                adapter.notifyItemRemoved(adapterPosition)
                for(i in 0 until adapter.mainArray.size) adapter.notifyItemChanged(i)
                adapter.adapterCallback.onItemDelete()
            }

            viewBinding.tvTitle.text = context.resources.getStringArray(R.array.title_name)[adapterPosition]
            ImageManager.chooseScaleType(viewBinding.imageContent,bitMap)
            viewBinding.imageContent.setImageBitmap(bitMap)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(newList: List<Bitmap>, needClear: Boolean) {
        if (needClear) mainArray.clear()
        mainArray.addAll(newList)
        notifyDataSetChanged()
    }

}