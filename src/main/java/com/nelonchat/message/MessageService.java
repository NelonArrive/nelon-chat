package com.nelonchat.message;

import com.nelonchat.chat.Chat;
import com.nelonchat.chat.ChatRepository;
import com.nelonchat.file.FileService;
import com.nelonchat.file.FileUtils;
import com.nelonchat.notification.Notification;
import com.nelonchat.notification.NotificationService;
import com.nelonchat.notification.NotificationType;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {
	
	private final MessageRepository messageRepository;
	private final ChatRepository chatRepository;
	private final MessageMapper mapper;
	private final NotificationService notificationService;
	private final FileService fileService;
	
	public void saveMessage(MessageRequest request) {
		Chat chat = chatRepository.findById(request.getChatId())
			.orElseThrow(() -> new EntityNotFoundException("Chat not found"));
		
		Message message = new Message();
		message.setContent(request.getContent());
		message.setChat(chat);
		message.setSenderId(request.getSenderId());
		message.setReceiverId(request.getReceiverId());
		message.setType(request.getType());
		message.setState(MessageState.SENT);
		
		messageRepository.save(message);
		
		Notification notification = Notification.builder()
			.chatId(chat.getId())
			.messageType(request.getType())
			.content(request.getContent())
			.senderId(request.getSenderId())
			.receiverId(request.getReceiverId())
			.type(NotificationType.MESSAGE)
			.chatName(chat.getTargetChatName(message.getSenderId()))
			.build();
		
		notificationService.sendNotification(request.getReceiverId(), notification);
	}
	
	public List<MessageResponse> findChatMessages(String chatId) {
		return messageRepository.findMessagesByChatId(chatId)
			.stream()
			.map(mapper::toMessageResponse)
			.toList();
	}
	
	public void setMessagesToSeen(String chatId, Authentication authentication) {
		Chat chat = chatRepository.findById(chatId)
			.orElseThrow(() -> new EntityNotFoundException("Chat not found"));
		
		final String recipientId = getRecipientId(chat, authentication);
		messageRepository.setMessagesToSeenByChatId(chatId, MessageState.SEEN);
		
		Notification notification = Notification.builder()
			.chatId(chat.getId())
			.type(NotificationType.SEEN)
			.receiverId(recipientId)
			.senderId(getSenderId(chat, authentication))
			.build();
		
		notificationService.sendNotification(recipientId, notification);
	}
	
	public void uploadMediaMessage(String chatId, MultipartFile file, Authentication authentication) {
		Chat chat = chatRepository.findById(chatId)
			.orElseThrow(() -> new RuntimeException("Chat not found"));
		
		final String senderId = getSenderId(chat, authentication);
		final String receiverId = getRecipientId(chat, authentication);
		
		final String filePath = fileService.saveFile(file, senderId);
		Message message = new Message();
		message.setReceiverId(receiverId);
		message.setSenderId(senderId);
		message.setState(MessageState.SENT);
		message.setType(MessageType.IMAGE);
		message.setMediaFilePath(filePath);
		message.setChat(chat);
		messageRepository.save(message);
		
		Notification notification = Notification.builder()
			.chatId(chat.getId())
			.type(NotificationType.IMAGE)
			.senderId(senderId)
			.receiverId(receiverId)
			.messageType(MessageType.IMAGE)
			.media(FileUtils.readFileFromLocation(filePath))
			.build();
		
		notificationService.sendNotification(receiverId, notification);
	}
	
	private String getSenderId(Chat chat, Authentication authentication) {
		if (chat.getSender().getId().equals(authentication.getName())) {
			return chat.getSender().getId();
		}
		return chat.getRecipient().getId();
	}
	
	private String getRecipientId(Chat chat, Authentication authentication) {
		if (chat.getSender().getId().equals(authentication.getName())) {
			return chat.getRecipient().getId();
		}
		return chat.getSender().getId();
	}
}
