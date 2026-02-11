import { useCallback, useMemo, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useAuth } from "@/providers/auth-provider";

type RequestType = {
  workspaceId: string;
  memberId: string;
};

type Options = {
  onSuccess?: (data: string) => void;
  onError?: (error: Error) => void;
  onSettled?: () => void;
  throwError?: boolean;
};

export const useCreateOrGetConversation = () => {
  const queryClient = useQueryClient();
  const { user } = useAuth();
  const [data, setData] = useState<string | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [status, setStatus] = useState<
    "success" | "error" | "settled" | "pending" | null
  >(null);

  const isPending = useMemo(() => status === "pending", [status]);
  const isSuccess = useMemo(() => status === "success", [status]);
  const isError = useMemo(() => status === "error", [status]);
  const isSettled = useMemo(() => status === "success" || status === "error", [status]);

  const mutate = useCallback(
    async (values: RequestType, options?: Options) => {
      try {
        setData(null);
        setError(null);
        setStatus("pending");

        // Backend create-or-get resolves current user from @AuthenticationPrincipal
        // Only need to pass the target memberId and workspaceId
        const response = await api.post("/conversations/create-or-get", {
          workspaceId: values.workspaceId,
          memberId: values.memberId,
        });
        const result = response.data?.id ?? response.data;

        setData(result);
        setStatus("success");
        options?.onSuccess?.(result);
        return result;
      } catch (error) {
        setStatus("error");
        setError(error as Error);
        options?.onError?.(error as Error);
        if (options?.throwError) throw error;
      } finally {
        options?.onSettled?.();
        queryClient.invalidateQueries({ queryKey: ["conversations"] });
      }
    },
    [queryClient, user]
  );

  return { mutate, data, error, isPending, isSuccess, isError, isSettled };
};
