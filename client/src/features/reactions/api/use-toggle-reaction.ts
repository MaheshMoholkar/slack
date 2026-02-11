import { useCallback, useMemo, useState } from "react";
import { useQueryClient } from "@tanstack/react-query";
import api from "@/lib/api";
import { useCurrentMember } from "@/features/members/api/use-current-member";
import { useWorkspaceId } from "@/hooks/use-workspace-id";

type RequestType = {
  messageId: string;
  value: string;
};

type Options = {
  onSuccess?: (data: string) => void;
  onError?: (error: Error) => void;
  onSettled?: () => void;
  throwError?: boolean;
};

export const useToggleReaction = () => {
  const queryClient = useQueryClient();
  const workspaceId = useWorkspaceId();
  const { data: currentMember } = useCurrentMember({ workspaceId });
  const [data, setData] = useState<string | null>(null);
  const [error, setError] = useState<Error | null>(null);
  const [status, setStatus] = useState<
    "success" | "error" | "settled" | "pending" | null
  >(null);

  const isPending = useMemo(() => status === "pending", [status]);
  const isSuccess = useMemo(() => status === "success", [status]);
  const isError = useMemo(() => status === "error", [status]);
  const isSettled = useMemo(() => status === "settled", [status]);

  const mutate = useCallback(
    async (values: RequestType, options?: Options) => {
      try {
        setData(null);
        setError(null);
        setStatus("pending");

        const memberId = currentMember?.id;
        if (!memberId) throw new Error("Not a member of this workspace");

        // First check existing reactions to determine add vs remove
        const reactionsRes = await api.get(
          `/reactions/message/${values.messageId}`
        );
        const existingReactions: Array<{
          id: string;
          value: string;
          member?: { id: string };
          memberId?: string;
        }> = reactionsRes.data || [];

        const existingReaction = existingReactions.find(
          (r) =>
            r.value === values.value &&
            ((r.member?.id || r.memberId) === memberId)
        );

        let result: string;
        if (existingReaction) {
          // Remove existing reaction
          await api.delete("/reactions", {
            data: {
              messageId: values.messageId,
              memberId,
              value: values.value,
            },
          });
          result = existingReaction.id;
        } else {
          // Add new reaction
          const response = await api.post("/reactions", {
            messageId: values.messageId,
            memberId,
            value: values.value,
          });
          result = response.data?.id ?? response.data;
        }

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
        setStatus("settled");
        options?.onSettled?.();
        queryClient.invalidateQueries({ queryKey: ["messages"] });
        queryClient.invalidateQueries({ queryKey: ["message"] });
      }
    },
    [queryClient, currentMember]
  );

  return { mutate, data, error, isPending, isSuccess, isError, isSettled };
};
