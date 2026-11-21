import { useEffect, useState } from "react";
import {
    Activity,
    Webhook,
    CheckCircle,
    XCircle,
    Clock,
    RefreshCw
} from "lucide-react";

import {
    ResponsiveContainer,
    PieChart,
    Pie,
    Cell,
    Tooltip,
    Legend,
    BarChart,
    Bar,
    XAxis,
    YAxis,
    CartesianGrid
} from "recharts";

import DashboardLayout from "../layouts/DashboardLayout";
import dashboardService from "../services/dashboardService";
import RecentDeliveries from "../components/dashboard/RecentDeliveries";
import DashboardSkeleton from "../components/dashboard/DashboardSkeleton";
import DashboardError from "../components/dashboard/DashboardError";

const COLORS = ["#22c55e", "#ef4444", "#f59e0b"];

function Dashboard() {
    const [stats, setStats] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [refreshing, setRefreshing] = useState(false);

    useEffect(() => {
        fetchDashboardStats();
    }, []);

    const fetchDashboardStats = async (isRefresh = false) => {
        try {
            if (isRefresh) {
                setRefreshing(true);
            } else {
                setLoading(true);
            }

            const data = await dashboardService.getDashboardStats();

            setStats(data);
            setError("");

        } catch (err) {
            console.error("Dashboard API Error:", err);

            setError(
                err.response?.data?.message ||
                "Unable to load dashboard statistics."
            );

        } finally {
            setLoading(false);
            setRefreshing(false);
        }
    };

    const deliveryStats = stats?.deliveryStats;

    const statCards = [
        {
            title: "Total Events",
            value: stats?.totalEvents ?? 0,
            icon: Activity,
            description: "Events created"
        },
        {
            title: "Total Endpoints",
            value: stats?.totalEndpoints ?? 0,
            icon: Webhook,
            description: "Registered webhooks"
        },
        {
            title: "Total Deliveries",
            value: deliveryStats?.totalDeliveries ?? 0,
            icon: Clock,
            description: "Deliveries tracked"
        },
        {
            title: "Successful Deliveries",
            value: deliveryStats?.successfulDeliveries ?? 0,
            icon: CheckCircle,
            description: "Successfully delivered"
        },
        {
            title: "Failed Deliveries",
            value: deliveryStats?.failedDeliveries ?? 0,
            icon: XCircle,
            description: "Failed deliveries"
        },
        {
            title: "Pending Deliveries",
            value: deliveryStats?.pendingDeliveries ?? 0,
            icon: Clock,
            description: "Awaiting delivery"
        }
    ];

    const statusData = [
        {
            name: "Successful",
            value: deliveryStats?.successfulDeliveries ?? 0
        },
        {
            name: "Failed",
            value: deliveryStats?.failedDeliveries ?? 0
        },
        {
            name: "Pending",
            value: deliveryStats?.pendingDeliveries ?? 0
        }
    ];

    const comparisonData = [
        {
            name: "Deliveries",
            Successful: deliveryStats?.successfulDeliveries ?? 0,
            Failed: deliveryStats?.failedDeliveries ?? 0
        }
    ];

    return (
        <DashboardLayout title="Overview">

            <div className="space-y-8">

                {/* Welcome Section */}

                <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">

                    <div>
                        <h2 className="text-2xl font-bold text-white">
                            Welcome to HookFlow
                        </h2>

                        <p className="text-slate-400 mt-2">
                            Monitor your webhook events and delivery performance.
                        </p>
                    </div>

                    <button
                        onClick={() => fetchDashboardStats(true)}
                        disabled={refreshing}
                        className="flex items-center justify-center gap-2 px-4 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg transition"
                    >
                        <RefreshCw
                            size={17}
                            className={refreshing ? "animate-spin" : ""}
                        />

                        {refreshing ? "Refreshing..." : "Refresh"}
                    </button>

                </div>

                {error && stats && (
                    <div className="flex items-center justify-between gap-4 bg-red-500/10 border border-red-500/20 rounded-lg p-4">

                        <p className="text-sm text-red-400">
                            {error} Showing previously loaded data.
                        </p>

                        <button
                            onClick={() => fetchDashboardStats(true)}
                            disabled={refreshing}
                            className="text-sm text-red-400 hover:text-red-300 font-medium whitespace-nowrap"
                        >
                            Retry
                        </button>

                    </div>
                )}

                {loading && !stats && <DashboardSkeleton />}

                {error && !stats && (
                    <DashboardError
                        message={error}
                        onRetry={() => fetchDashboardStats(true)}
                        retrying={refreshing}
                    />
                )}

                {!loading && stats && (
                    <>
                        {/* Statistics Cards */}

                        <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">

                            {statCards.map((card) => {
                                const Icon = card.icon;

                                return (
                                    <div
                                        key={card.title}
                                        className="bg-slate-900 border border-slate-800 rounded-xl p-6 hover:border-indigo-500/50 transition"
                                    >
                                        <div className="flex items-center justify-between">

                                            <p className="text-slate-400 text-sm">
                                                {card.title}
                                            </p>

                                            <div className="p-3 bg-indigo-500/10 rounded-lg">
                                                <Icon
                                                    size={22}
                                                    className="text-indigo-400"
                                                />
                                            </div>

                                        </div>

                                        <h3 className="text-3xl font-bold text-white mt-5">
                                            {card.value}
                                        </h3>

                                        <p className="text-xs text-slate-500 mt-2">
                                            {card.description}
                                        </p>
                                    </div>
                                );
                            })}

                        </div>

                        {/* Charts */}

                        <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">

                            {/* Delivery Status Pie Chart */}

                            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">

                                <h3 className="text-lg font-semibold text-white">
                                    Delivery Status Distribution
                                </h3>

                                <p className="text-sm text-slate-400 mt-1">
                                    Breakdown of webhook delivery statuses
                                </p>

                                <div className="h-80 mt-4">

                                    {statusData.every(item => item.value === 0) ? (
                                        <div className="h-full flex items-center justify-center text-slate-500">
                                            No delivery data available yet.
                                        </div>
                                    ) : (
                                        <ResponsiveContainer width="100%" height="100%">
                                            <PieChart>
                                                <Pie
                                                    data={statusData}
                                                    dataKey="value"
                                                    nameKey="name"
                                                    cx="50%"
                                                    cy="45%"
                                                    outerRadius={100}
                                                    innerRadius={60}
                                                    paddingAngle={4}
                                                >
                                                    {statusData.map((entry, index) => (
                                                        <Cell
                                                            key={entry.name}
                                                            fill={COLORS[index]}
                                                        />
                                                    ))}
                                                </Pie>

                                                <Tooltip
                                                    contentStyle={{
                                                        backgroundColor: "#0f172a",
                                                        border: "1px solid #334155",
                                                        borderRadius: "8px",
                                                        color: "#fff"
                                                    }}
                                                />

                                                <Legend />
                                            </PieChart>
                                        </ResponsiveContainer>
                                    )}

                                </div>
                            </div>

                            {/* Success vs Failure Chart */}

                            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">

                                <h3 className="text-lg font-semibold text-white">
                                    Delivery Performance
                                </h3>

                                <p className="text-sm text-slate-400 mt-1">
                                    Successful versus failed deliveries
                                </p>

                                <div className="h-80 mt-4">

                                    <ResponsiveContainer width="100%" height="100%">
                                        <BarChart
                                            data={comparisonData}
                                            margin={{
                                                top: 20,
                                                right: 20,
                                                left: 0,
                                                bottom: 5
                                            }}
                                        >
                                            <CartesianGrid
                                                strokeDasharray="3 3"
                                                stroke="#334155"
                                            />

                                            <XAxis
                                                dataKey="name"
                                                stroke="#94a3b8"
                                            />

                                            <YAxis
                                                stroke="#94a3b8"
                                                allowDecimals={false}
                                            />

                                            <Tooltip
                                                contentStyle={{
                                                    backgroundColor: "#0f172a",
                                                    border: "1px solid #334155",
                                                    borderRadius: "8px",
                                                    color: "#fff"
                                                }}
                                            />

                                            <Legend />

                                            <Bar
                                                dataKey="Successful"
                                                fill="#22c55e"
                                                radius={[6, 6, 0, 0]}
                                            />

                                            <Bar
                                                dataKey="Failed"
                                                fill="#ef4444"
                                                radius={[6, 6, 0, 0]}
                                            />

                                        </BarChart>
                                    </ResponsiveContainer>

                                </div>
                            </div>

                        </div>

                        {/* Recent Deliveries */}

                        <RecentDeliveries
                            deliveries={stats.recentDeliveries || []}
                        />
                    </>
                )}

            </div>

        </DashboardLayout>
    );
}

export default Dashboard;