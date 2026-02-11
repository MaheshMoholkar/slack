import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { Member } from "@/types";

export const useGetMember = ({ id }: { id: string }) => {
  const { data, isLoading } = useQuery<Member>({
    queryKey: ["member", id],
    queryFn: async () => {
      const res = await api.get(`/members/${id}`);
      return res.data;
    },
    enabled: !!id,
  });
  return { data, isLoading };
};
