function DashboardSkeleton() {
    return (
        <div className="space-y-8 animate-pulse">

            {/* Statistics Cards */}

            <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-3 gap-6">

                {[1, 2, 3, 4, 5, 6].map((item) => (
                    <div
                        key={item}
                        className="bg-slate-900 border border-slate-800 rounded-xl p-6"
                    >
                        <div className="flex justify-between items-center">
                            <div className="h-4 w-32 bg-slate-700 rounded" />
                            <div className="h-11 w-11 bg-slate-700 rounded-lg" />
                        </div>

                        <div className="h-9 w-20 bg-slate-700 rounded mt-6" />

                        <div className="h-3 w-36 bg-slate-800 rounded mt-4" />
                    </div>
                ))}

            </div>

            {/* Chart Skeletons */}

            <div className="grid grid-cols-1 xl:grid-cols-2 gap-6">

                {[1, 2].map((item) => (
                    <div
                        key={item}
                        className="bg-slate-900 border border-slate-800 rounded-xl p-6"
                    >
                        <div className="h-5 w-48 bg-slate-700 rounded" />

                        <div className="h-3 w-64 bg-slate-800 rounded mt-3" />

                        <div className="h-64 bg-slate-800/50 rounded-lg mt-6" />
                    </div>
                ))}

            </div>

            {/* Recent Deliveries Skeleton */}

            <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">

                <div className="h-5 w-48 bg-slate-700 rounded" />

                <div className="space-y-5 mt-8">

                    {[1, 2, 3, 4, 5].map((item) => (
                        <div
                            key={item}
                            className="h-8 bg-slate-800 rounded"
                        />
                    ))}

                </div>

            </div>

        </div>
    );
}

export default DashboardSkeleton;