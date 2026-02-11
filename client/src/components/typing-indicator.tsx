interface TypingIndicatorProps {
  typingUsers: string[];
}

export const TypingIndicator = ({ typingUsers }: TypingIndicatorProps) => {
  if (typingUsers.length === 0) return null;

  let text: string;
  if (typingUsers.length === 1) {
    text = `${typingUsers[0]} is typing`;
  } else if (typingUsers.length === 2) {
    text = `${typingUsers[0]} and ${typingUsers[1]} are typing`;
  } else {
    text = "Several people are typing";
  }

  return (
    <div className="px-5 pb-1 text-xs text-muted-foreground flex items-center gap-1 h-6">
      <span className="flex gap-0.5">
        <span className="size-1.5 bg-muted-foreground/70 rounded-full animate-bounce [animation-delay:0ms]" />
        <span className="size-1.5 bg-muted-foreground/70 rounded-full animate-bounce [animation-delay:150ms]" />
        <span className="size-1.5 bg-muted-foreground/70 rounded-full animate-bounce [animation-delay:300ms]" />
      </span>
      <span>{text}</span>
    </div>
  );
};
