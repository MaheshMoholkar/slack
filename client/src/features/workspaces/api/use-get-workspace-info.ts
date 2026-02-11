import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { WorkspaceInfo } from "@/types";
import { useAuth } from "@/providers/auth-provider";

export const useGetWorkspaceInfo = ({ id }: { id: string }) => {
  const { user } = useAuth();
  const { data, isLoading } = useQuery<WorkspaceInfo>({
    queryKey: ["workspaceInfo", id],
    queryFn: async () => {
      const wsRes = await api.get(`/workspaces/${id}`);
      let isMember = false;
      try {
        await api.get(`/members/workspace/${id}/user/${user!.id}`);
        isMember = true;
      } catch {
        isMember = false;
      }
      return { name: wsRes.data.name, isMember };
    },
    enabled: !!id && !!user?.id,
  });
  return { data, isLoading };
};
