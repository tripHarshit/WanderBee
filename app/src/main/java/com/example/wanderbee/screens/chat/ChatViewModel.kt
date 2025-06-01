import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.wanderbee.data.remote.models.chat.ChatMessage
import com.example.wanderbee.data.repository.DefaultChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ChatViewModel(
    private val defaultChatRepository: DefaultChatRepository,
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
) : ViewModel() {

    private val _groupMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val groupMessages: StateFlow<List<ChatMessage>> = _groupMessages

    private val _privateMessages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val privateMessages: StateFlow<List<ChatMessage>> = _privateMessages

    // Join group chat and remove expired participants
    fun joinGroupChat(destinationId: String, destinationName: String) {
        viewModelScope.launch {
            defaultChatRepository.joinDestinationChat(destinationId, destinationName)
            defaultChatRepository.removeExpiredParticipants(destinationId)
        }
    }

    // Send message to group
    fun sendGroupMessage(destinationId: String, text: String) {
        val user = auth.currentUser ?: return
        val message = ChatMessage(
            text = text,
            senderId = user.uid,
            senderName = user.displayName ?: "Unknown"
        )
        viewModelScope.launch {
            defaultChatRepository.sendGroupMessage(destinationId, message)
        }
    }

    // Listen to group messages
    fun listenToGroupMessages(destinationId: String) {
        defaultChatRepository.listenToGroupMessages(destinationId) {
            _groupMessages.value = it
        }
    }

    // Start or get private chat and send message
    fun sendPrivateMessage(otherUserId: String, text: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val chatId = defaultChatRepository.getOrCreatePrivateChat(user.uid, otherUserId)
            val message = ChatMessage(
                text = text,
                senderId = user.uid,
                senderName = user.displayName ?: "Unknown"
            )
            defaultChatRepository.sendPrivateMessage(chatId, message)
        }
    }

    // Listen to private messages
    fun listenToPrivateMessages(otherUserId: String) {
        val user = auth.currentUser ?: return
        viewModelScope.launch {
            val chatId = defaultChatRepository.getOrCreatePrivateChat(user.uid, otherUserId)
            defaultChatRepository.listenToPrivateMessages(chatId) {
                _privateMessages.value = it
            }
        }
    }
}
