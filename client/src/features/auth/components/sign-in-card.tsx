import { useState } from "react";
import axios from "axios";
import { useAuth } from "@/providers/auth-provider";
import { SignInFlow } from "@/features/auth/types";

import { Button } from "@/components/ui/button";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { Input } from "@/components/ui/input";
import { Separator } from "@/components/ui/separator";
import { TriangleAlert } from "lucide-react";

interface SignInCardProps {
  setState: (state: SignInFlow) => void;
}

const GUEST_LOGINS = [
  {
    label: "Continue as Guest User 1",
    email: "guest.user1@slack-app.dev",
    password: "GuestUser@123",
  },
  {
    label: "Continue as Guest User 2",
    email: "guest.user2@slack-app.dev",
    password: "GuestUser@123",
  },
] as const;

export const SignInCard = ({ setState }: SignInCardProps) => {
  const { login } = useAuth();

  const [email, setEmail] = useState("");
  const [password, setPassword] = useState("");
  const [error, setError] = useState("");
  const [pending, setPending] = useState(false);

  const getLoginErrorMessage = (err: unknown) => {
    if (axios.isAxiosError(err)) {
      if (err.code === "ECONNABORTED" || !err.response) {
        return "Server is unreachable. Please try again.";
      }
      if (err.response.status === 401) {
        return "Invalid email or password";
      }
      return "Login failed. Please try again.";
    }
    return "Login failed. Please try again.";
  };

  const onPasswordSignIn = async (e: React.FormEvent<HTMLFormElement>) => {
    e.preventDefault();
    setPending(true);
    setError("");

    try {
      await login(email, password);
    } catch (err) {
      setError(getLoginErrorMessage(err));
    } finally {
      setPending(false);
    }
  };

  const onGuestSignIn = async (guestEmail: string, guestPassword: string) => {
    setPending(true);
    setError("");
    setEmail(guestEmail);
    setPassword(guestPassword);

    try {
      await login(guestEmail, guestPassword);
    } catch (err) {
      setError(getLoginErrorMessage(err));
    } finally {
      setPending(false);
    }
  };

  return (
    <Card className="w-full h-full p-8">
      <CardHeader className="px-0 pt-0">
        <CardTitle>Login to continue</CardTitle>
        <CardDescription>
          Use your email and password to continue
        </CardDescription>
      </CardHeader>
      {!!error && (
        <div className="bg-destructive/15 p-3 rounded-md flex items-center gap-x-2 text-sm text-destructive mb-6">
          <TriangleAlert className="size-4" />
          <p>{error}</p>
        </div>
      )}
      <CardContent className="space-y-5 px-0 pb-0">
        <form onSubmit={onPasswordSignIn} className="space-y-2.5">
          <Input
            disabled={pending}
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            placeholder="Email"
            type="email"
            required
          />
          <Input
            disabled={pending}
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            placeholder="Password"
            type="password"
            required
          />
          <Button type="submit" className="w-full" size="lg" disabled={pending}>
            Continue
          </Button>
          <div className="space-y-2 pt-1">
            {GUEST_LOGINS.map((guest) => (
              <Button
                key={guest.email}
                type="button"
                variant="outline"
                className="w-full"
                disabled={pending}
                onClick={() => void onGuestSignIn(guest.email, guest.password)}
              >
                {guest.label}
              </Button>
            ))}
          </div>
        </form>
        <Separator />
        <p className="text-xs text-muted-foreground">
          Don&apos;t have an account?{" "}
          <span
            onClick={() => setState("signUp")}
            className="text-sky-700 hover:underline cursor-pointer"
          >
            Sign up
          </span>
        </p>
      </CardContent>
    </Card>
  );
};
