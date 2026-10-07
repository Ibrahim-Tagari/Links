package com.example.prog_poe_final.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SearchView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.prog_poe_final.R
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.models.User
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private lateinit var searchView: SearchView
    private lateinit var searchedUserLayout: View
    private lateinit var searchedName: TextView
    private lateinit var imageView: ImageView

    // Store selected user
    private var selectedUser: User? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        // Initialize views
        searchView = view.findViewById(R.id.search_view)
        searchedUserLayout = view.findViewById(R.id.searched_user)
        searchedName = view.findViewById(R.id.searched_name)
        imageView = view.findViewById(R.id.imageView)

        searchedUserLayout.visibility = View.GONE // hide result by default

        // 🔍 Search input listener
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()) {
                    searchForUser(query)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                if (newText.isNullOrEmpty()) {
                    searchedUserLayout.visibility = View.GONE
                }
                return true
            }
        })

        // 👇 Navigate to ChatActivity when the user clicks the searched user layout
        searchedUserLayout.setOnClickListener {
            selectedUser?.let { user ->
                val intent = Intent(requireContext(), ChatActivity::class.java)
                intent.putExtra("userId", user.user_id)
                intent.putExtra("userName", user.name)
                startActivity(intent)
            } ?: Toast.makeText(requireContext(), "No user selected", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun searchForUser(query: String) {
        ApiClient.api.searchUsers(query).enqueue(object : Callback<List<User>> {
            override fun onResponse(call: Call<List<User>>, response: Response<List<User>>) {
                if (response.isSuccessful && !response.body().isNullOrEmpty()) {
                    val user = response.body()!!.first()
                    selectedUser = user

                    searchedUserLayout.visibility = View.VISIBLE
                    searchedName.text = user.name

                } else {
                    searchedUserLayout.visibility = View.GONE
                    Toast.makeText(requireContext(), "No user found", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<User>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
