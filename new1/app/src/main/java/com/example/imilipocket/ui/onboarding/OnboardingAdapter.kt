package com.example.imilipocket.ui.onboarding

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.imilipocket.R

class OnboardingAdapter(private val activity: OnboardingActivity) : RecyclerView.Adapter<OnboardingAdapter.OnboardingViewHolder>() {

    private val onboardingItems = listOf(
        OnboardingItem(
            R.drawable.image1,
            "Welcome to PocketFlow",
            "Your personal finance companion for smart money management"
        ),
        OnboardingItem(
            R.drawable.image2,
            "Set Budgets",
            "Create and manage budgets for different categories"
        ),
        OnboardingItem(
            R.drawable.image3,
            "Let's Get Started",
            "Set up your passcode to begin managing your finances"
        )
    )

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OnboardingViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_onboarding, parent, false)
        return OnboardingViewHolder(view)
    }

    override fun onBindViewHolder(holder: OnboardingViewHolder, position: Int) {
        val item = onboardingItems[position]
        holder.bind(item)
        
        // If this is the last item, show the "Get Started" button
        if (position == onboardingItems.size - 1) {
            holder.showGetStartedButton()
        } else {
            holder.hideGetStartedButton()
        }
    }

    override fun getItemCount() = onboardingItems.size

    inner class OnboardingViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val imageView: ImageView = itemView.findViewById(R.id.imageView)
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.descriptionTextView)
        private val getStartedButton: View = itemView.findViewById(R.id.getStartedButton)

        fun bind(item: OnboardingItem) {
            imageView.setImageResource(item.imageResId)
            titleTextView.text = item.title
            descriptionTextView.text = item.description

            getStartedButton.setOnClickListener {
                activity.navigateToPasscode()
            }
        }

        fun showGetStartedButton() {
            getStartedButton.visibility = View.VISIBLE
        }

        fun hideGetStartedButton() {
            getStartedButton.visibility = View.GONE
        }
    }

    data class OnboardingItem(
        val imageResId: Int,
        val title: String,
        val description: String
    )
} 