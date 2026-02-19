import { useState } from "react";

import { SignInFlow } from "@/features/auth/types";
import { SignInCard } from "./sign-in-card";
import { SignUpCard } from "./sign-up-card";

export const AuthScreen = () => {
  const [state, setState] = useState<SignInFlow>("signIn");

  return (
    <div className="h-full flex items-center justify-center bg-[#5C3B58] px-4">
      <div className="w-full md:w-[420px]">
        <div className="mb-5 text-center text-white">
          <div className="mx-auto mb-3 flex h-14 w-14 items-center justify-center rounded-2xl bg-white/15 backdrop-blur-sm">
            <img src="/slack.png" alt="Slack logo" className="h-8 w-8" />
          </div>
          <h1 className="text-2xl font-semibold tracking-tight">Slack</h1>
          <p className="mt-1 text-sm text-white/80">
            Team messaging for demos and collaboration
          </p>
        </div>
        {state === "signIn" ? (
          <SignInCard setState={setState} />
        ) : (
          <SignUpCard setState={setState} />
        )}
      </div>
    </div>
  );
};
