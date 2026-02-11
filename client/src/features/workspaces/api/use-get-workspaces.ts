import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Workspace } from "@/types";
import { useAuth } from "@/providers/auth-provider";

export const useGetWorkspaces = () => {
  const { user } = useAuth();
  const { data, isLoading, isFetching } = useQuery<Workspace[]>({
    queryKey: ["workspaces", user?.id],
    queryFn: async () => {
      const res = await api.get(`/workspaces/user/${user!.id}`);
      return res.data;
    },
    enabled: !!user?.id,
  });
  return { data, isLoading, isFetching };
};
