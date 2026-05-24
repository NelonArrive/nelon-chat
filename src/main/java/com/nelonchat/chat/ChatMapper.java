package com.nelonchat.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatMapper {
	public ChatResponse toChatResponse(Chat chat, String senderId) {
		boolean isSender = chat.getSender().getId().equals(senderId);
		
		boolean isRecipientOnline = isSender
			? chat.getRecipient().isUserOnline()
			: chat.getSender().isUserOnline();
		
		String recipientId = isSender
			? chat.getRecipient().getId()
			: chat.getSender().getId();
		
		long unread = chat.getUnreadMessages(senderId);
		
		return ChatResponse.builder()
			.id(chat.getId())
			.name(chat.getChatName(senderId))
			.unreadCount(unread)
			.lastMessage(chat.getLastMessage())
			.lastMessageTime(chat.getLastMessageTime())
			.isRecipientOnline(isRecipientOnline)
			.senderId(chat.getSender().getId())
			.lastSeen(isSender
				? chat.getRecipient().getLastSeen()
				: chat.getSender().getLastSeen())
			.receiverId(recipientId)
			.build();
	}
}