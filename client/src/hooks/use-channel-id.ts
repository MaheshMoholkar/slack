import { useParams } from "react-router-dom";

export const useChannelId = () => {
  const params = useParams();
  return params.channelId as string;
};
