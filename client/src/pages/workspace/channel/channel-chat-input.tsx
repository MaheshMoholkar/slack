import { useCallback, useRef, useState } from "react";
import Quill from "quill";
import { toast } from "sonner";

import { useCreateMessage } from "@/features/messages/api/use-create-message";
import { useCurrentMember } from "@/features/members/api/use-current-member";

import { useChannelId } from "@/hooks/use-channel-id";
import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { useWebSocket } from "@/providers/websocket-provider";
import { useAuth } from "@/providers/auth-provider";

import api from "@/lib/api";
import Editor from "@/components/editor";

interface ChannelChatInputProps {
  placeholder: string;
}

type CreateMessageValues = {
  channelId: string;
  workspaceId: string;
  memberId: string;
  body: string;
  imageId?: string;
};

export const ChannelChatInput = ({ placeholder }: ChannelChatInputProps) => {
  const [editorKey, setEditorKey] = useState(0);
  const [isPending, setIsPending] = useState(false);

  const editorRef = useRef<Quill | null>(null);

  const channelId = useChannelId();
  const workspaceId = useWorkspaceId();
  const { data: currentMember } = useCurrentMember({ workspaceId });
  const { mutate: createMessage } = useCreateMessage();
  const { publish } = useWebSocket();
  const { user } = useAuth();

  const typingTimeoutRef = useRef<ReturnType<typeof setTimeout> | null>(null);
  const isTypingRef = useRef(false);

  const handleTyping = useCallback(() => {
    if (!user) return;

    if (!isTypingRef.current) {
      isTypingRef.current = true;
      publish("/app/typing", {
        workspaceId,
        channelId,
        userId: user.id,
        typing: true,
      });
    }

    if (typingTimeoutRef.current) {
      clearTimeout(typingTimeoutRef.current);
    }

    typingTimeoutRef.current = setTimeout(() => {
      isTypingRef.current = false;
      publish("/app/typing", {
        workspaceId,
        channelId,
        userId: user.id,
        typing: false,
      });
    }, 2000);
  }, [workspaceId, channelId, user, publish]);

  const handleSubmit = async ({
    body,
    image,
  }: {
    body: string;
    image: File | null;
  }) => {
    try {
      setIsPending(true);
      editorRef.current?.enable(false);

      const values: CreateMessageValues = {
        channelId,
        workspaceId,
        memberId: currentMember!.id,
        body,
        imageId: undefined,
      };

      if (image) {
        const formData = new FormData();
        formData.append("file", image);
        const uploadRes = await api.post("/upload", formData, {
          headers: { "Content-Type": "multipart/form-data" },
        });
        values.imageId = uploadRes.data?.storageId ?? uploadRes.data;
      }

      await createMessage(values, {
        throwError: true,
      });

      setEditorKey((prevKey) => prevKey + 1);
    } catch {
      toast.error("Failed to send message");
    } finally {
      setIsPending(false);
      editorRef.current?.enable(true);
    }
  };

  return (
    <div className="px-5 w-full">
      <Editor
        key={editorKey}
        placeholder={placeholder}
        onSubmit={handleSubmit}
        onTyping={handleTyping}
        disabled={isPending}
        innerRef={editorRef}
      />
    </div>
  );
};
