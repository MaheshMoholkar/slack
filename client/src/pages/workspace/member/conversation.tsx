import { useCallback, useEffect, useRef, useState } from "react";
import { Loader } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";

import { useMemberId } from "@/hooks/use-member-id";
import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { useGetMember } from "@/features/members/api/use-get-member";
import { useGetMessages } from "@/features/messages/api/use-get-messages";
import { usePanel } from "@/hooks/use-panel";
import { useWebSocket } from "@/providers/websocket-provider";
import { useAuth } from "@/providers/auth-provider";

import { MemberHeader } from "./member-header";
import { MemberChatInput } from "./member-chat-input";
import { MessageList } from "@/components/message-list";
import { TypingIndicator } from "@/components/typing-indicator";

interface ConversationProps {
  id: string;
}

export const Conversation = ({ id }: ConversationProps) => {
  const memberId = useMemberId();
  const workspaceId = useWorkspaceId();
  const { subscribe, unsubscribe, isConnected } = useWebSocket();
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const { onOpenProfile } = usePanel();

  const { data: member, isLoading: memberLoading } = useGetMember({ id: memberId });
  const { results, status, loadMore } = useGetMessages({ conversationId: id });

  const [typingUsers, setTypingUsers] = useState<Record<string, string>>({});
  const typingTimersRef = useRef<Record<string, ReturnType<typeof setTimeout>>>({});

  const clearTypingUser = useCallback((userId: string) => {
    setTypingUsers((prev) => {
      const next = { ...prev };
      delete next[userId];
      return next;
    });
    delete typingTimersRef.current[userId];
  }, []);

  // Subscribe to WebSocket for real-time messages
  useEffect(() => {
    if (!isConnected || !workspaceId || !id) return;

    const destination = `/topic/workspace/${workspaceId}/conversation/${id}`;
    const subscriptionId = subscribe(destination, (message) => {
      const event = JSON.parse(message.body);
      
      // Invalidate messages query to refetch and show new message
      if (event.type === "MESSAGE_SENT" || event.type === "MESSAGE_UPDATED" || event.type === "MESSAGE_DELETED" || event.type === "REACTION_ADDED" || event.type === "REACTION_REMOVED") {
        queryClient.invalidateQueries({
          queryKey: ["messages", { channelId: undefined, conversationId: id, parentMessageId: undefined }],
        });
        // Also invalidate any open thread queries so thread replies and reactions update
        queryClient.invalidateQueries({
          predicate: (query) => {
            const key = query.queryKey;
            return (key[0] === "messages" && typeof key[1] === "object" && (key[1] as any)?.parentMessageId != null)
              || key[0] === "message";
          },
        });
      }

      if (event.type === "TYPING_UPDATE") {
        const { userId, isTyping } = event.payload;
        if (userId === user?.id) return;

        if (isTyping) {
          const name = member?.user.name ?? "Someone";
          setTypingUsers((prev) => ({ ...prev, [userId]: name }));

          if (typingTimersRef.current[userId]) {
            clearTimeout(typingTimersRef.current[userId]);
          }
          typingTimersRef.current[userId] = setTimeout(() => {
            clearTypingUser(userId);
          }, 3000);
        } else {
          if (typingTimersRef.current[userId]) {
            clearTimeout(typingTimersRef.current[userId]);
          }
          clearTypingUser(userId);
        }
      }
    });

    return () => {
      unsubscribe(subscriptionId);
      Object.values(typingTimersRef.current).forEach(clearTimeout);
      typingTimersRef.current = {};
      setTypingUsers({});
    };
  }, [isConnected, workspaceId, id, subscribe, unsubscribe, queryClient, user?.id, member, clearTypingUser]);

  if (memberLoading || status === "LoadingFirstPage") {
    return (
      <div className="h-full flex items-center justify-center">
        <Loader className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <MemberHeader
        memberName={member?.user.name}
        memberImage={member?.user.imageUrl}
        onClick={() => onOpenProfile(memberId)}
      />
      <MessageList
        data={results}
        variant="conversation"
        memberImage={member?.user.imageUrl}
        memberName={member?.user.name}
        loadMore={loadMore}
        isLoadingMore={status === "LoadingMore"}
        canLoadMore={status === "CanLoadMore"}
      />
      <TypingIndicator typingUsers={Object.values(typingUsers)} />
      <MemberChatInput
        placeholder={`Message ${member?.user.name ?? "member"}`}
        conversationId={id}
      />
    </div>
  );
};
