import { Bell, User } from "lucide-react";
import { useAuth } from "../../context/AuthContext";

function Header({ title = "Overview" }) {

    const { user } = useAuth();

    return (
        <header className="h-20 bg-slate-900 border-b border-slate-800 px-8 flex items-center justify-between">

            <div>
                <h1 className="text-2xl font-bold text-white">
                    {title}
                </h1>

                <p className="text-sm text-slate-400">
                    Welcome back to HookFlow
                </p>
            </div>

            <div className="flex items-center gap-6">

                <button className="relative text-slate-400 hover:text-white">
                    <Bell size={22} />
                </button>

                <div className="flex items-center gap-3">

                    <div className="w-10 h-10 rounded-full bg-indigo-600 flex items-center justify-center">
                        <User size={20} className="text-white" />
                    </div>

                    <div>
                        <p className="text-sm font-medium text-white">
                            {user?.name || "User"}
                        </p>

                        <p className="text-xs text-slate-400">
                            {user?.email || ""}
                        </p>
                    </div>

                </div>

            </div>

        </header>
    );
}

export default Header;