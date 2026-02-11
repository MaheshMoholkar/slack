import { useEffect, useMemo, useState } from "react";
import { Link, useNavigate, useParams } from "react-router-dom";
import { Loader } from "lucide-react";
import { toast } from "sonner";

import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";

import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { useGetWorkspaceInfo } from "@/features/workspaces/api/use-get-workspace-info";
import { useJoin } from "@/features/workspaces/api/use-join";

const JoinPage = () => {
  const navigate = useNavigate();
  const workspaceId = useWorkspaceId();
  const { joinCode } = useParams<{ joinCode?: string }>();

  const { data, isLoading } = useGetWorkspaceInfo({ id: workspaceId });
  const { mutate, isPending } = useJoin();

  const [value, setValue] = useState(joinCode?.toUpperCase() || "");

  const isMember = useMemo(() => data?.isMember, [data?.isMember]);

  useEffect(() => {
    if (isMember) {
      navigate(`/workspace/${workspaceId}`, { replace: true });
    }
  }, [isMember, workspaceId, navigate]);

  // Auto-submit if join code is provided in URL and valid
  useEffect(() => {
    if (joinCode && joinCode.length === 6 && !isPending && !isMember && !isLoading) {
      handleComplete();
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [joinCode, isLoading, isMember]);

  const handleComplete = () => {
    mutate(
      { workspaceId, joinCode: value },
      {
        onSuccess: () => {
          navigate(`/workspace/${workspaceId}`, { replace: true });
          toast.success("Workspace joined");
        },
        onError: () => {
          toast.error("Failed to join workspace");
        },
      }
    );
  };

  if (isLoading) {
    return (
      <div className="h-full flex items-center justify-center">
        <Loader className="size-6 animate-spin text-muted-foreground" />
      </div>
    );
  }

  return (
    <div className="h-full flex flex-col gap-y-8 items-center justify-center bg-white p-8 rounded-lg shadow-md">
      <img src="/logo.svg" alt="Logo" className="size-14" />
      <div className="flex flex-col gap-y-4 items-center justify-center max-w-md">
        <div className="flex flex-col gap-y-2 items-center justify-center">
          <h1 className="text-2xl font-bold">Join {data?.name}</h1>
          <p className="text-md text-muted-foreground">
            Enter the workspace code to join
          </p>
        </div>
        <Input
          value={value}
          onChange={(e) => setValue(e.target.value.toUpperCase())}
          maxLength={6}
          disabled={isPending}
          placeholder="Enter 6-character code"
          className="text-center text-2xl tracking-[0.5em] font-mono uppercase"
          onKeyDown={(e) => {
            if (e.key === "Enter" && value.length === 6) {
              handleComplete();
            }
          }}
        />
        <div className="flex gap-x-4">
          <Button size="lg" variant="outline" asChild>
            <Link to="/">Back to home</Link>
          </Button>
          <Button
            size="lg"
            onClick={handleComplete}
            disabled={isPending || value.length !== 6}
          >
            Join Workspace
          </Button>
        </div>
      </div>
    </div>
  );
};

export default JoinPage;
