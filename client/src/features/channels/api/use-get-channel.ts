import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Channel } from "@/types";

export const useGetChannel = ({ id }: { id: string }) => {
  const { data, isLoading } = useQuery<Channel>({
    queryKey: ["channel", id],
    queryFn: async () => {
      const res = await api.get(`/channels/${id}`);
      return res.data;
    },
    enabled: !!id,
  });
  return { data, isLoading };
};
