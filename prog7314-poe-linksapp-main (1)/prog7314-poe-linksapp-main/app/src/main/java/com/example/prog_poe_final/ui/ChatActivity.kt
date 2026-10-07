package com.example.prog_poe_final.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.prog_poe_final.R
import com.example.prog_poe_final.adapter.MessagesAdapter
import com.example.prog_poe_final.api.ApiClient
import com.example.prog_poe_final.models.Message
import com.example.prog_poe_final.models.MessageRequest
import com.example.prog_poe_final.utils.CurrentUserHolder
import com.example.prog_poe_final.utils.OfflineManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ChatActivity : AppCompatActivity() {

    private var chatId: Int? = null
    private var otherUserId: Int = -1
    private lateinit var rvMessages: RecyclerView
    private lateinit var etMessage: EditText
    private lateinit var sendButton: ImageView
    private lateinit var nameTextView: TextView
    private val messages = mutableListOf<Message>()
    private lateinit var adapter: MessagesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        rvMessages = findViewById(R.id.rvMessages)
        etMessage = findViewById(R.id.etMessage)
        sendButton = findViewById(R.id.sendButton)
        nameTextView = findViewById(R.id.searcheduser_name)

        rvMessages.layoutManager = LinearLayoutManager(this)
        adapter = MessagesAdapter(messages)
        rvMessages.adapter = adapter

        otherUserId = intent.getIntExtra("userId", -1)
        val userName = intent.getStringExtra("userName")
        nameTextView.text = userName ?: "Unknown User"

        findViewById<ImageView>(R.id.back_icon).setOnClickListener { finish() }

        createOrFetchChat()

        sendButton.setOnClickListener { sendMessage() }
    }

    // 🔄 WHEN ACTIVITY COMES BACK ONLINE → SEND OFFLINE MESSAGES
    override fun onResume() {
        super.onResume()

        if (OfflineManager.isOnline(this)) {
            val queue = OfflineManager.getQueue(this)

            if (queue.isNotEmpty()) {
                for (json in queue) {
                    sendQueuedMessage(json)
                }
                OfflineManager.clearQueue(this)
                Toast.makeText(this, "Offline messages sent!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // 👉 Converts queued JSON into MessageRequest and sends it
    private fun sendQueuedMessage(json: String) {
        val parts = json.replace("{", "")
            .replace("}", "")
            .replace("\"", "")
            .split(",")

        val map = parts.associate {
            val (key, value) = it.split(":")
            key.trim() to value.trim()
        }

        val request = MessageRequest(
            chat_id = map["chat_id"]!!.toInt(),
            sender_id = map["sender_id"]!!.toInt(),
            message_text = map["message_text"]!!
        )

        ApiClient.api.sendMessage(request).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(
                call: Call<Map<String, Any>>,
                response: Response<Map<String, Any>>
            ) {
                // No UI update needed (messages already shown earlier)
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {}
        })
    }

    // ---------------------------------------------------------
    // CREATE OR FETCH CHAT
    // ---------------------------------------------------------
    private fun createOrFetchChat() {
        val currentUser = CurrentUserHolder.getCurrentUser() ?: return

        val chatRequest = mapOf(
            "user1_id" to currentUser.user_id!!,
            "user2_id" to otherUserId
        )

        ApiClient.api.createOrFetchChat(chatRequest)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        val body = response.body()!!

                        chatId = when {
                            body["chat_id"] != null -> (body["chat_id"] as Double).toInt()
                            (body["chat"] as? Map<*, *>)?.get("chat_id") != null ->
                                ((body["chat"] as Map<*, *>)["chat_id"] as Double).toInt()
                            else -> null
                        }

                        if (chatId != null) {
                            loadMessages()
                        }
                    } else {
                        Toast.makeText(this@ChatActivity, "Failed to start chat", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(this@ChatActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
    }

    // ---------------------------------------------------------
    // LOAD MESSAGES
    // ---------------------------------------------------------
    private fun loadMessages() {
        val id = chatId ?: return

        ApiClient.api.getChatMessages(id).enqueue(object : Callback<List<Message>> {
            @SuppressLint("NotifyDataSetChanged")
            override fun onResponse(call: Call<List<Message>>, response: Response<List<Message>>) {
                if (response.isSuccessful) {
                    messages.clear()
                    messages.addAll(response.body() ?: emptyList())
                    adapter.notifyDataSetChanged()
                    rvMessages.scrollToPosition(messages.size - 1)
                }
            }

            override fun onFailure(call: Call<List<Message>>, t: Throwable) {
                Toast.makeText(this@ChatActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    // ---------------------------------------------------------
    // SEND MESSAGE (OFFLINE + ONLINE)
    // ---------------------------------------------------------
    private fun sendMessage() {
        val content = etMessage.text.toString().trim()
        val id = chatId ?: return
        val currentUser = CurrentUserHolder.getCurrentUser() ?: return

        if (content.isEmpty()) return

        val tempMessage = Message(
            message_id = -1,
            chat_id = id,
            sender_id = currentUser.user_id!!,
            message_text = content,
            created_at = "Sending..."
        )

        messages.add(tempMessage)
        adapter.notifyItemInserted(messages.size - 1)
        rvMessages.scrollToPosition(messages.size - 1)
        etMessage.text.clear()

        // -------- OFFLINE MODE --------
        if (!OfflineManager.isOnline(this)) {
            val json = """{
                "chat_id": $id,
                "sender_id": ${currentUser.user_id},
                "message_text": "$content"
            }""".trimIndent()

            OfflineManager.saveToQueue(this, json)

            Toast.makeText(this, "Saved offline — will send when online", Toast.LENGTH_SHORT).show()
            return
        }

        // -------- ONLINE MODE --------
        val messageRequest = MessageRequest(
            chat_id = id,
            sender_id = currentUser.user_id,
            message_text = content
        )

        ApiClient.api.sendMessage(messageRequest)
            .enqueue(object : Callback<Map<String, Any>> {
                override fun onResponse(
                    call: Call<Map<String, Any>>,
                    response: Response<Map<String, Any>>
                ) {
                    val data = response.body()?.get("data") as? Map<*, *> ?: return

                    val newMessage = Message(
                        message_id = (data["message_id"] as Double).toInt(),
                        chat_id = (data["chat_id"] as Double).toInt(),
                        sender_id = (data["sender_id"] as Double).toInt(),
                        message_text = data["message_text"].toString(),
                        created_at = data["created_at"].toString()
                    )

                    messages[messages.size - 1] = newMessage
                    adapter.notifyItemChanged(messages.size - 1)
                }

                override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                    Toast.makeText(
                        this@ChatActivity,
                        "Error: ${t.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }
}
