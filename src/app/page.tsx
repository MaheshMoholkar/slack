"use client";

import { UserButton } from "@/features/auth/components/user-button";

export default function Home() {
  return (
    <div className="flex flex-col items-center justify-center min-h-screen py-2">
      <h1 className="text-4xl font-bold">Welcome to Convex</h1>
      <UserButton />
    </div>
  );
}
