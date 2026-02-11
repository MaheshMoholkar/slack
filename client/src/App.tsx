import { BrowserRouter, Routes, Route, Navigate, Outlet } from "react-router-dom";
import { NuqsAdapter } from "nuqs/adapters/react-router/v7";
import { Toaster } from "@/components/ui/sonner";

import { AuthProvider, useAuth } from "@/providers/auth-provider";
import { QueryProvider } from "@/providers/query-provider";
import { WebSocketProvider } from "@/providers/websocket-provider";
import { JotaiProvider } from "@/providers/jotai-provider";
import { CreateWorkspaceModal } from "@/features/workspaces/components/create-workspace-modal";
import { CreateChannelModal } from "@/features/channels/components/create-channel-modal";

import AuthPage from "@/pages/auth/auth-page";
import HomePage from "@/pages/home-page";
import JoinPage from "@/pages/join/join-page";
import WorkspaceLayout from "@/pages/workspace/workspace-layout";
import WorkspacePage from "@/pages/workspace/workspace-page";
import ChannelPage from "@/pages/workspace/channel/channel-page";
import MemberPage from "@/pages/workspace/member/member-page";

// Auth guard component
const ProtectedRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="h-full flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
      </div>
    );
  }

  if (!user) {
    return <Navigate to="/auth" replace />;
  }

  return <>{children}</>;
};

// Redirect authenticated users away from auth page
const AuthRoute = ({ children }: { children: React.ReactNode }) => {
  const { user, isLoading } = useAuth();

  if (isLoading) {
    return (
      <div className="h-full flex items-center justify-center">
        <div className="animate-spin rounded-full h-8 w-8 border-b-2 border-gray-900" />
      </div>
    );
  }

  if (user) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

function AppRoutes() {
  return (
    <Routes>
      <Route
        path="/auth"
        element={
          <AuthRoute>
            <AuthPage />
          </AuthRoute>
        }
      />
      <Route
        path="/"
        element={
          <ProtectedRoute>
            <HomePage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/join/:workspaceId/:joinCode?"
        element={
          <ProtectedRoute>
            <JoinPage />
          </ProtectedRoute>
        }
      />
      <Route
        path="/workspace/:workspaceId"
        element={
          <ProtectedRoute>
            <WorkspaceLayout />
          </ProtectedRoute>
        }
      >
        <Route index element={<WorkspacePage />} />
        <Route path="channel/:channelId" element={<ChannelPage />} />
        <Route path="member/:memberId" element={<MemberPage />} />
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  );
}

function App() {
  return (
    <BrowserRouter>
      <NuqsAdapter>
        <QueryProvider>
          <AuthProvider>
            <JotaiProvider>
      <WebSocketProvider>
        <CreateWorkspaceModal />
        <CreateChannelModal />
        <Toaster />
        <AppRoutes />
      </WebSocketProvider>
            </JotaiProvider>
          </AuthProvider>
        </QueryProvider>
      </NuqsAdapter>
    </BrowserRouter>
  );
}

export default App;
