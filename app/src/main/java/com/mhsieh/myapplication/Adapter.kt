package com.mhsieh.myapplication

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.mhsieh.myapplication.data.FoodData
import com.mhsieh.myapplication.databinding.ListItemLayoutBinding
import com.mhsieh.myapplication.util.calcShelfLife
import timber.log.Timber


class FoodAdapter(private val clickListener: FoodDataListener) :
    androidx.recyclerview.widget.ListAdapter<FoodData, FoodAdapter.FoodViewHolder>(
        FoodDataDiffCallback()
    ) {

    var onFoodWastedPress: ((FoodData) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FoodViewHolder {
        return FoodViewHolder.from(parent).apply {
            onFoodWastedPress = { foodData ->
                this@FoodAdapter.onFoodWastedPress?.invoke(foodData)
            }
        }
    }

    override fun onBindViewHolder(holder: FoodViewHolder, position: Int) {
        holder.bind(getItem(position), clickListener)
    }

    class FoodViewHolder private constructor(private val binding: ListItemLayoutBinding) :
        RecyclerView.ViewHolder(binding.root) {
        var onFoodWastedPress: ((FoodData) -> Unit)? = null

        fun bind(item: FoodData, clickListener: FoodDataListener) {
            binding.food = item

            /*binding.foodName.text = item.foodName
            binding.foodAge.text = foodStatusText(item)*/
            binding.clicklistener = clickListener
            binding.foodWastedButton.setOnClickListener {
                onFoodWastedPress?.invoke(item)
            }
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup): FoodViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = ListItemLayoutBinding.inflate(
                    layoutInflater,
                    parent,
                    false
                )
                return FoodViewHolder(binding)
            }
        }

        fun foodStatusText(food: FoodData): String {
            var shelfLife = food.shelfLife
            if (food.spoiled) {
                return "Lasted ${shelfLife} days"
            } else {
                var foodStatus = when (shelfLife) {
                    0 -> "Fresh"
                    in 1..4 -> "$shelfLife days old"
                    else -> "Food may be spoiled!"
                }
                return foodStatus
            }
        }
    }
}

class FoodDataListener(val clickListener: (foodId: Int) -> Unit) {
    fun onClick(food: FoodData) = clickListener(food.foodId)
}

class FoodDataDiffCallback : DiffUtil.ItemCallback<FoodData>() {
    override fun areItemsTheSame(oldItem: FoodData, newItem: FoodData): Boolean {
        return oldItem.foodId == newItem.foodId
    }

    override fun areContentsTheSame(oldItem: FoodData, newItem: FoodData): Boolean {
        return oldItem == newItem
    }

}