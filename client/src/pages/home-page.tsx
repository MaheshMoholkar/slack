import { useEffect, useMemo } from "react";
import { useNavigate } from "react-router-dom";
import { Loader } from "lucide-react";

import { useGetWorkspaces } from "@/features/workspaces/api/use-get-workspaces";
import { useCreateWorkspaceModal } from "@/features/workspaces/store/use-create-workspace-modal";

const HomePage = () => {
  const navigate = useNavigate();
  const { data: workspaces, isLoading, isFetching } = useGetWorkspaces();
  const [open, setOpen] = useCreateWorkspaceModal();

  const workspaceId = useMemo(() => workspaces?.[0]?.id, [workspaces]);

  useEffect(() => {
    if (isLoading || isFetching) return;

    if (workspaceId) {
      navigate(`/workspace/${workspaceId}`, { replace: true });
    } else if (!open) {
      setOpen(true);
    }
  }, [workspaceId, isLoading, isFetching, open, setOpen, navigate]);

  return (
    <div className="h-full flex items-center justify-center">
      <Loader className="size-6 animate-spin text-muted-foreground" />
    </div>
  );
};

export default HomePage;
