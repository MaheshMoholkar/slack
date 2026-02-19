import {
  Bell,
  Bookmark,
  CalendarClock,
  LucideIcon,
  Megaphone,
} from "lucide-react";
import { UserButton } from "@/features/auth/components/user-button";
import { WorkspaceSwitcher } from "./workspace-switcher";

type FakeSidebarIcon = {
  id: string;
  label: string;
  icon: LucideIcon;
  color: string;
};

const FAKE_SIDEBAR_ICONS: FakeSidebarIcon[] = [
  {
    id: "announcements",
    label: "Announcements",
    icon: Megaphone,
    color: "bg-[#0f7b5f]",
  },
  {
    id: "bookmarks",
    label: "Bookmarks",
    icon: Bookmark,
    color: "bg-[#1264a3]",
  },
  {
    id: "calendar",
    label: "Calendar",
    icon: CalendarClock,
    color: "bg-[#7c3aed]",
  },
  {
    id: "notifications",
    label: "Notifications",
    icon: Bell,
    color: "bg-[#b45309]",
  },
];

export const Sidebar = () => {
  return (
    <aside className="w-[70px] h-full bg-[#481349] flex flex-col gap-y-4 items-center pt-[9px] pb-4">
      <WorkspaceSwitcher />
      <div className="flex flex-col items-center gap-y-2">
        {FAKE_SIDEBAR_ICONS.map((item) => (
          <div
            key={item.id}
            title={item.label}
            aria-hidden="true"
            className={`size-9 rounded-lg ${item.color} text-white flex items-center justify-center opacity-90`}
          >
            <item.icon className="size-4" />
          </div>
        ))}
      </div>
      <div className="flex flex-col items-center justify-center gap-y-1 mt-auto">
        <UserButton />
      </div>
    </aside>
  );
};
