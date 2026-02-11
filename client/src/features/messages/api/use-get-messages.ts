import { useInfiniteQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { API_BASE_URL } from "@/lib/api";

const BATCH_SIZE = 20;

interface UseGetMessagesProps {
  channelId?: string;
  conversationId?: string;
  parentMessageId?: string;
}

export interface MessageWithUser {
  _id: string;
  _creationTime: number;
  memberId: string;
  body: string;
  image: string | null;
  updatedAt?: number;
  user: {
    _id: string;
    name: string;
    image?: string;
  };
  reactions: Array<{
    _id: string;
    value: string;
    count: number;
    memberIds: string[];
  }>;
  threadCount?: number;
  threadImage?: string;
  threadName?: string;
  threadTimestamp?: number;
}

export type GetMessagesReturnType = MessageWithUser[];

function transformMessage(msg: any): MessageWithUser {
  const member = msg.member || {};
  const user = member.user || {};

  // Group reactions by value
  const reactionsMap = new Map<
    string,
    { id: string; value: string; count: number; memberIds: string[] }
  >();
  if (msg.reactions && Array.isArray(msg.reactions)) {
    for (const r of msg.reactions) {
      const key = r.value;
      if (reactionsMap.has(key)) {
        const existing = reactionsMap.get(key)!;
        existing.count++;
        existing.memberIds.push(r.memberId || r.member?.id);
      } else {
        reactionsMap.set(key, {
          id: r.id,
          value: r.value,
          count: 1,
          memberIds: [r.memberId || r.member?.id],
        });
      }
    }
  }

  return {
    _id: msg.id,
    _creationTime: msg.createdAt,
    memberId: msg.memberId || member.id,
    body: msg.body,
    image: msg.imageId
      ? `${API_BASE_URL}/upload/files/${msg.imageId}`
      : null,
    updatedAt: msg.updatedAt,
    user: {
      _id: user.id,
      name: user.name,
      image: user.imageUrl,
    },
    reactions: Array.from(reactionsMap.values()).map((r) => ({
      _id: r.id,
      value: r.value,
      count: r.count,
      memberIds: r.memberIds,
    })),
    threadCount: msg.threadCount,
    threadImage: msg.threadImage,
    threadName: msg.threadName,
    threadTimestamp: msg.threadTimestamp,
  };
}

export { transformMessage };

export const useGetMessages = ({
  channelId,
  conversationId,
  parentMessageId,
}: UseGetMessagesProps) => {
  const queryKey = [
    "messages",
    { channelId, conversationId, parentMessageId },
  ];

  const { data, fetchNextPage, hasNextPage, isFetchingNextPage, isLoading } =
    useInfiniteQuery({
      queryKey,
      queryFn: async ({ pageParam = 0 }) => {
        let url = "";
        if (parentMessageId) {
          // Thread messages — not paginated
          url = `/messages/thread/${parentMessageId}`;
          const res = await api.get(url);
          const messages = Array.isArray(res.data)
            ? res.data
            : res.data.content || [];
          return {
            content: messages.map(transformMessage),
            last: true,
            number: 0,
          };
        } else if (channelId) {
          url = `/messages/channel/${channelId}?page=${pageParam}&size=${BATCH_SIZE}`;
        } else if (conversationId) {
          url = `/messages/conversation/${conversationId}?page=${pageParam}&size=${BATCH_SIZE}`;
        } else {
          return { content: [], last: true, number: 0 };
        }
        const res = await api.get(url);
        return {
          content: (res.data.content || []).map(transformMessage),
          last: res.data.last,
          number: res.data.number,
        };
      },
      getNextPageParam: (lastPage) => {
        if (lastPage.last) return undefined;
        return lastPage.number + 1;
      },
      initialPageParam: 0,
      enabled: !!channelId || !!conversationId || !!parentMessageId,
    });

  const results = data?.pages.flatMap((page) => page.content) ?? [];

  let status: string;
  if (isLoading) {
    status = "LoadingFirstPage";
  } else if (isFetchingNextPage) {
    status = "LoadingMore";
  } else if (hasNextPage) {
    status = "CanLoadMore";
  } else {
    status = "Exhausted";
  }

  return {
    results,
    status,
    loadMore: () => fetchNextPage(),
  };
};
