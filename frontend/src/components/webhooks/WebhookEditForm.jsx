import { useState } from "react";
import { X } from "lucide-react";

function WebhookEditForm({ webhook, onSubmit, onClose, submitting }) {
    const [formData, setFormData] = useState({
        name: webhook.name || "",
        url: webhook.url || "",
        eventType: webhook.eventType || "",
    });

    const [errors, setErrors] = useState({});

    const handleChange = (e) => {
        const { name, value } = e.target;

        setFormData((previous) => ({
            ...previous,
            [name]: value,
        }));

        setErrors((previous) => ({
            ...previous,
            [name]: "",
        }));
    };

    const validate = () => {
        const newErrors = {};

        if (!formData.name.trim()) {
            newErrors.name = "Webhook name is required.";
        } else if (formData.name.trim().length > 100) {
            newErrors.name = "Name cannot exceed 100 characters.";
        }

        if (!formData.url.trim()) {
            newErrors.url = "Endpoint URL is required.";
        } else {
            try {
                const parsedUrl = new URL(formData.url);

                if (!["http:", "https:"].includes(parsedUrl.protocol)) {
                    newErrors.url = "Only HTTP and HTTPS URLs are allowed.";
                }
            } catch {
                newErrors.url = "Enter a valid URL.";
            }

            if (formData.url.length > 2048) {
                newErrors.url = "URL cannot exceed 2048 characters.";
            }
        }

        if (!formData.eventType.trim()) {
            newErrors.eventType = "Event type is required.";
        } else if (formData.eventType.trim().length > 100) {
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
            eventType: formData.eventType.trim(),
        });
    };

    const inputClass =
        "w-full rounded-lg border border-gray-600 bg-gray-800 px-4 py-2.5 text-sm text-gray-200 outline-none transition focus:border-indigo-500 focus:ring-2 focus:ring-indigo-500/20";

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 p-4">
            <div className="w-full max-w-lg rounded-xl border border-gray-700 bg-gray-900 shadow-xl">

                {/* Header */}
                <div className="flex items-center justify-between border-b border-gray-700 px-6 py-4">
                    <h2 className="text-lg font-semibold text-gray-300">
                        Edit Webhook
                    </h2>

                    <button
                        type="button"
                        onClick={onClose}
                        disabled={submitting}
                        className="text-gray-400 transition hover:text-gray-200"
                    >
                        <X size={20} />
                    </button>
                </div>

                {/* Form */}
                <form onSubmit={handleSubmit} className="space-y-5 p-6">

                    {/* Name */}
                    <div>
                        <label className="mb-2 block text-sm font-medium text-gray-300">
                            Webhook Name
                        </label>

                        <input
                            type="text"
                            name="name"
                            value={formData.name}
                            onChange={handleChange}
                            maxLength={100}
                            className={inputClass}
                            placeholder="e.g. Payment Notifications"
                        />

                        {errors.name && (
                            <p className="mt-1 text-xs text-red-400">{errors.name}</p>
                        )}
                    </div>

                    {/* URL */}
                    <div>
                        <label className="mb-2 block text-sm font-medium text-gray-300">
                            Endpoint URL
                        </label>

                        <input
                            type="url"
                            name="url"
                            value={formData.url}
                            onChange={handleChange}
                            maxLength={2048}
                            className={inputClass}
                            placeholder="https://example.com/webhook"
                        />

                        {errors.url && (
                            <p className="mt-1 text-xs text-red-400">{errors.url}</p>
                        )}
                    </div>

                    {/* Event Type */}
                    <div>
                        <label className="mb-2 block text-sm font-medium text-gray-300">
                            Event Type
                        </label>

                        <input
                            type="text"
                            name="eventType"
                            value={formData.eventType}
                            onChange={handleChange}
                            maxLength={100}
                            className={inputClass}
                            placeholder="e.g. order.created"
                        />

                        {errors.eventType && (
                            <p className="mt-1 text-xs text-red-400">
                                {errors.eventType}
                            </p>
                        )}
                    </div>

                    {/* Actions */}
                    <div className="flex justify-end gap-3 border-t border-gray-700 pt-4">
                        <button
                            type="button"
                            onClick={onClose}
                            disabled={submitting}
                            className="rounded-lg border border-gray-600 px-4 py-2 text-sm font-medium text-gray-300 transition hover:bg-gray-800 disabled:opacity-50"
                        >
                            Cancel
                        </button>

                        <button
                            type="submit"
                            disabled={submitting}
                            className="rounded-lg bg-indigo-600 px-4 py-2 text-sm font-medium text-white transition hover:bg-indigo-700 disabled:cursor-not-allowed disabled:opacity-50"
                        >
                            {submitting ? "Saving..." : "Save Changes"}
                        </button>
                    </div>
                </form>
            </div>
        </div>
    );
}

export default WebhookEditForm;