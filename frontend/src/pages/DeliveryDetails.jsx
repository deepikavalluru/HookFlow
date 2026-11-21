import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";

import {
    ArrowLeft,
    RefreshCw,
    RotateCcw,
    Activity,
    Clock,
    CheckCircle,
    XCircle,
    AlertCircle,
    Calendar,
    Server,
} from "lucide-react";

import DashboardLayout from "../layouts/DashboardLayout";
import deliveryService from "../services/deliveryService";

function StatusBadge({ status }) {
    const styles = {
        DELIVERED: "bg-green-500/10 text-green-400 border-green-500/20",
        FAILED: "bg-red-500/10 text-red-400 border-red-500/20",
        PENDING: "bg-yellow-500/10 text-yellow-400 border-yellow-500/20",
        RETRY_SCHEDULED:
            "bg-orange-500/10 text-orange-400 border-orange-500/20",
        SUCCESS: "bg-green-500/10 text-green-400 border-green-500/20",
    };

    return (
        <span
            className={`inline-flex px-3 py-1 rounded-full text-xs font-medium border ${
                styles[status] ||
                "bg-gray-500/10 text-gray-400 border-gray-500/20"
            }`}
        >
      {status || "UNKNOWN"}
    </span>
    );
}

function DeliveryDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [delivery, setDelivery] = useState(null);
    const [attempts, setAttempts] = useState([]);

    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [retrying, setRetrying] = useState(false);

    const [error, setError] = useState("");
    const [retryMessage, setRetryMessage] = useState("");

    const fetchDetails = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const [deliveryData, attemptData] = await Promise.all([
                deliveryService.getDeliveryById(id),
                deliveryService.getDeliveryAttempts(id),
            ]);

            setDelivery(deliveryData);
            setAttempts(attemptData);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load delivery details."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchDetails();
    }, [id]);

    const handleRetry = async () => {
        try {
            setRetrying(true);
            setRetryMessage("");
            setError("");

            await deliveryService.retryDelivery(id);

            setRetryMessage("Manual retry successfully initiated.");

            // The backend responds with HTTP 202.
            // Refresh shortly afterward to allow async processing to begin.
            setTimeout(() => {
                fetchDetails(true);
            }, 1000);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to initiate delivery retry."
            );
        } finally {
            setRetrying(false);
        }
    };

    if (loading) {
        return (
            <DashboardLayout>
                <div className="p-12 text-center text-gray-400">
                    Loading delivery details...
                </div>
            </DashboardLayout>
        );
    }

    if (error && !delivery) {
        return (
            <DashboardLayout>
                <div className="max-w-3xl mx-auto py-16 text-center">
                    <AlertCircle
                        size={40}
                        className="mx-auto text-red-400 mb-4"
                    />

                    <h2 className="text-xl font-semibold text-white">
                        Unable to load delivery
                    </h2>

                    <p className="text-gray-400 mt-2">{error}</p>

                    <button
                        onClick={() => navigate("/deliveries")}
                        className="mt-6 text-blue-400 hover:text-blue-300"
                    >
                        Back to Deliveries
                    </button>
                </div>
            </DashboardLayout>
        );
    }

    if (!delivery) return null;

    return (
        <DashboardLayout>
            <div className="space-y-6">

                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">

                    <div>
                        <button
                            onClick={() => navigate("/deliveries")}
                            className="flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-4"
                        >
                            <ArrowLeft size={16} />
                            Back to Deliveries
                        </button>

                        <h1 className="text-2xl font-bold text-white">
                            Delivery #{delivery.id}
                        </h1>

                        <p className="text-gray-400 mt-1">
                            Inspect delivery information and attempt history.
                        </p>
                    </div>

                    <div className="flex gap-3">

                        <button
                            onClick={() => fetchDetails(true)}
                            disabled={refreshing}
                            className="flex items-center gap-2 px-4 py-2.5 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-800"
                        >
                            <RefreshCw
                                size={16}
                                className={refreshing ? "animate-spin" : ""}
                            />
                            Refresh
                        </button>

                        {delivery.status === "FAILED" && (
                            <button
                                onClick={handleRetry}
                                disabled={retrying}
                                className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white rounded-lg"
                            >
                                <RotateCcw size={16} />
                                {retrying ? "Retrying..." : "Retry Delivery"}
                            </button>
                        )}
                    </div>
                </div>

                {/* Notifications */}
                {retryMessage && (
                    <div className="bg-green-500/10 border border-green-500/20 text-green-400 rounded-lg p-4 text-sm">
                        {retryMessage}
                    </div>
                )}

                {error && delivery && (
                    <div className="bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg p-4 text-sm">
                        {error}
                    </div>
                )}

                {/* Delivery information */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">

                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">

                        <div>
                            <p className="text-gray-500 text-sm">
                                Delivery Status
                            </p>

                            <div className="mt-2">
                                <StatusBadge status={delivery.status} />
                            </div>
                        </div>

                        <div>
                            <p className="text-gray-500 text-sm">
                                Attempt Count
                            </p>

                            <p className="text-2xl font-bold text-white mt-1">
                                {delivery.attemptCount ?? 0}
                            </p>
                        </div>
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-6 mt-7 pt-6 border-t border-gray-800">

                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <Activity size={15} />
                                Event ID
                            </div>

                            <button
                                onClick={() => navigate(`/events/${delivery.eventId}`)}
                                className="text-blue-400 hover:text-blue-300 mt-2"
                            >
                                #{delivery.eventId}
                            </button>
                        </div>

                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <Server size={15} />
                                Endpoint ID
                            </div>

                            <p className="text-gray-200 mt-2">
                                #{delivery.endpointId}
                            </p>
                        </div>

                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <CheckCircle size={15} />
                                HTTP Response
                            </div>

                            <p className="text-gray-200 mt-2">
                                {delivery.lastResponseCode ?? "Not available"}
                            </p>
                        </div>

                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <Calendar size={15} />
                                Created At
                            </div>

                            <p className="text-gray-200 mt-2 text-sm">
                                {delivery.createdAt
                                    ? new Date(delivery.createdAt).toLocaleString()
                                    : "Not available"}
                            </p>
                        </div>
                    </div>
                </div>

                {/* Last error */}
                {delivery.lastError && (
                    <div className="bg-red-500/5 border border-red-500/20 rounded-xl p-5">
                        <div className="flex items-center gap-2 mb-3">
                            <AlertCircle size={18} className="text-red-400" />

                            <h2 className="font-semibold text-white">
                                Latest Delivery Error
                            </h2>
                        </div>

                        <p className="text-sm text-red-400 break-words">
                            {delivery.lastError}
                        </p>
                    </div>
                )}

                {/* Attempt history */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">

                    <div className="p-5 border-b border-gray-800">
                        <h2 className="text-lg font-semibold text-white">
                            Attempt History
                        </h2>

                        <p className="text-sm text-gray-400 mt-1">
                            Complete history of delivery attempts.
                        </p>
                    </div>

                    {attempts.length === 0 ? (
                        <div className="p-12 text-center">
                            <Clock
                                size={35}
                                className="mx-auto text-gray-600 mb-4"
                            />

                            <h3 className="text-white font-medium">
                                No attempts recorded
                            </h3>

                            <p className="text-gray-400 text-sm mt-2">
                                Attempt history will appear here when delivery is processed.
                            </p>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-800">

                            {attempts.map((attempt) => (
                                <div
                                    key={attempt.id}
                                    className="p-5"
                                >
                                    <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">

                                        <div className="flex items-start gap-4">

                                            <div
                                                className={`p-3 rounded-lg ${
                                                    attempt.status === "SUCCESS"
                                                        ? "bg-green-500/10 text-green-400"
                                                        : "bg-red-500/10 text-red-400"
                                                }`}
                                            >
                                                {attempt.status === "SUCCESS" ? (
                                                    <CheckCircle size={20} />
                                                ) : (
                                                    <XCircle size={20} />
                                                )}
                                            </div>

                                            <div>
                                                <h3 className="text-white font-medium">
                                                    Attempt #{attempt.attemptNumber}
                                                </h3>

                                                <p className="text-gray-500 text-xs mt-2">
                                                    {attempt.attemptedAt
                                                        ? new Date(
                                                            attempt.attemptedAt
                                                        ).toLocaleString()
                                                        : "Time unavailable"}
                                                </p>
                                            </div>
                                        </div>

                                        <div className="flex items-center gap-4">
                                            <div className="text-right">
                                                <p className="text-gray-500 text-xs">
                                                    HTTP Response
                                                </p>

                                                <p className="text-gray-200 mt-1">
                                                    {attempt.responseCode ?? "—"}
                                                </p>
                                            </div>

                                            <StatusBadge status={attempt.status} />
                                        </div>
                                    </div>

                                    {attempt.errorMessage && (
                                        <div className="mt-4 ml-0 sm:ml-16 bg-red-500/5 border border-red-500/10 rounded-lg p-3">
                                            <p className="text-xs text-gray-500 mb-1">
                                                Error Details
                                            </p>

                                            <p className="text-sm text-red-400 break-words">
                                                {attempt.errorMessage}
                                            </p>
                                        </div>
                                    )}
                                </div>
                            ))}

                        </div>
                    )}
                </div>

            </div>
        </DashboardLayout>
    );
}

export default DeliveryDetails;