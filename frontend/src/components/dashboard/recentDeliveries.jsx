import { Clock, ArrowUpRight, Inbox } from "lucide-react";

function RecentDeliveries({ deliveries = [] }) {

    const getStatusStyle = (status) => {
        switch (status?.toUpperCase()) {
            case "DELIVERED":
                return "bg-green-500/10 text-green-400 border-green-500/20";

            case "FAILED":
                return "bg-red-500/10 text-red-400 border-red-500/20";

            case "PENDING":
                return "bg-yellow-500/10 text-yellow-400 border-yellow-500/20";

            default:
                return "bg-slate-500/10 text-slate-400 border-slate-500/20";
        }
    };

    const formatDate = (date) => {
        if (!date) return "—";

        return new Date(date).toLocaleString();
    };

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">

            <div className="p-6 border-b border-slate-800 flex items-center justify-between">

                <div>
                    <h3 className="text-lg font-semibold text-white">
                        Recent Deliveries
                    </h3>

                    <p className="text-sm text-slate-400 mt-1">
                        Your latest webhook delivery activity
                    </p>
                </div>

                <Clock size={20} className="text-slate-400" />

            </div>

            {deliveries.length === 0 ? (

                <div className="py-16 px-6 text-center">

                    <div className="w-14 h-14 mx-auto rounded-full bg-slate-800 flex items-center justify-center">
                        <Inbox size={26} className="text-slate-400" />
                    </div>

                    <h4 className="text-white font-medium mt-4">
                        No deliveries yet
                    </h4>

                    <p className="text-sm text-slate-400 mt-2 max-w-sm mx-auto">
                        Your webhook deliveries will appear here once you start submitting events.
                    </p>

                </div>

            ) : (

                <div className="overflow-x-auto">

                    <table className="w-full text-left">

                        <thead className="bg-slate-800/50">

                        <tr className="text-xs uppercase tracking-wider text-slate-400">

                            <th className="px-6 py-4">Delivery</th>
                            <th className="px-6 py-4">Event ID</th>
                            <th className="px-6 py-4">Endpoint ID</th>
                            <th className="px-6 py-4">Status</th>
                            <th className="px-6 py-4">HTTP Code</th>
                            <th className="px-6 py-4">Attempts</th>
                            <th className="px-6 py-4">Created At</th>

                        </tr>

                        </thead>

                        <tbody className="divide-y divide-slate-800">

                        {deliveries.map((delivery) => (

                            <tr
                                key={delivery.id}
                                className="hover:bg-slate-800/40 transition"
                            >

                                <td className="px-6 py-4 text-sm font-medium text-white">
                                    #{delivery.id}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-300">
                                    #{delivery.eventId ?? "—"}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-300">
                                    #{delivery.endpointId ?? "—"}
                                </td>

                                <td className="px-6 py-4">

                                        <span className={`px-3 py-1 rounded-full text-xs font-medium border ${getStatusStyle(delivery.status)}`}>
                                            {delivery.status}
                                        </span>

                                </td>

                                <td className="px-6 py-4 text-sm text-slate-300">
                                    {delivery.lastResponseCode ?? "—"}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-300">
                                    {delivery.attemptCount ?? 0}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-400 whitespace-nowrap">
                                    {formatDate(delivery.createdAt)}
                                </td>

                            </tr>

                        ))}

                        </tbody>

                    </table>

                </div>

            )}

        </div>
    );
}

export default RecentDeliveries;