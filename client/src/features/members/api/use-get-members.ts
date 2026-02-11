import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Member } from "@/types";

export const useGetMembers = ({ workspaceId }: { workspaceId: string }) => {
  const { data, isLoading } = useQuery<Member[]>({
    queryKey: ["members", workspaceId],
    queryFn: async () => {
      const res = await api.get(`/workspaces/${workspaceId}/members`);
      return res.data;
    },
    enabled: !!workspaceId,
  });
  return { data, isLoading };
};
