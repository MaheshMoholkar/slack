import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Member } from "@/types";
import { useAuth } from "@/providers/auth-provider";

export const useCurrentMember = ({ workspaceId }: { workspaceId: string }) => {
  const { user } = useAuth();
  const { data, isLoading } = useQuery<Member>({
    queryKey: ["currentMember", workspaceId, user?.id],
    queryFn: async () => {
      const res = await api.get(
        `/members/workspace/${workspaceId}/user/${user!.id}`
      );
      return res.data;
    },
    enabled: !!workspaceId && !!user?.id,
  });
  return { data, isLoading };
};
