// Types matching Spring Boot entities

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
  _id: string;
  id: string;
  name: string;
  workspaceId: string;
  createdAt?: string | Date;
}

export interface Member {
  id: string;
  role: "ADMIN" | "MEMBER";
  user: User;
  workspaceId?: string;
}

export interface Message {
  id: string;
  body: string;
  imageId?: string;
  memberId: string;
  workspaceId: string;
  channelId?: string;
  conversationId?: string;
  parentMessageId?: string;
  createdAt: number;
  updatedAt?: number;
  // Populated fields from API
  member?: Member;
  image?: string | null;
  user: {
    id: string;
    name: string;
    image?: string;
  };
  reactions: ReactionGroup[];
  threadCount?: number;
  threadImage?: string;
  threadName?: string;
  threadTimestamp?: number;
}

export interface Reaction {
  id: string;
  value: string;
  workspaceId: string;
  messageId: string;
  memberId: string;
}

export interface ReactionGroup {
  id: string;
  value: string;
  count: number;
  memberIds: string[];
}

export interface Conversation {
  id: string;
  memberOneId: string;
  memberTwoId: string;
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
