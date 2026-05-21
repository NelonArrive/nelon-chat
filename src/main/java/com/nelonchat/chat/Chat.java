package com.nelonchat.chat;

import com.nelonchat.common.BaseAuditingEntity;
import com.nelonchat.message.Message;
import com.nelonchat.message.MessageState;
import com.nelonchat.message.MessageType;
import com.nelonchat.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "chat")
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID,
	query = "SELECT DISTINCT c FROM Chat c WHERE c.sender.id = :senderId OR c.recipient.id = :senderId ORDER BY createdDate DESC"
)
@NamedQuery(name = ChatConstants.FIND_CHAT_BY_SENDER_ID_AND_RECEIVER,
	query = "SELECT DISTINCT c FROM Chat c WHERE (c.sender.id = :senderId AND c.recipient.id = :recipientId) OR (c.sender.id = :recipientId AND c.recipient.id = :senderId) ORDER BY createdDate DESC"
)
public class Chat extends BaseAuditingEntity {
	
	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	private String id;
	
	@ManyToOne
	@JoinColumn(name = "sender_id")
	private User sender;
	
	@ManyToOne
	@JoinColumn(name = "recipient_id")
	private User recipient;
	
	@OneToMany(mappedBy = "chat", fetch = FetchType.EAGER)
	@OrderBy("createdDate DESC")
	private List<Message> messages;
	
	@Transient
	public String getChatName(final String senderId) {
		if (recipient.getId().equals(senderId)) {
			return sender.getFirstName() + " " + sender.getLastName();
		}
		return recipient.getFirstName() + " " + recipient.getLastName();
	}
	
	@Transient
	public long getUnreadMessages(final String senderId) {
		return messages
			.stream()
			.filter(m -> m.getReceiverId().equals(senderId))
			.filter(m -> MessageState.SENT == m.getState())
			.count();
	}
	
	@Transient
	public String getLastMessages() {
		if (messages != null && !messages.isEmpty()) {
			if (messages.getFirst().getType() != MessageType.TEXT) {
				return "Attachment";
			}
			return messages.getFirst().getContent();
		}
		return null;
	}
	
	@Transient
	public LocalDateTime getLastMessageTime() {
		if (messages != null && !messages.isEmpty()) {
			return messages.getFirst().getCreatedDate();
		}
		return null;
	}
}
