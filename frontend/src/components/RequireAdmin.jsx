import { Navigate, Outlet } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';

// UX-only guard. Real enforcement is on the server (@PreAuthorize + count stripping).
export default function RequireAdmin({ children }) {
  const { isAuthenticated, isAdmin } = useAuth();
  if (!isAuthenticated) return <Navigate to="/login" replace />;
  if (!isAdmin) return <Navigate to="/elections" replace />;
  return children ?? <Outlet />;
}