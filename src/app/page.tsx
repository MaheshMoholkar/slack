"use client";

import { useAuthActions } from "@convex-dev/auth/react";

export default function Home() {
  const { signOut } = useAuthActions();
  return (
    <div className="flex flex-col items-center justify-center min-h-screen py-2">
      <h1 className="text-4xl font-bold">Welcome to Convex</h1>
      <p className="mt-4 text-lg">
        This is a simple example of using Convex with Next.js.
      </p>
      <button
        onClick={() => signOut()}
        className="mt-6 px-4 py-2 text-white bg-blue-600 rounded"
      >
        Sign Out
      </button>
    </div>
  );
}
