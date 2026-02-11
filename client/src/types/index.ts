// Types matching Spring Boot DTOs exactly

export interface User {
  id: string;
  name: string;
  email: string;
  imageUrl?: string;
}

export interface Workspace {
  id: string;
  name: string;
  userId: string;
  joinCode: string;
}

export interface Channel {
  id: string;
  name: string;
  workspaceId: string;
  createdAt?: string;
}

export interface Member {
  id: string;
  role: "ADMIN" | "MEMBER";
  user: User;
  workspace: Workspace;
}

export interface Message {
  id: string;
  body: string;
  imageId?: string;
  member: {
    id: string;
    role: "ADMIN" | "MEMBER";
    user: {
      id: string;
      name: string;
      imageUrl?: string;
    };
  };
  workspaceId: string;
  channelId?: string;
  conversationId?: string;
  parentMessageId?: string;
  reactions: Reaction[];
  createdAt: number;
  updatedAt?: number;
}

export interface Reaction {
  id: string;
  value: string;
  workspaceId: string;
  messageId: string;
  member: {
    id: string;
    userName: string;
    userImage?: string;
  };
}

export interface Conversation {
  id: string;
  memberOne: {
    id: string;
    role: "ADMIN" | "MEMBER";
    user: {
      id: string;
      name: string;
      imageUrl?: string;
    };
  };
  memberTwo: {
    id: string;
    role: "ADMIN" | "MEMBER";
    user: {
      id: string;
      name: string;
      imageUrl?: string;
    };
  };
  workspaceId: string;
}

export interface WorkspaceInfo {
  name: string;
  isMember: boolean;
}

export interface AuthResponse {
  token: string;
  user: User;
}

export interface PaginatedResponse<T> {
  content: T[];
  totalPages: number;
  totalElements: number;
  last: boolean;
  number: number;
}

// WebSocket event types
export type EventType =
  | "MESSAGE_SENT"
  | "MESSAGE_UPDATED"
  | "MESSAGE_DELETED"
  | "CHANNEL_CREATED"
  | "CHANNEL_UPDATED"
  | "CHANNEL_DELETED"
  | "MEMBER_JOINED"
  | "MEMBER_UPDATED"
  | "MEMBER_LEFT"
  | "REACTION_ADDED"
  | "REACTION_REMOVED"
  | "CONVERSATION_CREATED"
  | "CONVERSATION_UPDATED"
  | "CONVERSATION_DELETED"
  | "WORKSPACE_CREATED"
  | "WORKSPACE_UPDATED"
  | "WORKSPACE_DELETED"
  | "PRESENCE_UPDATE"
  | "TYPING_UPDATE";

export interface WebSocketEvent<T = unknown> {
  type: EventType;
  workspaceId: string;
  channelId?: string;
  conversationId?: string;
  payload: T;
}
