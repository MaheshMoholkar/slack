import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Workspace } from "@/types";

export const useGetWorkspace = ({ id }: { id: string }) => {
  const { data, isLoading } = useQuery<Workspace>({
    queryKey: ["workspace", id],
    queryFn: async () => {
      const res = await api.get(`/workspaces/${id}`);
      return res.data;
    },
    enabled: !!id,
  });
  return { data, isLoading };
};
