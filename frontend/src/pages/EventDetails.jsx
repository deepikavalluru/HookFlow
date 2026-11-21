import { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import {
    ArrowLeft,
    Activity,
    Calendar,
    Code,
    RefreshCw,
    Send,
    CheckCircle,
    XCircle,
    Clock,
    AlertCircle,
} from "lucide-react";

import DashboardLayout from "../layouts/DashboardLayout";
import eventService from "../services/eventService";

function StatusBadge({ status }) {
    const styles = {
        PENDING: "bg-yellow-500/10 text-yellow-400 border-yellow-500/20",
        RETRY_SCHEDULED: "bg-orange-500/10 text-orange-400 border-orange-500/20",
        DELIVERED: "bg-green-500/10 text-green-400 border-green-500/20",
        FAILED: "bg-red-500/10 text-red-400 border-red-500/20",
    };

    return (
        <span
            className={`inline-flex px-3 py-1 rounded-full text-xs font-medium border ${
                styles[status] || "bg-gray-500/10 text-gray-400 border-gray-500/20"
            }`}
        >
      {status || "UNKNOWN"}
    </span>
    );
}

function EventDetails() {
    const { id } = useParams();
    const navigate = useNavigate();

    const [event, setEvent] = useState(null);
    const [deliveries, setDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");

    const fetchDetails = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const [eventData, allDeliveries] = await Promise.all([
                eventService.getEventById(id),
                eventService.getAllDeliveries(),
            ]);

            setEvent(eventData);

            setDeliveries(
                allDeliveries.filter(
                    (delivery) => String(delivery.eventId) === String(id)
                )
            );
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load event details."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchDetails();
    }, [id]);

    if (loading) {
        return (
            <DashboardLayout>
                <div className="p-12 text-center text-gray-400">
                    Loading event details...
                </div>
            </DashboardLayout>
        );
    }

    if (error || !event) {
        return (
            <DashboardLayout>
                <div className="max-w-3xl mx-auto py-16 text-center">
                    <AlertCircle
                        size={40}
                        className="mx-auto text-red-400 mb-4"
                    />

                    <h2 className="text-xl text-white font-semibold">
                        Unable to load event
                    </h2>

                    <p className="text-gray-400 mt-2">
                        {error || "Event not found."}
                    </p>

                    <button
                        onClick={() => navigate("/events")}
                        className="mt-6 text-blue-400 hover:text-blue-300"
                    >
                        Back to Events
                    </button>
                </div>
            </DashboardLayout>
        );
    }

    const deliveredCount = deliveries.filter(
        (delivery) => delivery.status === "DELIVERED"
    ).length;

    const failedCount = deliveries.filter(
        (delivery) => delivery.status === "FAILED"
    ).length;

    const pendingCount = deliveries.filter(
        (delivery) =>
            delivery.status === "PENDING" ||
            delivery.status === "RETRY_SCHEDULED"
    ).length;

    return (
        <DashboardLayout>
            <div className="space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <button
                            onClick={() => navigate("/events")}
                            className="flex items-center gap-2 text-gray-400 hover:text-white text-sm mb-4"
                        >
                            <ArrowLeft size={16} />
                            Back to Events
                        </button>

                        <h1 className="text-2xl font-bold text-white">
                            Event Details
                        </h1>

                        <p className="text-gray-400 mt-1">
                            Inspect event information and its webhook deliveries.
                        </p>
                    </div>

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
                </div>

                {/* Event information */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl p-6">
                    <div className="flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
                        <div className="flex items-start gap-4">
                            <div className="p-3 bg-blue-500/10 rounded-xl text-blue-400">
                                <Activity size={24} />
                            </div>

                            <div>
                                <p className="text-gray-500 text-sm">
                                    Event Type
                                </p>

                                <h2 className="text-xl font-semibold text-white mt-1 break-all">
                                    {event.eventType}
                                </h2>

                                <p className="text-gray-500 text-sm mt-2">
                                    Event ID: #{event.id}
                                </p>
                            </div>
                        </div>

                        <StatusBadge status={event.status} />
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-5 mt-7 pt-6 border-t border-gray-800">
                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <Calendar size={15} />
                                Created At
                            </div>

                            <p className="text-gray-200 mt-2 text-sm">
                                {event.createdAt
                                    ? new Date(event.createdAt).toLocaleString()
                                    : "Not available"}
                            </p>
                        </div>

                        <div>
                            <div className="flex items-center gap-2 text-gray-500 text-sm">
                                <Send size={15} />
                                Total Deliveries
                            </div>

                            <p className="text-gray-200 mt-2 text-sm">
                                {deliveries.length}
                            </p>
                        </div>
                    </div>
                </div>

                {/* Delivery summary */}
                <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center gap-2 text-green-400">
                            <CheckCircle size={18} />
                            <span className="text-sm">Delivered</span>
                        </div>

                        <p className="text-3xl font-bold text-white mt-3">
                            {deliveredCount}
                        </p>
                    </div>

                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center gap-2 text-red-400">
                            <XCircle size={18} />
                            <span className="text-sm">Failed</span>
                        </div>

                        <p className="text-3xl font-bold text-white mt-3">
                            {failedCount}
                        </p>
                    </div>

                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center gap-2 text-yellow-400">
                            <Clock size={18} />
                            <span className="text-sm">Pending / Retrying</span>
                        </div>

                        <p className="text-3xl font-bold text-white mt-3">
                            {pendingCount}
                        </p>
                    </div>
                </div>

                {/* Payload */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
                    <div className="flex items-center gap-2 p-5 border-b border-gray-800">
                        <Code size={18} className="text-blue-400" />

                        <div>
                            <h2 className="font-semibold text-white">
                                Event Payload
                            </h2>
                            <p className="text-gray-500 text-xs mt-1">
                                Original JSON data submitted with this event.
                            </p>
                        </div>
                    </div>

                    <pre className="p-5 text-sm text-green-300 font-mono overflow-x-auto whitespace-pre-wrap break-words">
            {JSON.stringify(event.payload, null, 2)}
          </pre>
                </div>

                {/* Associated deliveries */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
                    <div className="p-5 border-b border-gray-800">
                        <h2 className="text-lg font-semibold text-white">
                            Associated Deliveries
                        </h2>

                        <p className="text-sm text-gray-400 mt-1">
                            Delivery records created for matching webhook endpoints.
                        </p>
                    </div>

                    {deliveries.length === 0 ? (
                        <div className="p-12 text-center">
                            <Send
                                size={35}
                                className="mx-auto text-gray-600 mb-4"
                            />

                            <h3 className="text-white font-medium">
                                No deliveries found
                            </h3>

                            <p className="text-gray-400 text-sm mt-2">
                                No active webhook endpoint matched this event type when
                                the event was submitted.
                            </p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm text-left">
                                <thead className="bg-gray-950 text-gray-400">
                                <tr>
                                    <th className="px-5 py-4 font-medium">Delivery ID</th>
                                    <th className="px-5 py-4 font-medium">Endpoint ID</th>
                                    <th className="px-5 py-4 font-medium">Status</th>
                                    <th className="px-5 py-4 font-medium">Attempts</th>
                                    <th className="px-5 py-4 font-medium">HTTP Code</th>
                                    <th className="px-5 py-4 font-medium">Created</th>
                                </tr>
                                </thead>

                                <tbody className="divide-y divide-gray-800">
                                {deliveries.map((delivery) => (
                                    <tr
                                        key={delivery.id}
                                        className="hover:bg-gray-800/40"
                                    >
                                        <td className="px-5 py-4 text-white">
                                            #{delivery.id}
                                        </td>

                                        <td className="px-5 py-4 text-gray-300">
                                            #{delivery.endpointId}
                                        </td>

                                        <td className="px-5 py-4">
                                            <StatusBadge status={delivery.status} />
                                        </td>

                                        <td className="px-5 py-4 text-gray-300">
                                            {delivery.attemptCount ?? 0}
                                        </td>

                                        <td className="px-5 py-4 text-gray-300">
                                            {delivery.lastResponseCode ?? "—"}
                                        </td>

                                        <td className="px-5 py-4 text-gray-400 whitespace-nowrap">
                                            {delivery.createdAt
                                                ? new Date(delivery.createdAt).toLocaleString()
                                                : "—"}
                                        </td>
                                    </tr>
                                ))}
                                </tbody>
                            </table>
                        </div>
                    )}
                </div>

                {/* Delivery errors */}
                {deliveries.some((delivery) => delivery.lastError) && (
                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center gap-2 mb-4">
                            <AlertCircle size={18} className="text-red-400" />
                            <h2 className="font-semibold text-white">
                                Delivery Errors
                            </h2>
                        </div>

                        <div className="space-y-3">
                            {deliveries
                                .filter((delivery) => delivery.lastError)
                                .map((delivery) => (
                                    <div
                                        key={delivery.id}
                                        className="bg-red-500/5 border border-red-500/10 rounded-lg p-4"
                                    >
                                        <p className="text-sm text-gray-300">
                                            Delivery #{delivery.id}
                                        </p>

                                        <p className="text-sm text-red-400 mt-2 break-words">
                                            {delivery.lastError}
                                        </p>
                                    </div>
                                ))}
                        </div>
                    </div>
                )}
            </div>
        </DashboardLayout>
    );
}

export default EventDetails;