import React, { Suspense } from "react";
import { Outlet } from "react-router-dom";
import { Loader } from "lucide-react";

import {
  ResizableHandle,
  ResizablePanel,
  ResizablePanelGroup,
} from "@/components/ui/resizable";

import { usePanel } from "@/hooks/use-panel";

import { Sidebar } from "./sidebar";
import { Toolbar } from "./toolbar";
import { WorkspaceSidebar } from "./workspace-sidebar";

const Thread = React.lazy(() =>
  import("@/features/messages/components/thread").then((m) => ({
    default: m.Thread,
  }))
);
const Profile = React.lazy(() =>
  import("@/features/members/components/profile").then((m) => ({
    default: m.Profile,
  }))
);

const WorkspaceLayout = () => {
  const { parentMessageId, profileMemberId, onClose } = usePanel();

  const showPanel = !!parentMessageId || !!profileMemberId;

  return (
    <div className="h-full">
      <Toolbar />
      <div className="flex h-[calc(100vh-40px)]">
        <Sidebar />
        <ResizablePanelGroup
          direction="horizontal"
          autoSaveId="ca-workspace-layout"
        >
          <ResizablePanel
            defaultSize={20}
            minSize={11}
            className="bg-[#5E2C5F]"
          >
            <WorkspaceSidebar />
          </ResizablePanel>
          <ResizableHandle withHandle />
          <ResizablePanel minSize={20} defaultSize={80}>
            <Outlet />
          </ResizablePanel>
          {showPanel && (
            <>
              <ResizableHandle withHandle />
              <ResizablePanel minSize={20} defaultSize={29}>
                <Suspense
                  fallback={
                    <div className="flex h-full items-center justify-center">
                      <Loader className="size-5 animate-spin text-muted-foreground" />
                    </div>
                  }
                >
                  {parentMessageId ? (
                    <Thread
                      messageId={parentMessageId}
                      onClose={onClose}
                    />
                  ) : profileMemberId ? (
                    <Profile
                      memberId={profileMemberId}
                      onClose={onClose}
                    />
                  ) : null}
                </Suspense>
              </ResizablePanel>
            </>
          )}
        </ResizablePanelGroup>
      </div>
    </div>
  );
};

export default WorkspaceLayout;
