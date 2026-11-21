import { useEffect, useState } from "react";
import {
    Plus,
    RefreshCw,
    Search,
    Activity,
    Calendar,
    ArrowUpRight,
    X,
    Code,
    Send,
} from "lucide-react";

import DashboardLayout from "../layouts/DashboardLayout";
import eventService from "../services/eventService";
import { useNavigate } from "react-router-dom";

function StatusBadge({ status }) {
    const styles = {
        PENDING: "bg-yellow-500/10 text-yellow-400 border-yellow-500/20",
        DELIVERED: "bg-green-500/10 text-green-400 border-green-500/20",
        FAILED: "bg-red-500/10 text-red-400 border-red-500/20",
    };

    return (
        <span
            className={`px-3 py-1 rounded-full text-xs font-medium border ${
                styles[status] || "bg-gray-500/10 text-gray-400 border-gray-500/20"
            }`}
        >
      {status || "UNKNOWN"}
    </span>
    );
}

function CreateEventModal({ onClose, onCreated }) {
    const [eventType, setEventType] = useState("");
    const [payload, setPayload] = useState(
        JSON.stringify(
            {
                message: "Hello from HookFlow",
                timestamp: new Date().toISOString(),
            },
            null,
            2
        )
    );

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        if (!eventType.trim()) {
            setError("Event type is required.");
            return;
        }

        let parsedPayload;

        try {
            parsedPayload = JSON.parse(payload);
        } catch {
            setError("Please enter valid JSON in the payload.");
            return;
        }

        try {
            setLoading(true);

            const createdEvent = await eventService.createEvent({
                eventType: eventType.trim(),
                payload: parsedPayload,
            });

            onCreated(createdEvent);
            onClose();
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to create event. Please try again."
            );
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/70 p-4">
            <div className="w-full max-w-2xl bg-gray-900 border border-gray-700 rounded-2xl shadow-2xl">
                <div className="flex items-center justify-between p-6 border-b border-gray-800">
                    <div>
                        <h2 className="text-xl font-semibold text-white">
                            Create Event
                        </h2>
                        <p className="text-sm text-gray-400 mt-1">
                            Submit an event to trigger matching webhooks.
                        </p>
                    </div>

                    <button
                        onClick={onClose}
                        className="text-gray-400 hover:text-white"
                    >
                        <X size={21} />
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="p-6 space-y-5">
                    <div>
                        <label className="block text-sm text-gray-300 mb-2">
                            Event Type
                        </label>

                        <input
                            type="text"
                            value={eventType}
                            onChange={(e) => setEventType(e.target.value)}
                            placeholder="e.g. ORDER_CREATED"
                            className="w-full bg-gray-950 border border-gray-700 rounded-lg px-4 py-3 text-white placeholder-gray-600 focus:outline-none focus:border-blue-500"
                        />

                        <p className="text-xs text-gray-500 mt-2">
                            Must match the event type configured on your webhook endpoints.
                        </p>
                    </div>

                    <div>
                        <label className="flex items-center gap-2 text-sm text-gray-300 mb-2">
                            <Code size={15} />
                            Event Payload (JSON)
                        </label>

                        <textarea
                            value={payload}
                            onChange={(e) => setPayload(e.target.value)}
                            rows={10}
                            spellCheck={false}
                            className="w-full bg-gray-950 border border-gray-700 rounded-lg px-4 py-3 text-sm text-green-300 font-mono focus:outline-none focus:border-blue-500 resize-y"
                        />
                    </div>

                    {error && (
                        <div className="bg-red-500/10 border border-red-500/20 text-red-400 rounded-lg p-3 text-sm">
                            {error}
                        </div>
                    )}

                    <div className="flex justify-end gap-3 pt-2">
                        <button
                            type="button"
                            onClick={onClose}
                            className="px-4 py-2.5 rounded-lg text-gray-300 hover:bg-gray-800"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={loading}
                            className="flex items-center gap-2 px-5 py-2.5 bg-blue-600 hover:bg-blue-500 disabled:opacity-50 text-white rounded-lg font-medium"
                        >
                            <Send size={16} />
                            {loading ? "Submitting..." : "Submit Event"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

function Events() {
    const navigate = useNavigate();

    const [events, setEvents] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");
    const [search, setSearch] = useState("");
    const [showModal, setShowModal] = useState(false);
    const [successMessage, setSuccessMessage] = useState("");

    const fetchEvents = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const data = await eventService.getAllEvents();
            setEvents(data);
        } catch (err) {
            setError(
                err.response?.data?.message || "Unable to load events."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchEvents();
    }, []);

    const handleCreated = (event) => {
        setEvents((previous) => [
            event,
            ...previous.filter((item) => item.id !== event.id),
        ]);

        setSuccessMessage("Event submitted successfully.");

        setTimeout(() => setSuccessMessage(""), 4000);
    };

    const filteredEvents = events.filter((event) =>
        event.eventType?.toLowerCase().includes(search.toLowerCase())
    );

    return (
        <DashboardLayout>
            <div className="space-y-6">
                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-white">
                            Events
                        </h1>
                        <p className="text-gray-400 mt-1">
                            Submit and monitor events across your webhook endpoints.
                        </p>
                    </div>

                    <div className="flex gap-3">
                        <button
                            onClick={() => fetchEvents(true)}
                            disabled={refreshing}
                            className="flex items-center gap-2 px-4 py-2.5 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-800"
                        >
                            <RefreshCw
                                size={16}
                                className={refreshing ? "animate-spin" : ""}
                            />
                            Refresh
                        </button>

                        <button
                            onClick={() => setShowModal(true)}
                            className="flex items-center gap-2 px-4 py-2.5 bg-blue-600 hover:bg-blue-500 text-white rounded-lg"
                        >
                            <Plus size={18} />
                            Create Event
                        </button>
                    </div>
                </div>

                {/* Success */}
                {successMessage && (
                    <div className="bg-green-500/10 border border-green-500/20 text-green-400 rounded-lg p-3 text-sm">
                        {successMessage}
                    </div>
                )}

                {/* Summary */}
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center justify-between">
                            <p className="text-gray-400 text-sm">Total Events</p>
                            <Activity className="text-blue-400" size={20} />
                        </div>
                        <p className="text-3xl font-bold text-white mt-3">
                            {events.length}
                        </p>
                    </div>

                    <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
                        <div className="flex items-center justify-between">
                            <p className="text-gray-400 text-sm">Event Types</p>
                            <Code className="text-purple-400" size={20} />
                        </div>
                        <p className="text-3xl font-bold text-white mt-3">
                            {new Set(events.map((event) => event.eventType)).size}
                        </p>
                    </div>
                </div>

                {/* Search */}
                <div className="relative">
                    <Search
                        size={18}
                        className="absolute left-4 top-1/2 -translate-y-1/2 text-gray-500"
                    />

                    <input
                        type="text"
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                        placeholder="Search events by type..."
                        className="w-full bg-gray-900 border border-gray-800 rounded-xl pl-11 pr-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                    />
                </div>

                {/* Events list */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">
                    <div className="p-5 border-b border-gray-800">
                        <h2 className="text-lg font-semibold text-white">
                            Event History
                        </h2>
                        <p className="text-sm text-gray-400 mt-1">
                            All events submitted through your account.
                        </p>
                    </div>

                    {loading ? (
                        <div className="p-12 text-center text-gray-400">
                            Loading events...
                        </div>
                    ) : error ? (
                        <div className="p-10 text-center">
                            <p className="text-red-400 mb-4">{error}</p>
                            <button
                                onClick={() => fetchEvents()}
                                className="text-blue-400 hover:text-blue-300"
                            >
                                Try again
                            </button>
                        </div>
                    ) : filteredEvents.length === 0 ? (
                        <div className="p-12 text-center">
                            <Activity size={36} className="mx-auto text-gray-600 mb-4" />

                            <h3 className="text-white font-medium">
                                {search ? "No matching events" : "No events yet"}
                            </h3>

                            <p className="text-gray-400 text-sm mt-2">
                                {search
                                    ? "Try searching with a different event type."
                                    : "Create your first event to start triggering webhooks."}
                            </p>
                        </div>
                    ) : (
                        <div className="divide-y divide-gray-800">
                            {filteredEvents.map((event) => (
                                <div
                                    key={event.id}
                                    onClick={() => navigate(`/events/${event.id}`)}
                                    className="p-5 flex flex-col sm:flex-row sm:items-center justify-between gap-4 hover:bg-gray-800/40 cursor-pointer transition"
                                >
                                    <div className="flex items-start gap-4 min-w-0">
                                        <div className="p-3 bg-blue-500/10 rounded-lg text-blue-400">
                                            <Activity size={20} />
                                        </div>

                                        <div className="min-w-0">
                                            <h3 className="text-white font-medium truncate">
                                                {event.eventType}
                                            </h3>

                                            <div className="flex items-center gap-2 text-gray-500 text-xs mt-2">
                                                <span>Event #{event.id}</span>
                                                <span>•</span>
                                                <Calendar size={13} />
                                                <span>
                          {event.createdAt
                              ? new Date(event.createdAt).toLocaleString()
                              : "Date unavailable"}
                        </span>
                                            </div>
                                        </div>
                                    </div>

                                    <div className="flex items-center justify-between sm:justify-end gap-4">
                                        <StatusBadge status={event.status} />

                                        <ArrowUpRight
                                            size={18}
                                            className="text-gray-500"
                                        />
                                    </div>
                                </div>
                            ))}
                        </div>
                    )}
                </div>
            </div>

            {showModal && (
                <CreateEventModal
                    onClose={() => setShowModal(false)}
                    onCreated={handleCreated}
                />
            )}
        </DashboardLayout>
    );
}

export default Events;