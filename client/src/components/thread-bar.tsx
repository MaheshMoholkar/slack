import { ChevronRight } from "lucide-react";
import { formatDistanceToNow } from "date-fns";

import { cn } from "@/lib/utils";
import { Hint } from "@/components/hint";
import { Avatar, AvatarImage, AvatarFallback } from "@/components/ui/avatar";

interface ThreadBarProps {
  count?: number;
  image?: string;
  timestamp?: number;
  name?: string;
  onClick?: () => void;
  disabled?: boolean;
  unavailableHint?: string;
}

export const ThreadBar = ({
  count,
  image,
  name = "Member",
  timestamp,
  onClick,
  disabled = false,
  unavailableHint,
}: ThreadBarProps) => {
  const avatarFallback = name.charAt(0).toUpperCase();

  if (!count || !timestamp) return null;

  const threadBar = (
    <button
      type="button"
      onClick={disabled ? undefined : onClick}
      aria-disabled={disabled || undefined}
      className={cn(
        "p-1 rounded-md border border-transparent flex items-center justify-start max-w-[600px]",
        disabled
          ? "opacity-60 cursor-not-allowed bg-slate-50"
          : "hover:bg-white hover:border-border group/thread-bar transition"
      )}
    >
      <div className="flex items-center gap-2 overflow-hidden">
        <Avatar className="size-6 shrink-0">
          <AvatarImage src={image} />
          <AvatarFallback>{avatarFallback}</AvatarFallback>
        </Avatar>
        <span className="text-xs text-sky-700 hover:underline font-bold truncate">
          {count} {count > 1 ? "replies" : "reply"}
        </span>
        {disabled ? (
          <span className="text-xs text-muted-foreground truncate">
            Thread unavailable
          </span>
        ) : (
          <>
            <span className="text-xs text-muted-foreground truncate group-hover/thread-bar:hidden block">
              Last reply {formatDistanceToNow(timestamp, { addSuffix: true })}
            </span>
            <span className="text-xs text-muted-foreground truncate group-hover/thread-bar:block hidden">
              View thread
            </span>
          </>
        )}
      </div>
      {!disabled && (
        <ChevronRight className="size-4 text-muted-foreground ml-auto opacity-0 group-hover/thread-bar:opacity-100 transition shrink-0" />
      )}
    </button>
  );

  if (disabled) {
    return (
      <Hint label={unavailableHint ?? "Threads are not available yet"}>
        {threadBar}
      </Hint>
    );
  }

  return threadBar;
};
