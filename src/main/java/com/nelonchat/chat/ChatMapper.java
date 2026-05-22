package com.nelonchat.chat;

import org.springframework.stereotype.Service;

@Service
public class ChatMapper {
	public ChatResponse toChatResponse(Chat chat, String senderId) {
		return ChatResponse.builder()
			.id(chat.getId())
			.name(chat.getChatName(senderId))
			.unreadCount(chat.getUnreadMessages(senderId))
			.lastMessage(chat.getLastMessages())
			.isRecipientOnline(chat.getRecipient().isUserOnline())
			.senderId(chat.getSender().getId())
			.receiveId(chat.getRecipient().getId())
			.build();
	}
}
