import api from "./api";

// Get all webhooks
const getAllWebhooks = async () => {
    const response = await api.get("/webhooks");
    return response.data;
};

// Get webhook by ID
const getWebhookById = async (id) => {
    const response = await api.get(`/webhooks/${id}`);
    return response.data;
};

// Create webhook
const createWebhook = async (webhookData) => {
    const response = await api.post("/webhooks", webhookData);
    return response.data;
};

// Update webhook
const updateWebhook = async (id, webhookData) => {
    const response = await api.put(`/webhooks/${id}`, webhookData);
    return response.data;
};

// Activate / Deactivate webhook
const updateWebhookStatus = async (id, active) => {
    const response = await api.patch(`/webhooks/${id}/status`, {
        active,
    });

    return response.data;
};

// Delete webhook
const deleteWebhook = async (id) => {
    const response = await api.delete(`/webhooks/${id}`);
    return response.data;
};

const webhookService = {
    getAllWebhooks,
    getWebhookById,
    createWebhook,
    updateWebhook,
    updateWebhookStatus,
    deleteWebhook,
};

export default webhookService;