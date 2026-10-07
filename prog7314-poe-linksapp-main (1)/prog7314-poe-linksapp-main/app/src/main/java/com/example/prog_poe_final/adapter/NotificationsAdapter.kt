package com.example.prog_poe_final.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_final.R
import com.example.prog_poe_final.models.Notification

class NotificationAdapter(private val notifications: List<Notification>) :
    RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return NotificationViewHolder(view)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(notifications[position])
    }

    override fun getItemCount(): Int = notifications.size

    class NotificationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderName: TextView = itemView.findViewById(R.id.tvSenderName)
        private val message: TextView = itemView.findViewById(R.id.tvMessage)
        private val timestamp: TextView = itemView.findViewById(R.id.tvTimestamp)

        fun bind(notification: Notification) {
            senderName.text = notification.sender_name
            message.text = notification.message
            timestamp.text = notification.created_at
        }
    }
}
