import { Link } from "react-router-dom";
import { LucideIcon } from "lucide-react";
import { IconType } from "react-icons/lib";
import { cva, type VariantProps } from "class-variance-authority";

import { Button } from "@/components/ui/button";
import { Hint } from "@/components/hint";
import { useWorkspaceId } from "@/hooks/use-workspace-id";
import { cn } from "@/lib/utils";

const sidebarItemVariants = cva(
  "flex items-center gap-1.5 justify-start font-normal h-7 px-[18px] text-sm overflow-hidden",
  {
    variants: {
      variant: {
        default: "text-[#f9edffcc]",
        active: "text-[#481349] bg-white/90 hover:bg-white/90",
      },
    },
    defaultVariants: {
      variant: "default",
    },
  }
);

interface SidebarItemProps {
  label: string;
  id: string;
  icon: LucideIcon | IconType;
  variant?: VariantProps<typeof sidebarItemVariants>["variant"];
  disabled?: boolean;
  unavailableHint?: string;
}

export const SidebarItem = ({
  label,
  id,
  icon: Icon,
  variant,
  disabled = false,
  unavailableHint,
}: SidebarItemProps) => {
  const workspaceId = useWorkspaceId();

  if (disabled) {
    return (
      <Hint label={unavailableHint ?? `${label} is not available`}>
        <Button
          type="button"
          variant="transparent"
          size="sm"
          aria-disabled
          className={cn(
            sidebarItemVariants({ variant }),
            "w-full opacity-60 cursor-not-allowed hover:bg-transparent"
          )}
        >
          <Icon className="size-3.5 mr-1 shrink-0" />
          <span className="text-sm truncate">{label}</span>
        </Button>
      </Hint>
    );
  }

  return (
    <Button
      variant="transparent"
      size="sm"
      className={cn(sidebarItemVariants({ variant }))}
      asChild
    >
      <Link to={`/workspace/${workspaceId}/channel/${id}`}>
        <Icon className="size-3.5 mr-1 shrink-0" />
        <span className="text-sm truncate">{label}</span>
      </Link>
    </Button>
  );
};
