function WebhookStatus({ active }) {

    return (
        <span
            className={`px-3 py-1 rounded-full text-xs font-medium border ${
                active
                    ? "bg-green-500/10 text-green-400 border-green-500/20"
                    : "bg-red-500/10 text-red-400 border-red-500/20"
            }`}
        >
            {active ? "Active" : "Inactive"}
        </span>
    );
}

export default WebhookStatus;