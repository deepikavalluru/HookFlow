import { useEffect, useMemo, useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    Activity,
    CheckCircle,
    XCircle,
    Clock,
    RefreshCw,
    Search,
    ArrowUpRight,
    Send,
    RotateCcw,
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

function StatCard({ title, value, icon: Icon, color }) {
    return (
        <div className="bg-gray-900 border border-gray-800 rounded-xl p-5">
            <div className="flex items-center justify-between">
                <p className="text-gray-400 text-sm">{title}</p>
                <Icon size={21} className={color} />
            </div>

            <p className="text-3xl font-bold text-white mt-3">
                {value}
            </p>
        </div>
    );
}

function Deliveries() {
    const navigate = useNavigate();

    const [deliveries, setDeliveries] = useState([]);
    const [loading, setLoading] = useState(true);
    const [refreshing, setRefreshing] = useState(false);
    const [error, setError] = useState("");

    const [search, setSearch] = useState("");
    const [statusFilter, setStatusFilter] = useState("ALL");

    const fetchDeliveries = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            setError("");

            const data = await deliveryService.getAllDeliveries();

            setDeliveries(data);
        } catch (err) {
            setError(
                err.response?.data?.message ||
                "Unable to load deliveries."
            );
        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    useEffect(() => {
        fetchDeliveries();
    }, []);

    const stats = useMemo(() => {
        return {
            total: deliveries.length,
            delivered: deliveries.filter(
                (item) => item.status === "DELIVERED"
            ).length,
            failed: deliveries.filter(
                (item) => item.status === "FAILED"
            ).length,
            pending: deliveries.filter(
                (item) =>
                    item.status === "PENDING" ||
                    item.status === "RETRY_SCHEDULED"
            ).length,
        };
    }, [deliveries]);

    const filteredDeliveries = useMemo(() => {
        return deliveries.filter((delivery) => {
            const matchesStatus =
                statusFilter === "ALL" ||
                delivery.status === statusFilter;

            const searchValue = search.toLowerCase();

            const matchesSearch =
                String(delivery.id).includes(searchValue) ||
                String(delivery.eventId).includes(searchValue) ||
                String(delivery.endpointId).includes(searchValue);

            return matchesStatus && matchesSearch;
        });
    }, [deliveries, statusFilter, search]);

    const filters = [
        "ALL",
        "DELIVERED",
        "FAILED",
        "PENDING",
        "RETRY_SCHEDULED",
    ];

    return (
        <DashboardLayout>
            <div className="space-y-6">

                {/* Header */}
                <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-bold text-white">
                            Deliveries
                        </h1>

                        <p className="text-gray-400 mt-1">
                            Monitor webhook delivery activity and failures.
                        </p>
                    </div>

                    <button
                        onClick={() => fetchDeliveries(true)}
                        disabled={refreshing}
                        className="flex items-center justify-center gap-2 px-4 py-2.5 border border-gray-700 text-gray-300 rounded-lg hover:bg-gray-800"
                    >
                        <RefreshCw
                            size={16}
                            className={refreshing ? "animate-spin" : ""}
                        />
                        Refresh
                    </button>
                </div>

                {/* Statistics */}
                <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
                    <StatCard
                        title="Total Deliveries"
                        value={stats.total}
                        icon={Activity}
                        color="text-blue-400"
                    />

                    <StatCard
                        title="Delivered"
                        value={stats.delivered}
                        icon={CheckCircle}
                        color="text-green-400"
                    />

                    <StatCard
                        title="Failed"
                        value={stats.failed}
                        icon={XCircle}
                        color="text-red-400"
                    />

                    <StatCard
                        title="Pending / Retrying"
                        value={stats.pending}
                        icon={Clock}
                        color="text-yellow-400"
                    />
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
                        placeholder="Search by delivery ID, event ID or endpoint ID..."
                        className="w-full bg-gray-900 border border-gray-800 rounded-xl pl-11 pr-4 py-3 text-white placeholder-gray-500 focus:outline-none focus:border-blue-500"
                    />
                </div>

                {/* Status filters */}
                <div className="flex gap-2 overflow-x-auto pb-2">
                    {filters.map((filter) => (
                        <button
                            key={filter}
                            onClick={() => setStatusFilter(filter)}
                            className={`px-4 py-2 rounded-lg text-sm whitespace-nowrap transition ${
                                statusFilter === filter
                                    ? "bg-blue-600 text-white"
                                    : "bg-gray-900 border border-gray-800 text-gray-400 hover:text-white hover:bg-gray-800"
                            }`}
                        >
                            {filter.replaceAll("_", " ")}
                        </button>
                    ))}
                </div>

                {/* Delivery table */}
                <div className="bg-gray-900 border border-gray-800 rounded-xl overflow-hidden">

                    <div className="p-5 border-b border-gray-800">
                        <h2 className="text-lg font-semibold text-white">
                            Delivery History
                        </h2>

                        <p className="text-sm text-gray-400 mt-1">
                            All webhook delivery attempts associated with your events.
                        </p>
                    </div>

                    {loading ? (
                        <div className="p-12 text-center text-gray-400">
                            Loading deliveries...
                        </div>
                    ) : error ? (
                        <div className="p-12 text-center">
                            <p className="text-red-400">{error}</p>

                            <button
                                onClick={() => fetchDeliveries()}
                                className="text-blue-400 mt-4 hover:text-blue-300"
                            >
                                Try again
                            </button>
                        </div>
                    ) : filteredDeliveries.length === 0 ? (
                        <div className="p-12 text-center">
                            <Send
                                size={36}
                                className="mx-auto text-gray-600 mb-4"
                            />

                            <h3 className="text-white font-medium">
                                No deliveries found
                            </h3>

                            <p className="text-gray-400 text-sm mt-2">
                                Try changing your search or status filter.
                            </p>
                        </div>
                    ) : (
                        <div className="overflow-x-auto">
                            <table className="w-full text-sm text-left">

                                <thead className="bg-gray-950 text-gray-400">
                                <tr>
                                    <th className="px-5 py-4 font-medium">Delivery</th>
                                    <th className="px-5 py-4 font-medium">Event</th>
                                    <th className="px-5 py-4 font-medium">Endpoint</th>
                                    <th className="px-5 py-4 font-medium">Status</th>
                                    <th className="px-5 py-4 font-medium">Attempts</th>
                                    <th className="px-5 py-4 font-medium">HTTP Code</th>
                                    <th className="px-5 py-4 font-medium">Created</th>
                                    <th className="px-5 py-4 font-medium"></th>
                                </tr>
                                </thead>

                                <tbody className="divide-y divide-gray-800">
                                {filteredDeliveries.map((delivery) => (
                                    <tr
                                        key={delivery.id}
                                        onClick={() =>
                                            navigate(`/deliveries/${delivery.id}`)
                                        }
                                        className="hover:bg-gray-800/40 cursor-pointer"
                                    >
                                        <td className="px-5 py-4 text-white font-medium">
                                            #{delivery.id}
                                        </td>

                                        <td className="px-5 py-4 text-gray-300">
                                            #{delivery.eventId}
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

                                        <td className="px-5 py-4">
                                            <ArrowUpRight
                                                size={17}
                                                className="text-gray-500"
                                            />
                                        </td>
                                    </tr>
                                ))}
                                </tbody>

                            </table>
                        </div>
                    )}

                    {!loading && !error && filteredDeliveries.length > 0 && (
                        <div className="px-5 py-4 border-t border-gray-800 text-xs text-gray-500">
                            Showing {filteredDeliveries.length} of {deliveries.length} deliveries
                        </div>
                    )}
                </div>
            </div>
        </DashboardLayout>
    );
}

export default Deliveries;