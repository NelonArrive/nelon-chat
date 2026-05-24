package com.nelonchat.message;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {
	
	@Transactional
	@Modifying
	@Query(name = MessageConstants.SET_MESSAGES_TO_SEEN_BY_CHAT)
	void setMessagesToSeenByChatId(@Param("chatId") String chatId, @Param("newState") MessageState state);
	
	@Query(name = MessageConstants.FIND_MESSAGES_BY_CHAT_ID)
	List<Message> findMessagesByChatId(@Param("chatId") String chatId, Pageable pageable);
}