import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { API_BASE_URL } from "@/lib/api";

function transformMessage(msg: any) {
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

export const useGetMessage = ({ id }: { id: string }) => {
  const { data, isLoading } = useQuery({
    queryKey: ["message", id],
    queryFn: async () => {
      const res = await api.get(`/messages/${id}`);
      return transformMessage(res.data);
    },
    enabled: !!id,
  });
  return { data, isLoading };
};
