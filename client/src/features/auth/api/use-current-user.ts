import { useQuery } from "@tanstack/react-query";
import api from "@/lib/api";
import { User } from "@/types";

export const useCurrentUser = () => {
  const { data, isLoading } = useQuery<User>({
    queryKey: ["currentUser"],
    queryFn: async () => {
      const res = await api.get("/auth/me");
      return res.data;
    },
    retry: false,
  });
  return { data, isLoading };
};
