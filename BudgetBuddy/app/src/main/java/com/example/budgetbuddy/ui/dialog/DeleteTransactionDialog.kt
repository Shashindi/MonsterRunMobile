package com.example.budgetbuddy.ui.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.example.budgetbuddy.R
import com.google.android.material.button.MaterialButton

class DeleteTransactionDialog : DialogFragment() {
    private var onDeleteConfirmed: (() -> Unit)? = null

    fun setOnDeleteConfirmedListener(listener: () -> Unit) {
        onDeleteConfirmed = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_delete_transaction, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.btnCancel).setOnClickListener {
            dismiss()
        }

        view.findViewById<MaterialButton>(R.id.btnDelete).setOnClickListener {
            onDeleteConfirmed?.invoke()
            dismiss()
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return dialog
    }
} 