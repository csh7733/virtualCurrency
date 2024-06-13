// src/components/PrivateRoute.js
import React from 'react';
import { Navigate } from 'react-router-dom';
import useCurrentMember from '../hooks/useCurrentMember';

const PrivateRoute = ({ element }) => {
  const { currentMember, isLoading } = useCurrentMember();

  if (isLoading) {
    return <div>Loading...</div>; // 로딩 상태 처리
  }

  return currentMember ? element : <Navigate to="/login" />;
};

export default PrivateRoute;
