import api from "./api";

const eventService = {
    getAllEvents: async () => {
        const response = await api.get("/events");
        return response.data;
    },

    getEventById: async (id) => {
        const response = await api.get(`/events/${id}`);
        return response.data;
    },

    createEvent: async (eventData) => {
        const response = await api.post("/events", eventData);
        return response.data;
    },

    getAllDeliveries: async () => {
        const response = await api.get("/deliveries");
        return response.data;
    },
};

export default eventService;