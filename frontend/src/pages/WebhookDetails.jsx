import { useEffect, useState } from "react";
import { useParams, useNavigate } from "react-router-dom";

import {
    ArrowLeft,
    Globe,
    Calendar,
    Hash,
    Zap,
    Pencil,
    Trash2,
    Power,
} from "lucide-react";

import DashboardLayout from "../layouts/DashboardLayout";
import webhookService from "../services/webhookService";
import WebhookStatus from "../components/webhooks/WebhookStatus";
import DashboardSkeleton from "../components/dashboard/DashboardSkeleton";
import WebhookEditForm from "../components/webhooks/WebhookEditForm";

function WebhookDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [webhook, setWebhook] = useState(null);

    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");

    const [showEditForm, setShowEditForm] = useState(false);

    const [submitting, setSubmitting] = useState(false);
    const [updatingStatus, setUpdatingStatus] = useState(false);
    const [deleting, setDeleting] = useState(false);
    const [showDeleteModal, setShowDeleteModal] = useState(false);

    // Fetch webhook details
    useEffect(() => {
        const fetchWebhook = async () => {
            try {
                setLoading(true);
                setError("");

                const data = await webhookService.getWebhookById(id);

                setWebhook(data);
            } catch (err) {
                console.error("Webhook Details Error:", err);

                setError(
                    err.response?.data?.message ||
                    "Unable to load webhook details."
                );
            } finally {
                setLoading(false);
            }
        };

        fetchWebhook();
    }, [id]);

    // Automatically dismiss success notification
    useEffect(() => {
        if (!successMessage) return;

        const timer = setTimeout(() => {
            setSuccessMessage("");
        }, 3000);

        return () => clearTimeout(timer);
    }, [successMessage]);

    // Update webhook details
    const handleUpdateWebhook = async (webhookData) => {
        try {
            setSubmitting(true);
            setError("");
            setSuccessMessage("");

            const updatedWebhook = await webhookService.updateWebhook(
                id,
                webhookData
            );

            setWebhook(updatedWebhook);
            setShowEditForm(false);

            setSuccessMessage("Webhook updated successfully!");
        } catch (err) {
            console.error("Webhook Update Error:", err);

            setError(
                err.response?.data?.message ||
                "Unable to update webhook. Please try again."
            );
        } finally {
            setSubmitting(false);
        }
    };

    // Activate / Deactivate webhook
    const handleToggleStatus = async () => {
        try {
            setUpdatingStatus(true);
            setError("");
            setSuccessMessage("");

            const updatedWebhook = await webhookService.updateWebhookStatus(
                id,
                !webhook.active
            );

            setWebhook(updatedWebhook);

            setSuccessMessage(
                updatedWebhook.active
                    ? "Webhook activated successfully!"
                    : "Webhook deactivated successfully!"
            );
        } catch (err) {
            console.error("Webhook Status Error:", err);

            setError(
                err.response?.data?.message ||
                "Unable to update webhook status."
            );
        } finally {
            setUpdatingStatus(false);
        }
    };

    // Delete webhook
    const handleDeleteWebhook = async () => {
        try {
            setDeleting(true);
            setError("");

            await webhookService.deleteWebhook(id);

            navigate("/webhooks", {
                state: {
                    successMessage: "Webhook deleted successfully!",
                },
            });
        } catch (err) {
            console.error("Webhook Delete Error:", err);

            console.error("Delete response:", err.response?.data);
            console.error("HTTP status:", err.response?.status);

            setError(
                err.response?.data?.message ||
                err.response?.data?.error ||
                "Unable to delete webhook. Please try again."
            );

            setShowDeleteModal(false);
        } finally {
            setDeleting(false);
        }
    };

    // Loading state
    if (loading) {
        return (
            <DashboardLayout>
                <DashboardSkeleton />
            </DashboardLayout>
        );
    }

    // Error state
    if (error && !webhook) {
        return (
            <DashboardLayout>
                <div className="rounded-xl border border-red-700 bg-red-900/30 p-6">
                    <p className="text-red-300">{error}</p>

                    <button
                        onClick={() => navigate("/webhooks")}
                        className="mt-4 text-sm font-medium text-indigo-400 hover:text-indigo-300"
                    >
                        Back to Webhooks
                    </button>
                </div>
            </DashboardLayout>
        );
    }

    const formatDate = (date) => {
        return new Date(date).toLocaleString();
    };

    return (
        <DashboardLayout>
            <div className="space-y-6">

                {/* Back Button */}
                <button
                    onClick={() => navigate("/webhooks")}
                    className="flex items-center gap-2 text-sm font-medium text-gray-400 transition hover:text-indigo-400"
                >
                    <ArrowLeft size={18} />
                    Back to Webhooks
                </button>

                {/* Error Notification */}
                {error && (
                    <div className="rounded-lg border border-red-700 bg-red-900/30 px-4 py-3 text-sm text-red-300">
                        {error}
                    </div>
                )}

                {/* Header */}
                <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                    <div>
                        <h1 className="text-2xl font-bold text-gray-300">
                            {webhook.name}
                        </h1>

                        <p className="mt-1 text-sm text-gray-400">
                            Webhook endpoint details
                        </p>
                    </div>

                    <div className="flex flex-wrap items-center gap-3">

                        <WebhookStatus active={webhook.active} />

                        {/* Edit Button */}
                        <button
                            onClick={() => setShowEditForm(true)}
                            disabled={submitting || updatingStatus || deleting}
                            className="inline-flex items-center gap-2 rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-700 disabled:opacity-50"
                        >
                            <Pencil size={16} />
                            Edit
                        </button>

                    </div>
                </div>

                {/* Endpoint Information */}
                <div className="rounded-xl border border-gray-700 bg-gray-900 p-6 shadow-sm">

                    <h2 className="mb-6 text-lg font-semibold text-gray-300">
                        Endpoint Information
                    </h2>

                    <div className="grid grid-cols-1 gap-6 md:grid-cols-2">

                        {/* ID */}
                        <div>
                            <div className="mb-2 flex items-center gap-2 text-sm text-gray-400">
                                <Hash size={16} />
                                Webhook ID
                            </div>

                            <p className="font-medium text-gray-300">
                                {webhook.id}
                            </p>
                        </div>

                        {/* Name */}
                        <div>
                            <div className="mb-2 flex items-center gap-2 text-sm text-gray-400">
                                <Zap size={16} />
                                Name
                            </div>

                            <p className="font-medium text-gray-300">
                                {webhook.name}
                            </p>
                        </div>

                        {/* URL */}
                        <div className="md:col-span-2">
                            <div className="mb-2 flex items-center gap-2 text-sm text-gray-400">
                                <Globe size={16} />
                                Endpoint URL
                            </div>

                            <p className="break-all rounded-lg bg-gray-800 p-3 text-sm text-gray-300">
                                {webhook.url}
                            </p>
                        </div>

                        {/* Event Type */}
                        <div>
                            <div className="mb-2 text-sm text-gray-400">
                                Event Type
                            </div>

                            <span className="inline-flex rounded-md bg-indigo-500/20 px-3 py-1 text-sm font-medium text-indigo-300">
                {webhook.eventType}
              </span>
                        </div>

                        {/* Created At */}
                        <div>
                            <div className="mb-2 flex items-center gap-2 text-sm text-gray-400">
                                <Calendar size={16} />
                                Created At
                            </div>

                            <p className="font-medium text-gray-300">
                                {formatDate(webhook.createdAt)}
                            </p>
                        </div>

                    </div>
                </div>

                {/* Webhook Actions */}
                <div className="rounded-xl border border-gray-700 bg-gray-900 p-6">

                    <h2 className="mb-2 text-lg font-semibold text-gray-300">
                        Webhook Actions
                    </h2>

                    <p className="mb-6 text-sm text-gray-400">
                        Manage the availability and lifecycle of this webhook endpoint.
                    </p>

                    <div className="flex flex-col gap-4 sm:flex-row">

                        {/* Activate / Deactivate */}
                        <button
                            onClick={handleToggleStatus}
                            disabled={updatingStatus || deleting}
                            className={`inline-flex items-center justify-center gap-2 rounded-lg px-5 py-2.5 text-sm font-medium text-white transition disabled:cursor-not-allowed disabled:opacity-50 ${
                                webhook.active
                                    ? "bg-amber-600 hover:bg-amber-700"
                                    : "bg-green-600 hover:bg-green-700"
                            }`}
                        >
                            <Power size={17} />

                            {updatingStatus
                                ? "Updating..."
                                : webhook.active
                                    ? "Deactivate Webhook"
                                    : "Activate Webhook"}
                        </button>

                        {/* Delete */}
                        <button
                            onClick={() => setShowDeleteModal(true)}
                            disabled={deleting || updatingStatus}
                            className="inline-flex items-center justify-center gap-2 rounded-lg border border-red-700 bg-red-900/20 px-5 py-2.5 text-sm font-medium text-red-400 transition hover:bg-red-900/40 disabled:opacity-50"
                        >
                            <Trash2 size={17} />
                            Delete Webhook
                        </button>

                    </div>
                </div>

                {/* Edit Modal */}
                {showEditForm && (
                    <WebhookEditForm
                        webhook={webhook}
                        onSubmit={handleUpdateWebhook}
                        onClose={() => setShowEditForm(false)}
                        submitting={submitting}
                    />
                )}

                {/* Success Notification */}
                {successMessage && (
                    <div className="fixed bottom-6 right-6 z-50 rounded-lg bg-green-600 px-5 py-3 text-sm font-medium text-white shadow-lg">
                        {successMessage}
                    </div>
                )}

                {showDeleteModal && (
                    <div className="fixed inset-0 z-[100] flex items-center justify-center bg-black/70 p-4 backdrop-blur-sm">

                        <div className="w-full max-w-md rounded-2xl border border-gray-700 bg-gray-900 p-6 shadow-2xl">

                            {/* Icon */}
                            <div className="mb-5 flex h-12 w-12 items-center justify-center rounded-full bg-red-500/10">
                                <Trash2 className="text-red-400" size={24} />
                            </div>

                            {/* Heading */}
                            <h2 className="text-xl font-semibold text-gray-200">
                                Delete Webhook?
                            </h2>

                            <p className="mt-3 text-sm leading-6 text-gray-400">
                                Are you sure you want to delete{" "}
                                <span className="font-semibold text-gray-200">
          {webhook.name}
        </span>
                                ?
                            </p>

                            <div className="mt-4 rounded-lg border border-red-800/50 bg-red-900/20 p-3">
                                <p className="text-xs leading-5 text-red-300">
                                    This action cannot be undone. Make sure you no longer need this
                                    webhook endpoint before proceeding.
                                </p>
                            </div>

                            {/* Buttons */}
                            <div className="mt-7 flex justify-end gap-3">

                                <button
                                    onClick={() => setShowDeleteModal(false)}
                                    disabled={deleting}
                                    className="rounded-lg border border-gray-700 px-4 py-2.5 text-sm font-medium text-gray-300 transition hover:bg-gray-800 disabled:opacity-50"
                                >
                                    Cancel
                                </button>

                                <button
                                    onClick={handleDeleteWebhook}
                                    disabled={deleting}
                                    className="inline-flex items-center gap-2 rounded-lg bg-red-600 px-4 py-2.5 text-sm font-medium text-white transition hover:bg-red-700 disabled:cursor-not-allowed disabled:opacity-50"
                                >
                                    <Trash2 size={16} />
                                    {deleting ? "Deleting..." : "Yes, Delete"}
                                </button>

                            </div>
                        </div>
                    </div>
                )}

            </div>
        </DashboardLayout>
    );
}

export default WebhookDetails;