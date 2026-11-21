import { AlertCircle, RefreshCw } from "lucide-react";

function DashboardError({ message, onRetry, retrying }) {

    return (
        <div className="bg-slate-900 border border-red-500/20 rounded-xl p-10 text-center">

            <div className="w-14 h-14 mx-auto rounded-full bg-red-500/10 flex items-center justify-center">
                <AlertCircle size={28} className="text-red-400" />
            </div>

            <h3 className="text-lg font-semibold text-white mt-5">
                Unable to load dashboard
            </h3>

            <p className="text-sm text-slate-400 mt-2">
                {message}
            </p>

            <button
                onClick={onRetry}
                disabled={retrying}
                className="mt-6 inline-flex items-center gap-2 px-5 py-2.5 bg-indigo-600 hover:bg-indigo-500 disabled:opacity-50 text-white rounded-lg transition"
            >
                <RefreshCw
                    size={16}
                    className={retrying ? "animate-spin" : ""}
                />

                {retrying ? "Retrying..." : "Try Again"}
            </button>

        </div>
    );
}

export default DashboardError;