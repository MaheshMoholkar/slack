import { useCallback, useEffect, useRef, useState } from "react";
import { Loader, TriangleAlert } from "lucide-react";
import { useQueryClient } from "@tanstack/react-query";

import { useGetChannel } from "@/features/channels/api/use-get-channel";
import { useGetMessages } from "@/features/messages/api/use-get-messages";
import { useGetMembers } from "@/features/members/api/use-get-members";
import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { useChannelId } from "@/hooks/use-channel-id";
import { useWebSocket } from "@/providers/websocket-provider";
import { useAuth } from "@/providers/auth-provider";

import { ChannelHeader } from "./channel-header";
import { ChannelChatInput } from "./channel-chat-input";
import { MessageList } from "@/components/message-list";
import { TypingIndicator } from "@/components/typing-indicator";

const ChannelPage = () => {
  const channelId = useChannelId();
  const workspaceId = useWorkspaceId();
  const { subscribe, unsubscribe, isConnected } = useWebSocket();
  const queryClient = useQueryClient();
  const { user } = useAuth();

  const { data: channel, isLoading: channelLoading } = useGetChannel({ id: channelId });
  const { results, status, loadMore } = useGetMessages({ channelId });
  const { data: members } = useGetMembers({ workspaceId });

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
    if (!isConnected || !workspaceId || !channelId) return;

    const destination = `/topic/workspace/${workspaceId}/channel/${channelId}`;
    const subscriptionId = subscribe(destination, (message) => {
      const event = JSON.parse(message.body);
      
      // Invalidate messages query to refetch and show new message
      if (event.type === "MESSAGE_SENT" || event.type === "MESSAGE_UPDATED" || event.type === "MESSAGE_DELETED" || event.type === "REACTION_ADDED" || event.type === "REACTION_REMOVED") {
        queryClient.invalidateQueries({
          queryKey: ["messages", { channelId, conversationId: undefined, parentMessageId: undefined }],
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
        // Don't show typing indicator for the current user
        if (userId === user?.id) return;

        if (isTyping) {
          const member = members?.find((m) => m.user.id === userId);
          const name = member?.user.name ?? "Someone";
          setTypingUsers((prev) => ({ ...prev, [userId]: name }));

          // Auto-clear after 3s in case stop event is missed
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
      // Clear all typing timers on unmount
      Object.values(typingTimersRef.current).forEach(clearTimeout);
      typingTimersRef.current = {};
      setTypingUsers({});
    };
  }, [isConnected, workspaceId, channelId, subscribe, unsubscribe, queryClient, user?.id, members, clearTypingUser]);

  if (channelLoading || status === "LoadingFirstPage") {
    return (
      <div className="h-full flex-1 flex items-center justify-center">
        <Loader className="animate-spin size-5 text-muted-foreground" />
      </div>
    );
  }

  if (!channel) {
    return (
      <div className="h-full flex-1 flex flex-col gap-y-2 items-center justify-center">
        <TriangleAlert className="size-5 text-muted-foreground" />
        <span className="text-sm text-muted-foreground">Channel not found</span>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full">
      <ChannelHeader title={channel.name} />
      <MessageList
        channelName={channel.name}
        channelCreationTime={channel.createdAt ? new Date(channel.createdAt).getTime() : Date.now()}
        data={results}
        loadMore={loadMore}
        isLoadingMore={status === "LoadingMore"}
        canLoadMore={status === "CanLoadMore"}
      />
      <TypingIndicator typingUsers={Object.values(typingUsers)} />
      <ChannelChatInput placeholder={`Message # ${channel.name}`} />
    </div>
  );
};

export default ChannelPage;
