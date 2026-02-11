import { useParams } from "react-router-dom";

export const useWorkspaceId = () => {
  const params = useParams();
  return params.workspaceId as string;
};
