import { Navigate, Outlet } from "react-router-dom";
import { useAuth } from "../context/AuthContext.jsx";

function ProtectedRoute() {

    const { isAuthenticated, loading } = useAuth();

    if (loading) {

        return (
            <div className="min-h-screen bg-slate-950 flex items-center justify-center">

                <div className="text-indigo-400 text-lg">
                    Checking authentication...
                </div>

            </div>
        );

    }

    if (!isAuthenticated) {

        return <Navigate to="/login" replace />;

    }

    return <Outlet />;

}

export default ProtectedRoute;