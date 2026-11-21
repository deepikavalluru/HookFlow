import { Webhook, ExternalLink, Inbox } from "lucide-react";
import WebhookStatus from "./WebhookStatus";

function WebhookTable({ webhooks = [], onView }) {

    const formatDate = (date) => {
        if (!date) return "—";

        return new Date(date).toLocaleDateString("en-IN", {
            day: "2-digit",
            month: "short",
            year: "numeric"
        });
    };

    return (
        <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">

            <div className="p-6 border-b border-slate-800 flex items-center justify-between">

                <div>
                    <h3 className="text-lg font-semibold text-white">
                        Registered Webhooks
                    </h3>

                    <p className="text-sm text-slate-400 mt-1">
                        Manage your webhook endpoints
                    </p>
                </div>

                <Webhook className="text-indigo-400" size={22} />

            </div>

            {webhooks.length === 0 ? (

                <div className="py-16 px-6 text-center">

                    <Inbox size={32} className="mx-auto text-slate-500" />

                    <h4 className="text-white font-medium mt-4">
                        No webhooks registered
                    </h4>

                    <p className="text-sm text-slate-400 mt-2">
                        Register your first webhook endpoint to get started.
                    </p>

                </div>

            ) : (

                <div className="overflow-x-auto">

                    <table className="w-full text-left">

                        <thead className="bg-slate-800/50">

                        <tr className="text-xs uppercase tracking-wider text-slate-400">

                            <th className="px-6 py-4">Name</th>
                            <th className="px-6 py-4">Endpoint URL</th>
                            <th className="px-6 py-4">Event Type</th>
                            <th className="px-6 py-4">Status</th>
                            <th className="px-6 py-4">Created</th>
                            <th className="px-6 py-4">Action</th>

                        </tr>

                        </thead>

                        <tbody className="divide-y divide-slate-800">

                        {webhooks.map((webhook) => (

                            <tr
                                key={webhook.id}
                                className="hover:bg-slate-800/40 transition"
                            >

                                <td className="px-6 py-4 text-sm font-medium text-white whitespace-nowrap">
                                    {webhook.name}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-400 max-w-xs truncate">
                                    {webhook.url}
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-300 whitespace-nowrap">
                                    {webhook.eventType}
                                </td>

                                <td className="px-6 py-4">
                                    <WebhookStatus active={webhook.active} />
                                </td>

                                <td className="px-6 py-4 text-sm text-slate-400 whitespace-nowrap">
                                    {formatDate(webhook.createdAt)}
                                </td>

                                <td className="px-6 py-4">

                                    <button
                                        onClick={() => onView(webhook.id)}
                                        className="text-indigo-400 hover:text-indigo-300 flex items-center gap-2 text-sm"
                                    >
                                        View
                                        <ExternalLink size={15} />
                                    </button>

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

export default WebhookTable;