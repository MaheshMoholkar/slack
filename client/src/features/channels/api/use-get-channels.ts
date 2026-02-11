import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Channel } from "@/types";

export const useGetChannels = ({ workspaceId }: { workspaceId: string }) => {
  const { data, isLoading } = useQuery<Channel[]>({
    queryKey: ["channels", workspaceId],
    queryFn: async () => {
      const res = await api.get(`/channels/workspace/${workspaceId}`);
      return res.data;
    },
    enabled: !!workspaceId,
  });
  return { data, isLoading };
};
