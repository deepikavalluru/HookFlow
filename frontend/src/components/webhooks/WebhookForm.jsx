import { useState } from "react";
import { X, Webhook, Globe, Tag } from "lucide-react";

function WebhookForm({ onSubmit, onClose, submitting }) {

    const [formData, setFormData] = useState({
        name: "",
        url: "",
        eventType: ""
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value
        }));

        setErrors((previous) => ({
            ...previous,
            [name]: ""
        }));
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = "Webhook name is required.";
        } else if (formData.name.length > 100) {
            newErrors.name = "Name cannot exceed 100 characters.";
        }

        if (!formData.url.trim()) {
            newErrors.url = "Destination URL is required.";
        } else if (formData.url.length > 2048) {
            newErrors.url = "URL cannot exceed 2048 characters.";
        } else {
            try {
                const parsedUrl = new URL(formData.url);

                if (!["http:", "https:"].includes(parsedUrl.protocol)) {
                    newErrors.url = "URL must use HTTP or HTTPS.";
                }
            } catch {
                newErrors.url = "Please enter a valid URL.";
            }
        }

        if (!formData.eventType.trim()) {
            newErrors.eventType = "Event type is required.";
        } else if (formData.eventType.length > 100) {
            newErrors.eventType = "Event type cannot exceed 100 characters.";
        }

        setErrors(newErrors);

        return Object.keys(newErrors).length === 0;
    };

    const handleSubmit = (e) => {
        e.preventDefault();

        if (!validate()) return;

        onSubmit({
            name: formData.name.trim(),
            url: formData.url.trim(),
            eventType: formData.eventType.trim()
        });
    };

    return (
        <div className="fixed inset-0 z-50 bg-black/70 flex items-center justify-center p-4">

            <div className="bg-slate-900 border border-slate-800 rounded-xl w-full max-w-lg">

                {/* Header */}

                <div className="flex items-center justify-between p-6 border-b border-slate-800">

                    <div className="flex items-center gap-3">

                        <div className="p-2 bg-indigo-500/10 rounded-lg">
                            <Webhook className="text-indigo-400" size={22} />
                        </div>

                        <div>
                            <h2 className="text-lg font-semibold text-white">
                                Register Webhook
                            </h2>

                            <p className="text-sm text-slate-400">
                                Connect a new endpoint to HookFlow.
                            </p>
                        </div>

                    </div>

                    <button
                        type="button"
                        onClick={onClose}
                        disabled={submitting}
                        className="text-slate-400 hover:text-white"
                    >
                        <X size={22} />
                    </button>

                </div>

                {/* Form */}

                <form onSubmit={handleSubmit} className="p-6 space-y-5">

                    {/* Name */}

                    <div>
                        <label className="block text-sm font-medium text-slate-300 mb-2">
                            Webhook Name
                        </label>

                        <div className="relative">
                            <Webhook size={18} className="absolute left-3 top-3 text-slate-500" />

                            <input
                                type="text"
                                name="name"
                                value={formData.name}
                                onChange={handleChange}
                                maxLength={100}
                                placeholder="Order Notifications"
                                className="w-full bg-slate-800 border border-slate-700 rounded-lg pl-10 pr-4 py-3 text-white outline-none focus:border-indigo-500"
                            />
                        </div>

                        {errors.name && (
                            <p className="text-red-400 text-xs mt-2">
                                {errors.name}
                            </p>
                        )}
                    </div>

                    {/* URL */}

                    <div>
                        <label className="block text-sm font-medium text-slate-300 mb-2">
                            Destination URL
                        </label>

                        <div className="relative">
                            <Globe size={18} className="absolute left-3 top-3 text-slate-500" />

                            <input
                                type="url"
                                name="url"
                                value={formData.url}
                                onChange={handleChange}
                                maxLength={2048}
                                placeholder="https://example.com/webhooks"
                                className="w-full bg-slate-800 border border-slate-700 rounded-lg pl-10 pr-4 py-3 text-white outline-none focus:border-indigo-500"
                            />
                        </div>

                        {errors.url && (
                            <p className="text-red-400 text-xs mt-2">
                                {errors.url}
                            </p>
                        )}
                    </div>

                    {/* Event Type */}

                    <div>
                        <label className="block text-sm font-medium text-slate-300 mb-2">
                            Event Type
                        </label>

                        <div className="relative">
                            <Tag size={18} className="absolute left-3 top-3 text-slate-500" />

                            <input
                                type="text"
                                name="eventType"
                                value={formData.eventType}
                                onChange={handleChange}
                                maxLength={100}
                                placeholder="ORDER_CREATED"
                                className="w-full bg-slate-800 border border-slate-700 rounded-lg pl-10 pr-4 py-3 text-white outline-none focus:border-indigo-500"
                            />
                        </div>

                        {errors.eventType && (
                            <p className="text-red-400 text-xs mt-2">
                                {errors.eventType}
                            </p>
                        )}
                    </div>

                    {/* Information */}

                    <div className="bg-indigo-500/10 border border-indigo-500/20 rounded-lg p-4">

                        <p className="text-sm text-indigo-300">
                            HookFlow will automatically generate a secure signing secret when this endpoint is registered.
                        </p>

                    </div>

                    {/* Buttons */}

                    <div className="flex justify-end gap-3 pt-2">

                        <button
                            type="button"
                            onClick={onClose}
                            disabled={submitting}
                            className="px-4 py-2.5 text-slate-300 hover:bg-slate-800 rounded-lg transition"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={submitting}
                            className="px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg transition"
                        >
                            {submitting ? "Registering..." : "Register Webhook"}
                        </button>

                    </div>

                </form>

            </div>

        </div>
    );
}

export default WebhookForm;