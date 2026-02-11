import { useParams } from "react-router-dom";

export const useMemberId = () => {
  const params = useParams();
  return params.memberId as string;
};
