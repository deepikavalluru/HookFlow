import { useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    User,
    Mail,
    ShieldCheck,
    Calendar,
    Hash,
    Webhook,
    LogOut,
    Copy,
    Check,
    KeyRound,
    Activity,
    Info,
} from "lucide-react";

import DashboardLayout from "../layouts/DashboardLayout";
import { useAuth } from "../context/AuthContext";

function Settings() {
    const { user, logout, isAuthenticated } = useAuth();
    const navigate = useNavigate();

    const [copied, setCopied] = useState(false);

    const handleLogout = () => {
        logout();
        navigate("/login", { replace: true });
    };

    const handleCopyId = async () => {
        try {
            await navigator.clipboard.writeText(String(user?.id));

            setCopied(true);

            setTimeout(() => {
                setCopied(false);
            }, 2000);
        } catch (error) {
            console.error("Unable to copy user ID:", error);
        }
    };

    const formatDate = (date) => {
        if (!date) return "Not available";

        return new Date(date).toLocaleDateString("en-IN", {
            day: "numeric",
            month: "long",
            year: "numeric",
        });
    };

    const initials = user?.name
        ? user.name
            .split(" ")
            .filter(Boolean)
            .map((part) => part[0])
            .join("")
            .slice(0, 2)
            .toUpperCase()
        : "U";

    return (
        <DashboardLayout>
            <div className="mx-auto max-w-5xl space-y-8">

                {/* Page Header */}
                <div>
                    <h1 className="text-2xl font-bold text-gray-200">
                        Settings
                    </h1>

                    <p className="mt-1 text-sm text-gray-500">
                        Manage your account information and preferences.
                    </p>
                </div>

                {/* Profile Section */}
                <section className="overflow-hidden rounded-xl border border-gray-800 bg-gray-900">

                    <div className="border-b border-gray-800 px-6 py-5">
                        <h2 className="text-lg font-semibold text-gray-200">
                            Profile Information
                        </h2>

                        <p className="mt-1 text-sm text-gray-500">
                            Your personal account information.
                        </p>
                    </div>

                    <div className="flex flex-col gap-6 p-6 sm:flex-row sm:items-center">

                        {/* Avatar */}
                        <div className="flex h-20 w-20 shrink-0 items-center justify-center rounded-full bg-indigo-600 text-2xl font-bold text-white">
                            {initials}
                        </div>

                        <div className="min-w-0">
                            <h3 className="text-xl font-semibold text-gray-200">
                                {user?.name || "User"}
                            </h3>

                            <p className="mt-1 text-sm text-gray-400">
                                {user?.email || "Email unavailable"}
                            </p>

                            <div className="mt-3 inline-flex items-center gap-2 rounded-full border border-green-800 bg-green-950/40 px-3 py-1 text-xs font-medium text-green-400">
                                <span className="h-2 w-2 rounded-full bg-green-500" />
                                Active Account
                            </div>
                        </div>
                    </div>
                </section>

                {/* Account Details */}
                <section className="rounded-xl border border-gray-800 bg-gray-900">

                    <div className="border-b border-gray-800 px-6 py-5">
                        <h2 className="text-lg font-semibold text-gray-200">
                            Account Details
                        </h2>

                        <p className="mt-1 text-sm text-gray-500">
                            Information associated with your HookFlow account.
                        </p>
                    </div>

                    <div className="divide-y divide-gray-800">

                        {/* Name */}
                        <div className="flex items-center gap-4 px-6 py-5">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <User size={19} className="text-indigo-400" />
                            </div>

                            <div className="min-w-0 flex-1">
                                <p className="text-xs text-gray-500">
                                    Full Name
                                </p>

                                <p className="mt-1 text-sm font-medium text-gray-200">
                                    {user?.name || "Not available"}
                                </p>
                            </div>
                        </div>

                        {/* Email */}
                        <div className="flex items-center gap-4 px-6 py-5">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <Mail size={19} className="text-indigo-400" />
                            </div>

                            <div className="min-w-0 flex-1">
                                <p className="text-xs text-gray-500">
                                    Email Address
                                </p>

                                <p className="mt-1 break-all text-sm font-medium text-gray-200">
                                    {user?.email || "Not available"}
                                </p>
                            </div>
                        </div>

                        {/* User ID */}
                        <div className="flex items-center gap-4 px-6 py-5">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <Hash size={19} className="text-indigo-400" />
                            </div>

                            <div className="min-w-0 flex-1">
                                <p className="text-xs text-gray-500">
                                    User ID
                                </p>

                                <p className="mt-1 text-sm font-medium text-gray-200">
                                    {user?.id ?? "Not available"}
                                </p>
                            </div>

                            {user?.id != null && (
                                <button
                                    onClick={handleCopyId}
                                    className="inline-flex items-center gap-2 rounded-lg border border-gray-700 px-3 py-2 text-xs text-gray-300 transition hover:bg-gray-800"
                                >
                                    {copied ? (
                                        <>
                                            <Check size={14} />
                                            Copied
                                        </>
                                    ) : (
                                        <>
                                            <Copy size={14} />
                                            Copy
                                        </>
                                    )}
                                </button>
                            )}
                        </div>

                        {/* Created At */}
                        <div className="flex items-center gap-4 px-6 py-5">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <Calendar
                                    size={19}
                                    className="text-indigo-400"
                                />
                            </div>

                            <div className="min-w-0 flex-1">
                                <p className="text-xs text-gray-500">
                                    Member Since
                                </p>

                                <p className="mt-1 text-sm font-medium text-gray-200">
                                    {formatDate(user?.createdAt)}
                                </p>
                            </div>
                        </div>

                    </div>
                </section>

                {/* Security Section */}
                <section className="rounded-xl border border-gray-800 bg-gray-900">

                    <div className="border-b border-gray-800 px-6 py-5">
                        <h2 className="text-lg font-semibold text-gray-200">
                            Security & Authentication
                        </h2>

                        <p className="mt-1 text-sm text-gray-500">
                            Your account authentication information.
                        </p>
                    </div>

                    <div className="space-y-5 p-6">

                        <div className="flex items-start gap-4">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <ShieldCheck
                                    size={20}
                                    className="text-green-400"
                                />
                            </div>

                            <div className="flex-1">
                                <h3 className="text-sm font-medium text-gray-200">
                                    Authentication Status
                                </h3>

                                <p className="mt-1 text-sm text-gray-500">
                                    {isAuthenticated
                                        ? "You are successfully authenticated."
                                        : "You are not authenticated."}
                                </p>

                                <span className="mt-3 inline-flex items-center gap-2 rounded-full bg-green-950 px-3 py-1 text-xs font-medium text-green-400">
                                    <Check size={13} />
                                    {isAuthenticated ? "Verified" : "Inactive"}
                                </span>
                            </div>
                        </div>

                        <div className="flex items-start gap-4 border-t border-gray-800 pt-5">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <KeyRound
                                    size={20}
                                    className="text-indigo-400"
                                />
                            </div>

                            <div className="flex-1">
                                <h3 className="text-sm font-medium text-gray-200">
                                    JWT Authentication
                                </h3>

                                <p className="mt-1 text-sm leading-6 text-gray-500">
                                    HookFlow uses JSON Web Tokens (JWT) to
                                    authenticate API requests. Your access
                                    token is attached automatically to
                                    authenticated API calls.
                                </p>
                            </div>
                        </div>

                        <div className="flex items-start gap-3 rounded-lg border border-blue-900/50 bg-blue-950/30 p-4">
                            <Info
                                size={18}
                                className="mt-0.5 shrink-0 text-blue-400"
                            />

                            <p className="text-xs leading-5 text-blue-300">
                                Never share your authentication token or
                                webhook signing secrets with anyone.
                            </p>
                        </div>

                    </div>
                </section>

                {/* Integrations Section */}
                <section className="rounded-xl border border-gray-800 bg-gray-900">

                    <div className="border-b border-gray-800 px-6 py-5">
                        <h2 className="text-lg font-semibold text-gray-200">
                            Integrations & Webhooks
                        </h2>

                        <p className="mt-1 text-sm text-gray-500">
                            Learn how to connect your applications to HookFlow.
                        </p>
                    </div>

                    <div className="space-y-5 p-6">

                        <div className="flex items-start gap-4">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <Webhook
                                    size={21}
                                    className="text-indigo-400"
                                />
                            </div>

                            <div className="flex-1">
                                <h3 className="text-sm font-medium text-gray-200">
                                    Register Webhook Endpoints
                                </h3>

                                <p className="mt-1 text-sm leading-6 text-gray-500">
                                    Register your application's HTTP endpoint
                                    to receive events from HookFlow.
                                </p>
                            </div>
                        </div>

                        <div className="flex items-start gap-4">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <Activity
                                    size={21}
                                    className="text-indigo-400"
                                />
                            </div>

                            <div className="flex-1">
                                <h3 className="text-sm font-medium text-gray-200">
                                    Submit Events
                                </h3>

                                <p className="mt-1 text-sm leading-6 text-gray-500">
                                    Submit events through the Events API.
                                    HookFlow automatically creates deliveries
                                    for matching active webhook endpoints.
                                </p>
                            </div>
                        </div>

                        <div className="flex items-start gap-4">
                            <div className="rounded-lg bg-gray-800 p-3">
                                <ShieldCheck
                                    size={21}
                                    className="text-green-400"
                                />
                            </div>

                            <div className="flex-1">
                                <h3 className="text-sm font-medium text-gray-200">
                                    Reliable Delivery
                                </h3>

                                <p className="mt-1 text-sm leading-6 text-gray-500">
                                    Monitor delivery attempts, inspect
                                    failures, and manually retry failed
                                    deliveries from your dashboard.
                                </p>
                            </div>
                        </div>

                        <button
                            onClick={() => navigate("/webhooks")}
                            className="w-full rounded-lg bg-indigo-600 px-4 py-3 text-sm font-medium text-white transition hover:bg-indigo-700 sm:w-auto"
                        >
                            Manage Webhooks
                        </button>

                    </div>
                </section>

                {/* Logout Section */}
                <section className="rounded-xl border border-red-900/50 bg-gray-900">

                    <div className="p-6">
                        <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">

                            <div>
                                <h2 className="text-lg font-semibold text-gray-200">
                                    Sign Out
                                </h2>

                                <p className="mt-1 text-sm text-gray-500">
                                    Sign out of your HookFlow account on this
                                    device.
                                </p>
                            </div>

                            <button
                                onClick={handleLogout}
                                className="inline-flex items-center justify-center gap-2 rounded-lg border border-red-800 bg-red-950/40 px-5 py-2.5 text-sm font-medium text-red-400 transition hover:bg-red-900/50"
                            >
                                <LogOut size={17} />
                                Logout
                            </button>

                        </div>
                    </div>
                </section>

            </div>
        </DashboardLayout>
    );
}

export default Settings;