import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Loader, TriangleAlert } from "lucide-react";

import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { useGetWorkspace } from "@/features/workspaces/api/use-get-workspace";
import { useGetChannels } from "@/features/channels/api/use-get-channels";
import { useCurrentMember } from "@/features/members/api/use-current-member";
import { useCreateChannelModal } from "@/features/channels/store/use-create-channel-modal";

const WorkspacePage = () => {
  const navigate = useNavigate();
  const workspaceId = useWorkspaceId();

  const { data: workspace, isLoading: workspaceLoading } = useGetWorkspace({
    id: workspaceId,
  });
  const { data: channels, isLoading: channelsLoading } = useGetChannels({
    workspaceId,
  });
  const { data: member, isLoading: memberLoading } = useCurrentMember({
    workspaceId,
  });

  const [open, setOpen] = useCreateChannelModal();

  const channelId = useMemo(() => channels?.[0]?.id, [channels]);
  const isAdmin = useMemo(() => member?.role === "ADMIN", [member?.role]);

  useEffect(() => {
    if (workspaceLoading || channelsLoading || memberLoading || !member || !workspace) return;

    if (channelId) {
      navigate(`/workspace/${workspaceId}/channel/${channelId}`, { replace: true });
    } else if (!open && isAdmin) {
      setOpen(true);
    }
  }, [
    member,
    channelId,
    isAdmin,
    workspaceLoading,
    channelsLoading,
    memberLoading,
    workspace,
    open,
    setOpen,
    navigate,
    workspaceId,
  ]);

  if (workspaceLoading || channelsLoading || memberLoading) {
    return (
      <div className="h-full flex-1 flex items-center justify-center flex-col gap-2">
        <Loader className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  if (!workspace || !member) {
    return (
      <div className="h-full flex-1 flex items-center justify-center flex-col gap-2">
        <TriangleAlert className="size-6 text-muted-foreground" />
        <span className="text-sm text-muted-foreground">
          Workspace not found
        </span>
      </div>
    );
  }

  return (
    <div className="h-full flex-1 flex items-center justify-center flex-col gap-2">
      <TriangleAlert className="size-6 text-muted-foreground" />
      <span className="text-sm text-muted-foreground">
        No channel found
      </span>
    </div>
  );
};

export default WorkspacePage;
