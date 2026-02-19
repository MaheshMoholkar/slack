package com.slack.server.config;

import com.slack.server.model.Channel;
import com.slack.server.model.Conversation;
import com.slack.server.model.Member;
import com.slack.server.model.Message;
import com.slack.server.model.Reaction;
import com.slack.server.model.Workspace;
import com.slack.server.model.User;
import com.slack.server.repository.ChannelRepository;
import com.slack.server.repository.ConversationRepository;
import com.slack.server.repository.MemberRepository;
import com.slack.server.repository.MessageRepository;
import com.slack.server.repository.ReactionRepository;
import com.slack.server.repository.UserRepository;
import com.slack.server.repository.WorkspaceRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Map;

@Configuration
public class GuestUserSeedConfig {
    private static final Logger log = LoggerFactory.getLogger(GuestUserSeedConfig.class);

    private record GuestUserSeed(String name, String email, String password) {}

    private static final List<GuestUserSeed> GUEST_USERS = List.of(
        new GuestUserSeed("Guest User 1", "guest.user1@slack-app.dev", "GuestUser@123"),
        new GuestUserSeed("Guest User 2", "guest.user2@slack-app.dev", "GuestUser@123")
    );

    @Bean
    public CommandLineRunner seedGuestUsers(
            UserRepository userRepository,
            WorkspaceRepository workspaceRepository,
            MemberRepository memberRepository,
            ChannelRepository channelRepository,
            ConversationRepository conversationRepository,
            MessageRepository messageRepository,
            ReactionRepository reactionRepository,
            PasswordEncoder passwordEncoder) {
        return args -> {
            log.warn("Resetting database and seeding demo data...");

            // Hard reset in FK-safe order
            reactionRepository.deleteAllInBatch();
            messageRepository.deleteAllInBatch();
            conversationRepository.deleteAllInBatch();
            channelRepository.deleteAllInBatch();
            memberRepository.deleteAllInBatch();
            workspaceRepository.deleteAllInBatch();
            userRepository.deleteAllInBatch();

            // Users
            User guestOne = createUser(
                userRepository,
                passwordEncoder,
                GUEST_USERS.get(0).name(),
                GUEST_USERS.get(0).email(),
                GUEST_USERS.get(0).password(),
                "https://i.pravatar.cc/150?img=11"
            );
            User guestTwo = createUser(
                userRepository,
                passwordEncoder,
                GUEST_USERS.get(1).name(),
                GUEST_USERS.get(1).email(),
                GUEST_USERS.get(1).password(),
                "https://i.pravatar.cc/150?img=12"
            );

            // Workspaces
            Workspace engineeringWorkspace = createWorkspace(workspaceRepository, "Engineering Hub", guestOne.getId(), "ENG123");
            Workspace recruitingWorkspace = createWorkspace(workspaceRepository, "Recruiting Desk", guestTwo.getId(), "REC123");

            // Members
            Member guestOneEngineering = createMember(memberRepository, guestOne, engineeringWorkspace, Member.Role.ADMIN);
            Member guestTwoEngineering = createMember(memberRepository, guestTwo, engineeringWorkspace, Member.Role.MEMBER);
            Member guestOneRecruiting = createMember(memberRepository, guestOne, recruitingWorkspace, Member.Role.MEMBER);
            Member guestTwoRecruiting = createMember(memberRepository, guestTwo, recruitingWorkspace, Member.Role.ADMIN);

            // Channels
            Channel engineeringGeneral = createChannel(channelRepository, engineeringWorkspace, "general");
            Channel engineeringHiring = createChannel(channelRepository, engineeringWorkspace, "hiring");
            Channel recruitingGeneral = createChannel(channelRepository, recruitingWorkspace, "general");

            // Conversation between both users inside Engineering Hub
            Conversation dmConversation = createConversation(conversationRepository, engineeringWorkspace, guestOneEngineering, guestTwoEngineering);

            long now = System.currentTimeMillis();
            long t = now - 60 * 60 * 1000;

            // Channel messages and thread replies
            Message engMsg1 = createChannelMessage(messageRepository, engineeringWorkspace, engineeringGeneral, guestOneEngineering,
                "Morning! Let's align on this week's hiring priorities.", t += 10_000);
            Message engMsg2 = createChannelMessage(messageRepository, engineeringWorkspace, engineeringGeneral, guestTwoEngineering,
                "Sounds good. I'm preparing a shortlist for backend + frontend roles.", t += 10_000);
            Message engThreadParent = createChannelMessage(messageRepository, engineeringWorkspace, engineeringHiring, guestOneEngineering,
                "Thread: please drop candidate updates here.", t += 10_000);
            createThreadReply(messageRepository, engineeringWorkspace, engineeringHiring, null, guestTwoEngineering, engThreadParent,
                "Interviewed Priya today. Strong system design and communication.", t += 10_000);
            createThreadReply(messageRepository, engineeringWorkspace, engineeringHiring, null, guestOneEngineering, engThreadParent,
                "Great. Let's move Priya to final round with leadership.", t += 10_000);

            // Direct messages and thread replies
            Message dm1 = createConversationMessage(messageRepository, engineeringWorkspace, dmConversation, guestOneEngineering,
                "Hey, can you review the role description before I publish?", t += 10_000);
            Message dm2 = createConversationMessage(messageRepository, engineeringWorkspace, dmConversation, guestTwoEngineering,
                "Yes, send it over. I can give feedback in 15 mins.", t += 10_000);
            Message dmThreadParent = createConversationMessage(messageRepository, engineeringWorkspace, dmConversation, guestTwoEngineering,
                "Thread: also track referral candidates here.", t += 10_000);
            createThreadReply(messageRepository, engineeringWorkspace, null, dmConversation, guestOneEngineering, dmThreadParent,
                "Added two referrals from last sprint demo.", t += 10_000);
            createThreadReply(messageRepository, engineeringWorkspace, null, dmConversation, guestTwoEngineering, dmThreadParent,
                "Perfect, I will fast-track both profiles.", t += 10_000);

            // Recruiting workspace messages
            Message recMsg1 = createChannelMessage(messageRepository, recruitingWorkspace, recruitingGeneral, guestTwoRecruiting,
                "Welcome to Recruiting Desk! Let's keep interview notes in this channel.", t += 10_000);
            Message recMsg2 = createChannelMessage(messageRepository, recruitingWorkspace, recruitingGeneral, guestOneRecruiting,
                "Sure. I will post today's panel feedback shortly.", t += 10_000);

            // Reactions
            createReaction(reactionRepository, recruitingWorkspace, recMsg1, guestOneRecruiting, "👍");
            createReaction(reactionRepository, engineeringWorkspace, engThreadParent, guestTwoEngineering, "🔥");
            createReaction(reactionRepository, engineeringWorkspace, dm1, guestTwoEngineering, "✅");

            Map<String, Long> counts = Map.of(
                "users", userRepository.count(),
                "workspaces", workspaceRepository.count(),
                "members", memberRepository.count(),
                "channels", channelRepository.count(),
                "conversations", conversationRepository.count(),
                "messages", messageRepository.count(),
                "reactions", reactionRepository.count()
            );

            log.info("Demo seed completed: {}", counts);
        };
    }

