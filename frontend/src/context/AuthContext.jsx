import {
    createContext,
    useContext,
    useEffect,
    useState
} from "react";

import api from "../services/api.js";
import authService from "../services/authService";

const AuthContext = createContext(null);

export function AuthProvider({ children }) {

    const [user, setUser] = useState(null);
    const [loading, setLoading] = useState(true);

    // Validate existing JWT when application starts
    useEffect(() => {

        const validateSession = async () => {

            const token = authService.getToken();

            if (!token) {
                setLoading(false);
                return;
            }

            try {

                const response = await api.get("/auth/me");

                setUser(response.data);

            } catch (error) {

                authService.logout();
                setUser(null);

            } finally {

                setLoading(false);

            }

        };

        validateSession();

    }, []);

    // Login
    const login = async (credentials) => {

        await authService.login(credentials);

        const response = await api.get("/auth/me");

        setUser(response.data);

        return response.data;
    };

    // Logout
    const logout = () => {

        authService.logout();

        setUser(null);

    };

    const value = {
        user,
        loading,
        isAuthenticated: !!user,
        login,
        logout
    };

    return (

        <AuthContext.Provider value={value}>
            {children}
        </AuthContext.Provider>

    );

}

// Custom hook
export function useAuth() {

    const context = useContext(AuthContext);

    if (!context) {
        throw new Error(
            "useAuth must be used inside AuthProvider"
        );
    }

    return context;
}