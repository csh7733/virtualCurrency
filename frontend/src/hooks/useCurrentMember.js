// src/hooks/useCurrentMember.js
import useSWR from "swr";
import apiClient from "../apiClient";

const useCurrentMember = () => {
  const apiUrl = "http://localhost:8080/api/member";

  // Fetcher 함수 정의
  const fetcher = async (url) => {
    const token = localStorage.getItem('token');
    if (!token) {
      // 토큰이 없으면 에러를 던집니다.
      throw new Error("No token available");
    }
    const response = await apiClient.get(url);
    return response.data;
  };

  // useSWR 훅 사용
  const { data, error, mutate } = useSWR(
    apiUrl,
    fetcher,
    {
      shouldRetryOnError: false, // 에러 발생 시 재시도 하지 않도록 설정
      revalidateOnFocus: false,  // 포커스 시 재검증 비활성화
    }
  );

  return {
    currentMember: data,
    isLoading: !error && !data,
    error,
    currentUserMutate: mutate,
  };
};

export default useCurrentMember;