    private User createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String rawPassword,
            String imageUrl) {
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setImageUrl(imageUrl);
        return userRepository.save(user);
    }

    private Workspace createWorkspace(
            WorkspaceRepository workspaceRepository,
            String name,
            String ownerUserId,
            String joinCode) {
        Workspace workspace = new Workspace();
        workspace.setName(name);
        workspace.setUserId(ownerUserId);
        workspace.setJoinCode(joinCode);
        return workspaceRepository.save(workspace);
    }

    private Member createMember(
            MemberRepository memberRepository,
            User user,
            Workspace workspace,
            Member.Role role) {
        Member member = new Member();
        member.setUser(user);
        member.setWorkspace(workspace);
        member.setRole(role);
        return memberRepository.save(member);
    }

    private Channel createChannel(
            ChannelRepository channelRepository,
            Workspace workspace,
            String channelName) {
        Channel channel = new Channel();
        channel.setWorkspace(workspace);
        channel.setName(channelName);
        return channelRepository.save(channel);
    }

    private Conversation createConversation(
            ConversationRepository conversationRepository,
            Workspace workspace,
            Member memberOne,
            Member memberTwo) {
        Conversation conversation = new Conversation();
        conversation.setWorkspace(workspace);
        conversation.setMemberOne(memberOne);
        conversation.setMemberTwo(memberTwo);
        return conversationRepository.save(conversation);
    }

    private Message createChannelMessage(
            MessageRepository messageRepository,
            Workspace workspace,
            Channel channel,
            Member member,
            String body,
            long createdAt) {
        Message message = new Message();
        message.setWorkspace(workspace);
        message.setChannel(channel);
        message.setMember(member);
        message.setBody(toQuillDelta(body));
        message.setCreatedAt(createdAt);
        return messageRepository.save(message);
    }

    private Message createConversationMessage(
            MessageRepository messageRepository,
            Workspace workspace,
            Conversation conversation,
            Member member,
            String body,
            long createdAt) {
        Message message = new Message();
        message.setWorkspace(workspace);
        message.setConversation(conversation);
        message.setMember(member);
        message.setBody(toQuillDelta(body));
        message.setCreatedAt(createdAt);
        return messageRepository.save(message);
    }

    private Message createThreadReply(
            MessageRepository messageRepository,
            Workspace workspace,
            Channel channel,
            Conversation conversation,
            Member member,
            Message parentMessage,
            String body,
            long createdAt) {
        Message message = new Message();
        message.setWorkspace(workspace);
        message.setChannel(channel);
        message.setConversation(conversation);
        message.setMember(member);
        message.setParentMessage(parentMessage);
        message.setBody(toQuillDelta(body));
        message.setCreatedAt(createdAt);
        return messageRepository.save(message);
    }

    private void createReaction(
            ReactionRepository reactionRepository,
            Workspace workspace,
            Message message,
            Member member,
            String value) {
        Reaction reaction = new Reaction();
        reaction.setWorkspace(workspace);
        reaction.setMessage(message);
        reaction.setMember(member);
        reaction.setValue(value);
        reactionRepository.save(reaction);
    }

    private String toQuillDelta(String plainText) {
        String escaped = plainText
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n");
        return "{\"ops\":[{\"insert\":\"" + escaped + "\\n\"}]}";
    }
}
