package com.example.prog_poe_final.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_final.R
import com.example.prog_poe_final.adapter.NotificationAdapter
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.models.Notification
import com.example.prog_poe_final.utils.CurrentUserHolder
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class NotificationFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: NotificationAdapter
    private val notifications = mutableListOf<Notification>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val view = inflater.inflate(R.layout.fragment_notification, container, false)

        recyclerView = view.findViewById(R.id.rvNotifications)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        adapter = NotificationAdapter(notifications)
        recyclerView.adapter = adapter

        // 🔥 Load notifications immediately
        loadNotifications()

        return view
    }

    private fun loadNotifications() {
        val user = CurrentUserHolder.getCurrentUser()

        if (user == null) {
            Toast.makeText(requireContext(), "User not logged in", Toast.LENGTH_SHORT).show()
            return
        }

        ApiClient.api.getNotifications(user.user_id!!)
            .enqueue(object : Callback<List<Notification>> {
                @SuppressLint("NotifyDataSetChanged")
                override fun onResponse(
                    call: Call<List<Notification>>,
                    response: Response<List<Notification>>
                ) {
                    if (response.isSuccessful) {
                        notifications.clear()
                        notifications.addAll(response.body() ?: emptyList())
                        adapter.notifyDataSetChanged()
                    } else {
                        Toast.makeText(requireContext(), "Failed to load notifications", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<List<Notification>>, t: Throwable) {
                    Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })

    }
}
