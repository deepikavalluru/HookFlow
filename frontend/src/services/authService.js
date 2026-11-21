import api from "./api.js";

const authService = {

    async login(credentials) {
        const response = await api.post("/auth/login", credentials);

        const { accessToken, tokenType } = response.data;

        localStorage.setItem("accessToken", accessToken);
        localStorage.setItem("tokenType", tokenType);

        return response.data;
    },

    logout() {
        localStorage.removeItem("accessToken");
        localStorage.removeItem("tokenType");
    },

    getToken() {
        return localStorage.getItem("accessToken");
    },

    isAuthenticated() {
        return !!localStorage.getItem("accessToken");
    },

    async register(userData) {
        const response = await api.post("/auth/register", userData);

        return response.data;
    },

};

export default authService;