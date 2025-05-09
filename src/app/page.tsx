"use client";

import { UserButton } from "@/features/auth/components/user-button";
import { useAuthActions } from "@convex-dev/auth/react";

export default function Home() {
  const { signOut } = useAuthActions();
  return (
    <div className="flex flex-col items-center justify-center min-h-screen py-2">
      <h1 className="text-4xl font-bold">Welcome to Convex</h1>
      <UserButton />
      <button
        onClick={() => signOut()}
        className="mt-6 px-4 py-2 text-white bg-blue-600 rounded"
      >
        Sign Out
      </button>
    </div>
  );
}
