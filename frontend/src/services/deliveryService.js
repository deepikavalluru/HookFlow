import api from "./api";

const deliveryService = {
    getAllDeliveries: async () => {
        const response = await api.get("/deliveries");
        return response.data;
    },

    getDeliveryById: async (id) => {
        const response = await api.get(`/deliveries/${id}`);
        return response.data;
    },

    getDeliveryAttempts: async (id) => {
        const response = await api.get(`/deliveries/${id}/attempts`);
        return response.data;
    },

    retryDelivery: async (id) => {
        const response = await api.post(`/deliveries/${id}/retry`);
        return response.data;
    },
};

export default deliveryService;