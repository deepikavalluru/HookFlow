import { useEffect, useState } from "react";
import { Plus, RefreshCw } from "lucide-react";
import { useNavigate, useLocation } from "react-router-dom";

import DashboardLayout from "../layouts/DashboardLayout";
import webhookService from "../services/webhookService";
import WebhookTable from "../components/webhooks/WebhookTable";
import DashboardSkeleton from "../components/dashboard/DashboardSkeleton";
import DashboardError from "../components/dashboard/DashboardError";
import WebhookForm from "../components/webhooks/WebhookForm";

function Webhooks() {
    const [webhooks, setWebhooks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");
    const location = useLocation();

    const [showForm, setShowForm] = useState(false);
    const [submitting, setSubmitting] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const navigate = useNavigate();

    // Fetch all webhooks
    const fetchWebhooks = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            const data = await webhookService.getAllWebhooks();

            setWebhooks(data);
            setError("");
        } catch (err) {
            console.error("Webhook API Error:", err);

            setError(
                err.response?.data?.message ||
                "Unable to load webhook endpoints."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    // Initial fetch
    useEffect(() => {
        fetchWebhooks();
    }, []);

    // Automatically dismiss success notification
    useEffect(() => {
        if (!successMessage) return;

        const timer = setTimeout(() => {
            setSuccessMessage("");
        }, 3000);

        return () => clearTimeout(timer);
    }, [successMessage]);

    useEffect(() => {
        if (location.state?.successMessage) {
            setSuccessMessage(location.state.successMessage);

            navigate(location.pathname, {
                replace: true,
                state: {},
            });
        }
    }, [location, navigate]);

    // Navigate to webhook details
    const handleView = (id) => {
        navigate(`/webhooks/${id}`);
    };

    // Register a new webhook
    const handleCreateWebhook = async (webhookData) => {
        try {
            setSubmitting(true);
            setError("");
            setSuccessMessage("");

            const createdWebhook =
                await webhookService.createWebhook(webhookData);

            // Add the newly created webhook immediately
            setWebhooks((previousWebhooks) => [
                createdWebhook,
                ...previousWebhooks,
            ]);

            setShowForm(false);
            setSuccessMessage("Webhook registered successfully!");
        } catch (err) {
            console.error("Webhook Registration Error:", err);

            setError(
                err.response?.data?.message ||
                "Unable to register webhook. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    };

    return (
        <DashboardLayout>
            <div className="space-y-6">

                {/* Page Header */}
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
                    <div>
                        <h1 className="text-2xl font-bold text-gray-300">
                            Webhook Endpoints
                        </h1>

                        <p className="mt-1 text-sm text-gray-500">
                            Manage your registered webhook endpoints.
                        </p>
                    </div>

                    <div className="flex items-center gap-3">

                        {/* Refresh Button */}
                        <button
                            onClick={() => fetchWebhooks(true)}
                            disabled={refreshing}
                            className="inline-flex items-center gap-2 rounded-lg border border-gray-300 bg-white px-4 py-2 text-sm font-medium text-gray-700 transition hover:bg-gray-50 disabled:cursor-not-allowed disabled:opacity-50"
                        >
                            <RefreshCw
                                size={16}
                                className={refreshing ? "animate-spin" : ""}
                            />
                            Refresh
                        </button>

                        {/* Add Webhook Button */}
                        <button
                            onClick={() => {
                                setError("");
                                setShowForm(true);
                            }}
                            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-700"
                        >
                            <Plus size={18} />
                            Add Webhook
                        </button>
                    </div>
                </div>

                {/* Loading State */}
                {loading ? (
                    <DashboardSkeleton />
                ) : error && webhooks.length === 0 ? (
                    <DashboardError
                        message={error}
                        onRetry={() => fetchWebhooks()}
                    />
                ) : (
                    <>
                        {/* Error Message */}
                        {error && (
                            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
                                {error}
                            </div>
                        )}

                        {/* Webhook Table */}
                        <WebhookTable
                            webhooks={webhooks}
                            onView={handleView}
                        />
                    </>
                )}

                {/* Registration Modal */}
                {showForm && (
                    <WebhookForm
                        onSubmit={handleCreateWebhook}
                        onClose={() => setShowForm(false)}
                        submitting={submitting}
                    />
                )}

                {/* Success Notification */}
                {successMessage && (
                    <div className="fixed bottom-6 right-6 z-50 rounded-lg bg-green-600 px-5 py-3 text-sm font-medium text-white shadow-lg">
                        {successMessage}
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}

export default Webhooks;